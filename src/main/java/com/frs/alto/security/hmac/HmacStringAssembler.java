package com.frs.alto.security.hmac;

import javax.servlet.http.HttpServletRequest;

public interface HmacStringAssembler {
	
	
	public String assemble(HttpServletRequest request, HmacPrincipal principal);

}
