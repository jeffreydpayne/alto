package com.frs.alto.core;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class DefaultTenantMetaData implements TenantMetaData {
	
	private String tenantName;
	private String tenantIdentifier;
	private Properties tenantProperties;
	private DatabaseConnectionMetaData defaultDatabaseMetaData;
	private Map<String, DatabaseConnectionMetaData> databaseConnectionMetaData;

	@Override
	public String getTenantName() {
		return tenantName;
	}

	@Override
	public String getTenantIdentifier() {
		return tenantIdentifier;
	}

	@Override
	public Properties getTenantProperties() {
		return tenantProperties;
	}

	@Override
	public DatabaseConnectionMetaData getDefaultDatabaseMetaData() {
		return defaultDatabaseMetaData;
	}

	@Override
	public Map<String, DatabaseConnectionMetaData> getDatabaseConnectionMetaData() {
		return databaseConnectionMetaData;
	}

	@Override
	public DatabaseConnectionMetaData getDatabaseConnectionMetaData(
			String connectionId) {
		if (databaseConnectionMetaData != null) {
			return databaseConnectionMetaData.get(connectionId);
		}
		else {
			return null;
		}
	}

	public void setTenantName(String tenantName) {
		this.tenantName = tenantName;
	}

	public void setTenantIdentifier(String tenantIdentifier) {
		this.tenantIdentifier = tenantIdentifier;
	}

	public void setTenantProperties(Properties tenantProperties) {
		this.tenantProperties = tenantProperties;
	}

	public void setDefaultDatabaseMetaData(
			DatabaseConnectionMetaData defaultDatabaseMetaData) {
		this.defaultDatabaseMetaData = defaultDatabaseMetaData;
	}

	public void setDatabaseConnectionMetaData(
			Map<String, DatabaseConnectionMetaData> databaseConnectionMetaData) {
		this.databaseConnectionMetaData = databaseConnectionMetaData;
	}
	
	public void addDatabaseMetaData(DatabaseConnectionMetaData db) {
		if (databaseConnectionMetaData == null) {
			databaseConnectionMetaData = new HashMap<String, DatabaseConnectionMetaData>();
		}
		databaseConnectionMetaData.put(db.getConnectionId(), db);
	}
	
	

}
