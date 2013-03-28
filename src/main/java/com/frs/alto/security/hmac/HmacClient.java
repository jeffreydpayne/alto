package com.frs.alto.security.hmac;

public interface HmacClient {
	
	public String getClientIdentifier();
	public String getApiId();
	public String getSecretKey();

}
