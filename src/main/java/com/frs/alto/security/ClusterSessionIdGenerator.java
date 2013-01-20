package com.frs.alto.security;

import javax.servlet.http.HttpServletRequest;

public interface ClusterSessionIdGenerator {
	
	public String generate(HttpServletRequest request);
	
	

}
