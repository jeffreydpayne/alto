package com.frs.alto.security;

import java.util.Locale;
import java.util.TimeZone;

import org.springframework.security.core.context.SecurityContextImpl;

public class ClusterSecurityContext extends SecurityContextImpl {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -8928859324640631687L;
	
	public ClusterSecurityContext() {
		super();
	}
	
	public ClusterSecurityContext(String clusterSessionId) {
		super();
		this.clusterSessionId = clusterSessionId;
	}
	
	private Locale locale = null;
	private TimeZone timeZone = null;
	private String clusterSessionId = null;
	private long authTimestamp = 0l;
	private long lastUpdateTimestamp = 0;
	
	public Locale getLocale() {
		return locale;
	}
	public void setLocale(Locale locale) {
		this.locale = locale;
	}
	public TimeZone getTimeZone() {
		return timeZone;
	}
	public void setTimeZone(TimeZone timeZone) {
		this.timeZone = timeZone;
	}
	public String getClusterSessionId() {
		return clusterSessionId;
	}
	public void setClusterSessionId(String clusterSessionId) {
		this.clusterSessionId = clusterSessionId;
	}

	public long getAuthTimestamp() {
		return authTimestamp;
	}

	public void setAuthTimestamp(long authTimestamp) {
		this.authTimestamp = authTimestamp;
	}

	public long getLastUpdateTimestamp() {
		return lastUpdateTimestamp;
	}

	public void setLastUpdateTimestamp(long lastUpdateTimestamp) {
		this.lastUpdateTimestamp = lastUpdateTimestamp;
	}
	

}
