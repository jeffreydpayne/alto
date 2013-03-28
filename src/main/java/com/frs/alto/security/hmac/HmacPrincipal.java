package com.frs.alto.security.hmac;

import java.security.Principal;

import org.springframework.security.core.userdetails.UserDetails;

public class HmacPrincipal {
	
	private UserDetails user = null;
	private String apiId = null;
	private String secretKey = null;
	private HmacClient client = null;
	private boolean enabled = true;
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
	public HmacClient getClient() {
		return client;
	}
	public void setClient(HmacClient client) {
		this.client = client;
	}
	public boolean isEnabled() {
		return enabled;
	}
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	public UserDetails getUser() {
		return user;
	}
	public void setUser(UserDetails user) {
		this.user = user;
	}
	
	

}
