package com.frs.alto.core;

import com.frs.alto.jdbc.Databases;

public class DefaultDatabaseConnectionMetaData implements
		DatabaseConnectionMetaData {
	
	private String connectionId;
	private String serverName;
	private String serverId;
	private String schemaName;
	private String userName;
	private String password;
    private Databases serverType;

	@Override
	public String getConnectionId() {
		return connectionId;
	}

    @Override
	public String getServerName() {
		return serverName;
	}

	@Override
	public String getServerId() {
		return serverId;
	}

	@Override
	public String getSchemaName() {
		return schemaName;
	}

	@Override
	public String getUserName() {
		return userName;
	}

	@Override
	public String getPassword() {
		return password;
	}

    @Override
    public Databases getServerType() {
        return serverType;
    }

	public void setConnectionId(String connectionId) {
		this.connectionId = connectionId;
	}

	public void setServerName(String serverName) {
		this.serverName = serverName;
	}

	public void setServerId(String serverId) {
		this.serverId = serverId;
	}

	public void setSchemaName(String schemaName) {
		this.schemaName = schemaName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public void setPassword(String password) {
		this.password = password;
	}

    public void setServerType(Databases serverType) {
        this.serverType = serverType;
    }


}
