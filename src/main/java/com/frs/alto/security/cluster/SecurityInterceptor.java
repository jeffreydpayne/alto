package com.frs.alto.security.cluster;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.frs.kennewick.util.Permission;
import com.mf.roundhouse.core.domain.MobileDevice;
import com.mf.roundhouse.core.domain.UserProfile;
import com.mf.roundhouse.core.service.MobileDeviceService;
import com.mf.roundhouse.core.service.UserProfileService;
import com.mf.roundhouse.core.system.RoundhouseSystemBean;
import com.mf.roundhouse.core.system.SessionCollaborator;
import com.mf.roundhouse.core.util.NetworkUtils;
import java.nio.charset.Charset;

public class SecurityInterceptor extends HandlerInterceptorAdapter {
	
	public final static String AUTH_HEADER_PREFIX = "FRS";
	
	private final static DateFormat ISO_DATE = new SimpleDateFormat("yyyy-MM-dd'T'HH:mmZ");
	
	private long dateWindow = 15 * 60 * 1000; //15 minutes
	
	private static final Charset CHAR_SET = Charset.forName("UTF-8");
  

	
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		
		SessionCollaborator collab = getSessionCollaborator(request, handler);
		
		processStringLocale(request, response, collab);
		
		boolean authorized = false;
		
		if (handler instanceof HandlerMethod) {
			HandlerMethod method = (HandlerMethod)handler;
			Permissions annot = method.getMethodAnnotation(Permissions.class);
			if (annot == null) {
				authorized = true;
			}
			else {
				if (collab == null) {
					authorized = false;
				}
				else {
					Permission perm;
					authorized = true;
					for (String permCode : annot.value()) {
						perm = new Permission(permCode);
						switch (annot.grouping()) {
							case ALL:
								if (!collab.isAuthorized(perm)) {
									authorized = false;
								}
								break;
							case ANY:
								if (collab.isAuthorized(perm)) {
									authorized = true;
								}
								break;
						}
					}
				}
			}
			
		}
		
		//populate collab
				
		return authorized;
		
	}

	public RoundhouseSystemBean getSystemBean() {
		return systemBean;
	}

	public void setSystemBean(RoundhouseSystemBean systemBean) {
		this.systemBean = systemBean;
	}
	
	
	protected SessionCollaborator getSessionCollaborator(HttpServletRequest request, Object handler) {
		
		SessionCollaborator collab = checkHttpSession(request.getSession());
		if (collab != null) {
			return collab;
		}
		
		return checkHeaders(request, handler);
		
		
	}
	
	protected SessionCollaborator checkHttpSession(HttpSession session) {
		return (SessionCollaborator)session.getAttribute(SessionCollaborator.SESSION_COLLABORATOR_KEY);
		
	}
	
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

	public UserProfileService getUserService() {
		return userService;
	}

	public void setUserService(UserProfileService userService) {
		this.userService = userService;
	}

	public MobileDeviceService getDeviceService() {
		return deviceService;
	}

	public void setDeviceService(MobileDeviceService deviceService) {
		this.deviceService = deviceService;
	}
	
	
	

}
