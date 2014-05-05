package com.frs.alto.security.cluster;

import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebArgumentResolver;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.ServletWebRequest;

public class ClusterSessionMetaDataArgumentResolver implements WebArgumentResolver {
	
	private ClusterSecurityController securityController;

	@Override
	public Object resolveArgument(MethodParameter methodParameter, NativeWebRequest webRequest) throws Exception {
		
		if (SessionMetaData.class.isAssignableFrom(methodParameter.getParameterType())) {
			ServletWebRequest request = (ServletWebRequest)webRequest;
			return request.getAttribute("session", ServletWebRequest.SCOPE_REQUEST);		
			
		}
		
		return UNRESOLVED;
	}

	public ClusterSecurityController getSecurityController() {
		return securityController;
	}

	public void setSecurityController(ClusterSecurityController securityController) {
		this.securityController = securityController;
	}
	
	
	

}
