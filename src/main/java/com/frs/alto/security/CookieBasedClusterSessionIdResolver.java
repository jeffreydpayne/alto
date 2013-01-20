package com.frs.alto.security;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class CookieBasedClusterSessionIdResolver implements ClusterSessionIdResolver {

	public final static String CLUSTER_SESSION_COOKIE_NAME = "CSESSIONID";
	private String clusterSessionIdCookieName = CLUSTER_SESSION_COOKIE_NAME;
	
	@Override
	public String resolve(HttpServletRequest request) {
		return getClusterSessionId(request);
	}
	

	protected String getClusterSessionId(HttpServletRequest request) {
		
		if (request.getCookies() != null) {
			for (Cookie cookie : request.getCookies()) {
				if (cookie.getName().equals(clusterSessionIdCookieName)) {
					return cookie.getValue();
				}
			}
		}
		
		return null;
		
	}


	@Override
	public void sendClusterSessionId(HttpServletRequest request,HttpServletResponse response, String clusterSessionId) {
		
		response.addCookie(new Cookie(clusterSessionIdCookieName, clusterSessionId));
		
		
	}
	
	
	

}
