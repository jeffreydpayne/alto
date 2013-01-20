package com.frs.alto.core;


public class RestTenantResolver implements TenantResolver {

	private String apiUrl = null;
	private String apiId = null;
	private String secretKey = null;
	
	
	@Override
	public TenantMetaData resolve(String hostName) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getApiUrl() {
		return apiUrl;
	}


	public void setApiUrl(String apiUrl) {
		this.apiUrl = apiUrl;
	}


	public String getApiId() {
		return apiId;
	}


	public void setApiId(String apiId) {
		this.apiId = apiId;
	}


	public String getSecretKey() {
		return secretKey;
	}


	public void setSecretKey(String secretKey) {
		this.secretKey = secretKey;
	}
	
	
	
	
}
