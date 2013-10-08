package com.frs.alto.jdbc;

import com.frs.alto.core.DefaultDatabaseConnectionMetaData;
import com.frs.alto.core.DefaultTenantMetaData;
import com.frs.alto.core.TenantMetaData;
import com.frs.alto.util.TenantUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.UUID;

@Test
public class MultiTenantJdbcDataSourceTest {

    public static final String DB_ID = "test";
    public static final String DB_USER = "alto";
    public static final String DB_PASS = "manager";


	@Test
	public void testMultiTenantDatabaseAccess() throws Exception {

        DataSource ds = new MultiTenantDataSource(DB_ID);

		TenantMetaData tenant1 = createTestTenant("tenant1");
		TenantMetaData tenant2 = createTestTenant("tenant2");
		TenantMetaData tenant3 = createTestTenant("tenant3");

		createTestTable(ds, tenant1);
		createTestTable(ds, tenant2);
		createTestTable(ds, tenant3);

		insertTestData(ds, tenant1);
		insertTestData(ds, tenant2);
		insertTestData(ds, tenant3);

		assertConsistency(ds, tenant1);
		assertConsistency(ds, tenant2);
		assertConsistency(ds, tenant3);

		dropTestTable(ds, tenant1);
		dropTestTable(ds, tenant2);
		dropTestTable(ds, tenant3);

	}

	protected TenantMetaData createTestTenant(String tenantId) throws Exception {

		DefaultTenantMetaData tenant = new DefaultTenantMetaData();
		tenant.setTenantIdentifier(tenantId);
		tenant.setTenantName(tenantId);

		DefaultDatabaseConnectionMetaData db = new DefaultDatabaseConnectionMetaData();
		db.setConnectionId(DB_ID);
		db.setUserName(DB_USER);
		db.setPassword(DB_PASS);
		db.setSchemaName(tenantId);
		db.setServerId("localhost");
		db.setServerName("localhost");
		tenant.addDatabaseMetaData(db);
		return tenant;
	}

	protected void createTestTable(DataSource ds, TenantMetaData tenant) throws Exception {

		TenantUtils.setThreadHost(tenant);

		String ddl = "create table tenant_test (id char(36) not null, test_value varchar(128) null)";

		Connection conn = ds.getConnection();
		PreparedStatement ps = conn.prepareStatement(ddl);
		ps.execute();
		ps.close();
		conn.close();

	}

	protected void dropTestTable(DataSource ds, TenantMetaData tenant) throws Exception {

		TenantUtils.setThreadHost(tenant);

		String ddl = "drop table tenant_test";

		Connection conn = ds.getConnection();
		PreparedStatement ps = conn.prepareStatement(ddl);
		ps.execute();
		ps.close();
		conn.close();

	}

	protected void assertConsistency(DataSource ds, TenantMetaData tenant) throws Exception {

		TenantUtils.setThreadHost(tenant);

		String sql = "select test_value from tenant_test";

		Connection conn = ds.getConnection();
		PreparedStatement ps = conn.prepareStatement(sql);
		ResultSet rs = ps.executeQuery();
		while (rs.next()) {
			Assert.assertEquals(rs.getString(1), tenant.getTenantIdentifier());
		}
		rs.close();
		ps.close();
		conn.close();

	}

	protected void insertTestData(DataSource ds, TenantMetaData tenant) throws Exception {

		TenantUtils.setThreadHost(tenant);

		String ddl = "insert into tenant_test (id, test_value) values (?, ?)";

		Connection conn = ds.getConnection();
		PreparedStatement ps = conn.prepareStatement(ddl);
		ps.setString(1, UUID.randomUUID().toString());
		ps.setString(2, tenant.getTenantIdentifier());
		ps.execute();
		ps.close();
		conn.close();

	}


}
