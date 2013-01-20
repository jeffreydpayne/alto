package com.frs.alto.jdbc;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import com.frs.alto.core.DatabaseConnectionMetaData;
import com.frs.alto.core.TenantMetaData;
import com.frs.alto.util.TenantUtils;
import com.mchange.v2.c3p0.ComboPooledDataSource;

public class C3p0TenantSwitchingDataSource implements DataSource {
	
	private String connectionId = null;
	
	private Collection<ComboPooledDataSource> proxiedDatasources;
	
	private Map<String, ComboPooledDataSource> dataSourceMap = null;
	
	public Collection<ComboPooledDataSource> getProxiedDatasources() {
		return proxiedDatasources;
	}

	public void setProxiedDatasources(Collection<ComboPooledDataSource> proxiedDatasources) {
		this.proxiedDatasources = proxiedDatasources;
		initializeDataSourceMap();
	}
	
	protected String resolveDatasourceId(ComboPooledDataSource ds) {
		
		if (ds.getDescription() != null) {
			return ds.getDescription();
		}
		else {
			return null;
		}
	}
	
	protected void initializeDataSourceMap() {
		dataSourceMap = new HashMap<String, ComboPooledDataSource>();
		for (ComboPooledDataSource ds : proxiedDatasources) {
			String server = resolveDatasourceId(ds);
			dataSourceMap.put(server, ds);
		}
		
	}

	@Override
	public boolean isWrapperFor(Class<?> arg0) throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public <T> T unwrap(Class<T> arg0) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PrintWriter getLogWriter() throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getLoginTimeout() throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setLogWriter(PrintWriter arg0) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setLoginTimeout(int arg0) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Connection getConnection() throws SQLException {
		

		
		
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Connection getConnection(String username, String password)
			throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}
	
	protected DataSource resolveThreadDatasource() {
		
		TenantMetaData metaData = TenantUtils.getThreadTenant();
		DatabaseConnectionMetaData db = null;
		if (connectionId != null) {
			db = metaData.getDatabaseConnectionMetaData(connectionId);
		}
		else {
			db = metaData.getDefaultDatabaseMetaData();
		}
		
		return dataSourceMap.get(db.getServerId());
		
	}
	
	
}
