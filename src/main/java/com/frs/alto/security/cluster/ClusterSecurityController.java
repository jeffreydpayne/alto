package com.frs.alto.security.cluster;

import java.util.Calendar;
import java.util.Date;
import java.util.logging.Logger;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.frs.alto.id.IdentifierGenerator;
import com.frs.alto.id.LocalUUIDGenerator;
import com.frs.alto.id.SecureRandomIdGenerator;

public class ClusterSecurityController {
	
	private Logger logger = Logger.getLogger(ClusterSecurityController.class.getName());
	
	private int sessionTimeout = 20; //in minute
	private boolean requestForgeryTokenEnabled = false;
	private String requestTokenHeaderName = "xsrftoken";
	private String sessionCookieName = "_altosessionid";
	private boolean sessionCookieHttpOnly = true;
	private int sessionLimitPerUser = 1;
	private boolean missingAnnotationPrevention = true;
	private String guestPermissionCode = "guest";
	
	private IdentifierGenerator idGenerator = new SecureRandomIdGenerator();
	private IdentifierGenerator uuidGenerator = new LocalUUIDGenerator();
	
	private PrincipalRepository principalRepository;
	private SessionRepository sessionRepository;
	
	private Authenticator primaryAuthenticator = null;
	private Authenticator secondaryAuthenticator = null;  //used for mfe logins
	
	
	public SessionMetaData acquireSession(HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		SessionMetaData session = (SessionMetaData)request.getAttribute("session");
		
		if (session.isAuthenticated() && !session.isDead() && (session.getPrincipal() == null)) {
			session.setPrincipal(principalRepository.lookupPrincipal(session.getPrincipalId()));
		}
		
		return session;

	}
	
	protected boolean sessionExists(String sessionId) {
		
		SessionMetaData session = sessionRepository.findById(sessionId);
		return session != null;
		
	}
	
	protected String generateSessionId(SessionMetaData session) {
		
		String sessionId = idGenerator.generateStringIdentifier(session);
		
		while (sessionExists(sessionId)) {
			sessionId = idGenerator.generateStringIdentifier(session);
		}
		
		return sessionId;
		
	}
	

	public void killSession(HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		SessionMetaData session = acquireSession(request, response);
		session.setDead(true);
		session.setKillDate(new Date());
		
		sessionRepository.save(session);
		
		
	}
	
	public SessionMetaData createSession(HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		
		SessionMetaData session = new SessionMetaData();
		session.setAuthenticated(false);
		session.setFirstRequestId((String)request.getAttribute("requestId"));
		session.setFirstRequestDate(new Date());
		session.setLastRequestDate(new Date());
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MINUTE, sessionTimeout);
		session.setDead(false);
		session.setRequestForgeryToken(idGenerator.generateStringIdentifier(session));
		session.setSessionId(generateSessionId(session));
		session.setLoggingId(uuidGenerator.generateStringIdentifier(session));
		sessionRepository.save(session);
		
		response.addHeader(requestTokenHeaderName, session.getRequestForgeryToken());
		
		Cookie cookie = new Cookie(sessionCookieName, session.getSessionId());
		cookie.setMaxAge(sessionTimeout * 60);
		cookie.setPath("/");
		response.addCookie(cookie);
		
