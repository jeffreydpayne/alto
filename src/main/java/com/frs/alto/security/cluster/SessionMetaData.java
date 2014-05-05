package com.frs.alto.security.cluster;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.frs.alto.domain.BaseDomainObject;

public class SessionMetaData extends BaseDomainObject {
	
	private String firstRequestId;
	private Date firstRequestDate;
	private Date lastRequestDate;
	private boolean dead = true;
	private Date killDate = null;
	private String loggingId;
	private String principalId;
	private boolean authenticated = false;
	private String requestForgeryToken = null;
	
	@JsonIgnore
	private ClusterPrincipal principal;

	public String getSessionId() {
		return getObjectIdentifier();
	}

	public void setSessionId(String sessionId) {
		setObjectIdentifier(sessionId);
	}

	public String getFirstRequestId() {
		return firstRequestId;
	}

	public void setFirstRequestId(String firstRequestId) {
		this.firstRequestId = firstRequestId;
	}

	public boolean isDead() {
		return dead;
	}

	public void setDead(boolean dead) {
		this.dead = dead;
	}

	public Date getKillDate() {
		return killDate;
	}

	public void setKillDate(Date killDate) {
		this.killDate = killDate;
	}

	public String getPrincipalId() {
		return principalId;
	}

	public void setPrincipalId(String principalId) {
		this.principalId = principalId;
	}

	public boolean isAuthenticated() {
		return authenticated;
	}

	public void setAuthenticated(boolean authenticated) {
		this.authenticated = authenticated;
	}

	public ClusterPrincipal getPrincipal() {
		return principal;
	}

	public void setPrincipal(ClusterPrincipal principal) {
		this.principal = principal;
	}

	public String getRequestForgeryToken() {
		return requestForgeryToken;
	}

	public void setRequestForgeryToken(String requestForgeryToken) {
		this.requestForgeryToken = requestForgeryToken;
	}

	public Date getFirstRequestDate() {
		return firstRequestDate;
	}

	public void setFirstRequestDate(Date firstRequestDate) {
		this.firstRequestDate = firstRequestDate;
	}

	public Date getLastRequestDate() {
		return lastRequestDate;
	}

	public void setLastRequestDate(Date lastRequestDate) {
		this.lastRequestDate = lastRequestDate;
	}

	public String getLoggingId() {
		return loggingId;
	}

	public void setLoggingId(String loggingId) {
		this.loggingId = loggingId;
	}
	
	public boolean hasPermission(String permCode) {
		if (principal == null) {
			return false;
		}
		else if (principal.getGrantedPermissionCodes() == null) {
			return false;
		}
		else {
			return principal.getGrantedPermissionCodes().contains(permCode);
		}
	}
	

}
