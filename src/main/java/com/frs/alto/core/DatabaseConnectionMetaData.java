package com.frs.alto.core;

import com.frs.alto.jdbc.Databases;

public interface DatabaseConnectionMetaData {

	public String getConnectionId();
	public String getServerName();
	public String getServerId();
	public String getSchemaName();
	public String getUserName();
	public String getPassword();
    public Databases getServerType();

}
