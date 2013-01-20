package com.frs.alto.web;

import java.util.Collection;

import javax.servlet.http.HttpServletRequest;

public interface ClientTypeResolver {
	
	public Collection<ClientType> resolveClientType(HttpServletRequest request);

}
