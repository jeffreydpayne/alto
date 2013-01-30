package com.frs.alto.tenant;

import org.springframework.beans.factory.InitializingBean;

import com.frs.alto.util.TenantUtils;

public class MultiTenantController implements InitializingBean {
	
	private String defaultTenantId = TenantUtils.DEFAULT_TENANT_ID;

	@Override
	public void afterPropertiesSet() throws Exception {
		TenantUtils.setDefaultTenantId(defaultTenantId);			
	}

	public String getDefaultTenantId() {
		return defaultTenantId;
	}

	public void setDefaultTenantId(String defaultTenantId) {
		this.defaultTenantId = defaultTenantId;
	}
	
	
	
	
	
	
	

}