		return session;
	}
	
	public void sendUnauthorizedResponse(HttpServletRequest request, HttpServletResponse response, String reason) throws Exception {
		
		response.setStatus(403);
		
		SessionMetaData session = acquireSession(request, response);
		
		logger.warning("[IP: " + request.getRemoteAddr() + " Session: " + session.getLoggingId() + "] " + reason);
		
		
	}
	/**
	 * 
	 * The purpose of this method is to ensure that a valid session exists before each request is processed.
	 * It does not insure that a valid login is present, only that a valid, exclusive, non-expired session is
	 * present. 
	 * 
	 * 
	 * @param request
	 * @param response
	 * @throws Exception
	 */
	public void visitRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		request.setAttribute("requestId", idGenerator.generateStringIdentifier(request));
		
		String sessionId = null;
		
		Cookie[] cookies = request.getCookies();
		
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				if (cookie.getName().equals(sessionCookieName)) {
					sessionId = cookie.getValue();
				}
			}
		}
		
		SessionMetaData session = sessionRepository.findById(sessionId);
		if (session == null) {
			session = createSession(request, response);
			logger.info("Created new session: " + session.getLoggingId());
		}
		else if (session.isDead()) {
			logger.info("Attempt to access dead session: " + session.getLoggingId());
			session = createSession(request, response);
		}
		else {
			Calendar cal = Calendar.getInstance();
			cal.setTime(session.getLastRequestDate());
			cal.add(Calendar.MINUTE, sessionTimeout);
			Date now = new Date();
			if (now.after(cal.getTime())) {
				logger.info("Found timed out session");
				killSession(request, response);
				session = createSession(request, response);
			}
		}
		
		request.setAttribute("session", session);
		
		if (requestForgeryTokenEnabled && !request.getMethod().equals("GET")) {
			String headerToken = request.getHeader(requestTokenHeaderName);
			if ( (headerToken == null) || headerToken.equals(session.getRequestForgeryToken())) {
				sendUnauthorizedResponse(request, response, "Invalid XRF Token");
				return;
			}
		}
		
		
		session.setLastRequestDate(new Date());
		sessionRepository.save(session);
		
	}
	
	public SessionMetaData authenticate(HttpServletRequest request, HttpServletResponse response, String userName, String password, String secondaryToken) throws Exception {
		
		SessionMetaData session = acquireSession(request, response);
		
		ClusterPrincipal principal = primaryAuthenticator.authenticate(userName, password);
		
		
		
		if (principal == null) {
			session.failedLoginAttempt();
			sessionRepository.save(session);
			return session;
		}
		
		if (secondaryAuthenticator != null) {
			principal = secondaryAuthenticator.authenticate(userName, secondaryToken);
		}
		
		
		if (principal != null) {
			//we change session on login
			killSession(request, response);
			session = createSession(request, response);
			session.setPrincipalId(principal.getPrincipalId());
			session.setAuthenticated(true);
			session.setPrincipal(principal);
			sessionRepository.save(session);
			return session;
		}
		else {
			
			session.failedLoginAttempt();
			sessionRepository.save(session);
			return session;
		}
		
	}

	public int getSessionTimeout() {
		return sessionTimeout;
	}

	public void setSessionTimeout(int sessionTimeout) {
		this.sessionTimeout = sessionTimeout;
	}


	public boolean isRequestForgeryTokenEnabled() {
		return requestForgeryTokenEnabled;
	}

	public void setRequestForgeryTokenEnabled(boolean requestForgeryTokenEnabled) {
		this.requestForgeryTokenEnabled = requestForgeryTokenEnabled;
	}

	public IdentifierGenerator getUuidGenerator() {
		return uuidGenerator;
	}

	public void setUuidGenerator(IdentifierGenerator uuidGenerator) {
		this.uuidGenerator = uuidGenerator;
	}

	public String getRequestTokenHeaderName() {
		return requestTokenHeaderName;
	}

	public void setRequestTokenHeaderName(String requestTokenHeaderName) {
		this.requestTokenHeaderName = requestTokenHeaderName;
	}

	public String getSessionCookieName() {
		return sessionCookieName;
	}

	public void setSessionCookieName(String sessionCookieName) {
		this.sessionCookieName = sessionCookieName;
	}

	public boolean isSessionCookieHttpOnly() {
		return sessionCookieHttpOnly;
	}

	public void setSessionCookieHttpOnly(boolean sessionCookieHttpOnly) {
		this.sessionCookieHttpOnly = sessionCookieHttpOnly;
	}

	public int getSessionLimitPerUser() {
		return sessionLimitPerUser;
	}

	public void setSessionLimitPerUser(int sessionLimitPerUser) {
		this.sessionLimitPerUser = sessionLimitPerUser;
	}

	public IdentifierGenerator getIdGenerator() {
		return idGenerator;
	}

	public void setIdGenerator(IdentifierGenerator idGenerator) {
		this.idGenerator = idGenerator;
	}

	public PrincipalRepository getPrincipalRepository() {
		return principalRepository;
	}

	public void setPrincipalRepository(PrincipalRepository principalRepository) {
		this.principalRepository = principalRepository;
	}

	public SessionRepository getSessionRepository() {
		return sessionRepository;
	}

	public void setSessionRepository(SessionRepository sessionRepository) {
		this.sessionRepository = sessionRepository;
	}

	public Authenticator getPrimaryAuthenticator() {
		return primaryAuthenticator;
	}

	public void setPrimaryAuthenticator(Authenticator primaryAuthenticator) {
		this.primaryAuthenticator = primaryAuthenticator;
	}

	public Authenticator getSecondaryAuthenticator() {
		return secondaryAuthenticator;
	}

	public void setSecondaryAuthenticator(Authenticator secondaryAuthenticator) {
		this.secondaryAuthenticator = secondaryAuthenticator;
	}

	public boolean isMissingAnnotationPrevention() {
		return missingAnnotationPrevention;
	}

	public void setMissingAnnotationPrevention(boolean missingAnnotationPrevention) {
		this.missingAnnotationPrevention = missingAnnotationPrevention;
	}

	public String getGuestPermissionCode() {
		return guestPermissionCode;
	}

	public void setGuestPermissionCode(String guestPermissionCode) {
		this.guestPermissionCode = guestPermissionCode;
	}
	
	
	

}
