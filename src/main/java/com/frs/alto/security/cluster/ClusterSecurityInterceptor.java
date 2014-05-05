package com.frs.alto.security.cluster;

import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

public class ClusterSecurityInterceptor extends HandlerInterceptorAdapter {
	
	public final static String AUTH_HEADER_PREFIX = "FRS";
	
	private final static DateFormat ISO_DATE = new SimpleDateFormat("yyyy-MM-dd'T'HH:mmZ");
	
	private long dateWindow = 15 * 60 * 1000; //15 minutes
	
	private static final Charset CHAR_SET = Charset.forName("UTF-8");
	
	private ClusterSecurityController securityController;
  

	
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
	
		SessionMetaData session = securityController.acquireSession(request, response);
		
		
		boolean authorized = false;
		
		if (handler instanceof HandlerMethod) {
			HandlerMethod method = (HandlerMethod)handler;
			Permissions annot = method.getMethodAnnotation(Permissions.class);
			if (annot == null) {
				authorized = true;
			}
			else {

				authorized = true;
				for (String permCode : annot.value()) {
					switch (annot.grouping()) {
						case ALL:
							if (!session.hasPermission(permCode)) {
								authorized = false;
							}
							break;
						case ANY:
							if (session.hasPermission(permCode)) {
								authorized = true;
							}
							break;
					}
				}
				
			}
			method.getMethodParameters();
			
		}
				
		return authorized;
		
	}


	/*
	
	protected SessionCollaborator checkHeaders(HttpServletRequest request, Object handler) {
		
		SessionCollaborator collab = null;
		
		String authHeader = request.getHeader("Authorization");
		if ( (authHeader != null) && authHeader.startsWith(AUTH_HEADER_PREFIX)) {
			
			String[] tokens = StringUtils.split(authHeader, " :");
			if (tokens.length == 2) {
				//session token
						
				collab = getSystemBean().getSessionByToken(tokens[1]);
			
			}
			else if (tokens.length == 3) {
				//digest authentication
				String dateHeader = request.getHeader("Date");
				if (dateHeader == null) {
					throw new SecurityException("Date header required for FRS hmac authentication.");
				}
				Date dt = null;
				try {
					dt = ISO_DATE.parse(dateHeader);
				}
				catch (ParseException e) {
					throw new SecurityException("Invalid date header.");
				}
				Date now = new Date();
				long diff = Math.abs(now.getTime() - dt.getTime());
				if (diff > dateWindow) {
					throw new SecurityException("Date header is stale.");
				}
				UserProfile user = getUserService().findByApiID(tokens[1]);
				String secretKey = null;
				if (user == null) {
					MobileDevice device = getDeviceService().findByApiId(tokens[1]);
					user = device.getUserProfile();
					secretKey = device.getSecretKey();
				}
				else {
					secretKey = user.getSecretKey();
				}
				if (user == null) {
					throw new SecurityException("Invalid api key");
				}
				
				String computeAuth = null;
				String canonicalString = assembleCanonicalString(tokens[1], dateHeader, request);
				try {
					Mac mac = Mac.getInstance("HmacSHA1");
	    		    SecretKeySpec secret = new SecretKeySpec(Base64.decodeBase64(secretKey.getBytes(CHAR_SET)),"HmacSHA1");
	    		    mac.init(secret);
	    		    byte[] digest = mac.doFinal(canonicalString.getBytes(CHAR_SET));
	    		    computeAuth = new String(Base64.encodeBase64(digest), CHAR_SET);
				}
				catch (Exception e) {
					throw new SecurityException(e);
				}
				
				if (computeAuth.equals(tokens[2])) {
					try {
						return getSessionCollaborator(request, user);
					}
					catch (Exception e) {
						throw new SecurityException(e);
					}
				}
				else {
					throw new SecurityException("Invalid message authentication code");
				}
									
			}
			
		}
		
		if (collab != null) {
			request.getSession().setAttribute(SessionCollaborator.SESSION_COLLABORATOR_KEY, collab);
		}
		
		return collab;
		
	}
	
	private String assembleCanonicalString(String apiId, String dateHeader, HttpServletRequest request) {
		
		String url = request.getServletPath() + request.getPathInfo();
		
		String salt = request.getHeader("Salt");
		
		Map<String, String> sortedParams = new TreeMap<String, String>();
		Enumeration<String> params = request.getParameterNames();
		String key = null;
		while (params.hasMoreElements()) {
			key = params.nextElement();
			sortedParams.put(key, request.getParameter(key));
		}
			
		StringBuilder sb = new StringBuilder();
		sb.append(url + "\n");
		sb.append(apiId + "\n");
		sb.append(dateHeader + "\n");
		if (salt != null) {
			sb.append(salt + "\n");
		}
		
		for (String value : sortedParams.values()) {
			sb.append(value + "\n");
		}
		
		
		
		return sb.toString();
		
	}
	
	public SessionCollaborator getSessionCollaborator(HttpServletRequest request, UserProfile user)
			throws Exception {
		
		HttpSession session = request.getSession(true);
		
		SessionCollaborator theCollab = new SessionCollaborator();
		if (theCollab.getUser() == null) {
			theCollab.setUser(user);
			systemBean.afterLogin(theCollab);
		}
		
		
		
		theCollab.setClientIpAddress(NetworkUtils.getRemoteAddress(request));
			
		session.setAttribute(SessionCollaborator.SESSION_COLLABORATOR_KEY, theCollab);
		
		if (theCollab.getUser() == null) {
			throw new IllegalStateException("No session user.");
		}

		return theCollab;

	}
	
	*/

	
	
	

}
