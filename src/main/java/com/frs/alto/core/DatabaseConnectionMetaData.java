package com.frs.alto.core;

public interface DatabaseConnectionMetaData {
	
	public String getConnectionId();
	public String getServerName();
	public String getServerId();
	public String getSchemaName();
	public String getUserName();
	public String getPassword();

}
