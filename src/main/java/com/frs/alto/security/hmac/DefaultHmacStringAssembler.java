package com.frs.alto.security.hmac;

import java.util.Enumeration;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;

/**
 * 
 * The default string assembler creates a string consisting of the request path,
 * the date, the apiId, the salt (if provided) and all url parameters 
 * sorted alphabetically.
 * 
 * The salt is not required, but strongly recommended to ensure that even with the
 * Date header, the same hash is never presented twice.  We recommend that clients
 * send cryptographically random md5 or sha1 hashes as salts.
 *  
 * @author Jeffrey Payne
 *
 */

public class DefaultHmacStringAssembler implements HmacStringAssembler {
	
	private boolean saltRequired = true;
	private String saltHeaderName = "Salt";
	
	
	@Override
	public String assemble(HttpServletRequest request, HmacPrincipal principal) {
		
		StringBuilder sb = new StringBuilder();
		
		String saltHeader = request.getHeader(saltHeaderName);
		
		if (saltRequired && (saltHeader == null) ) {
			return null;
		}
		
		
		String url = request.getServletPath() + request.getPathInfo();
		
		sb.append(url +"\n");
		sb.append(request.getHeader("Date") + "\n");
		sb.append(principal.getApiId() + "\n");
		
		if (saltHeader != null) {
			sb.append(saltHeader + "\n");
		}

		Map<String, String> sortedParams = new TreeMap<String, String>();
		Enumeration<String> params = request.getParameterNames();
		String key = null;
		while (params.hasMoreElements()) {
			key = params.nextElement();
			sortedParams.put(key, request.getParameter(key));
		}

		for (String value : sortedParams.values()) {
			sb.append(value + "\n");
		}

		return sb.toString();

	}


	public boolean isSaltRequired() {
		return saltRequired;
	}


	public void setSaltRequired(boolean saltRequired) {
		this.saltRequired = saltRequired;
	}


	public String getSaltHeaderName() {
		return saltHeaderName;
	}


	public void setSaltHeaderName(String saltHeaderName) {
		this.saltHeaderName = saltHeaderName;
	}

	
	
	
}
