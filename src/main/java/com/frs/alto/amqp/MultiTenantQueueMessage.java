package com.frs.alto.amqp;

public class MultiTenantQueueMessage {
	
	private String tenantId;
	private String message;
	public String getTenantId() {
		return tenantId;
	}
	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	
	

}
