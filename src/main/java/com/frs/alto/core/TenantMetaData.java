package com.frs.alto.core;

import java.util.Map;
import java.util.Properties;

public interface TenantMetaData {
	
	public String getTenantName();
	public String getTenantIdentifier();
	public Properties getTenantProperties();
	public DatabaseConnectionMetaData getDefaultDatabaseMetaData();
	public Map<String, DatabaseConnectionMetaData> getDatabaseConnectionMetaData();
	public DatabaseConnectionMetaData getDatabaseConnectionMetaData(String connectionId);
	
	

}
