package com.frs.alto.web;

import java.util.Collection;

import javax.servlet.http.HttpServletRequest;

public class NullClientTypeResolver implements ClientTypeResolver {

	@Override
	public Collection<ClientType> resolveClientType(HttpServletRequest request) {
		return null;
	}

}
