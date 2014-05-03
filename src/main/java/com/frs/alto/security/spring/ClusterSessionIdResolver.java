package com.frs.alto.security.spring;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface ClusterSessionIdResolver {
	
	public String resolve(HttpServletRequest request);
	public void sendClusterSessionId(HttpServletRequest request, HttpServletResponse response, String clusterSessionId);

}
