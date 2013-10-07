package com.frs.alto.jdbc;

import com.frs.alto.core.DatabaseConnectionMetaData;
import com.frs.alto.core.TenantMetaData;
import com.frs.alto.util.TenantUtils;
import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MultiTenantDataSource implements DataSource {

    private PrintWriter logWriter = new PrintWriter(System.out);
    private List<DataSource> dataSources = new ArrayList<DataSource>();
    private HashMap<String, DataSource> tenantMapping = new HashMap<String,DataSource>();

    /**
     * Constructor.
     * @param jdbcURL   A valid JDBC URL
     * @param username  A user with privileges to read and modify tenant schemas
     * @param password  The user's password
     * @throws SQLException
     */
    public MultiTenantDataSource(String jdbcURL, String username, String password) throws SQLException {

        addDataSource(jdbcURL, username, password);
    }

    /**
     * This method adds a DataSource to the set of DataSources managed.
     *
     * @param jdbcURL   A valid JDBC URL
     * @param username  A user with privileges to read and modify tenant schemas
     * @param password  The user's password
     * @return
     * @throws SQLException
     */
    public DataSource addDataSource(String jdbcURL, String username, String password) throws SQLException {

        MysqlDataSource ds = new MysqlDataSource();
        ds.setURL(jdbcURL);
        ds.setUser(username);
        ds.setPassword(password);
        ds.setLogWriter(logWriter);
        getCatalogs(ds);
        dataSources.add(ds);
        return (ds);
    }

    /**
     * This private method examines a DataSource for tenant schemas. They are then
     * mapped such that the correct DataSource can be found for a given tenant ID.
     *
     * @param dataSource    The DataSource containing tenant schemas.
     * @throws SQLException
     */
    private void getCatalogs(DataSource dataSource) throws SQLException {

        Connection cnx = null;
        ResultSet catalogRS = null;

        try {

            cnx = dataSource.getConnection();
            if ( cnx == null ) {
                throw new SQLException("Could not obtain a connection from DataSource.");
            }
            catalogRS = cnx.getMetaData().getCatalogs();
            while (catalogRS != null && catalogRS.next()) {
                String tenantID = catalogRS.getString(1);
                tenantMapping.put(tenantID, dataSource);

            }
        } catch (SQLException sqlErr) {

            throw new SQLException("Error fetching catalogs.", sqlErr);

        } finally {

            try {
                catalogRS.close();
            } catch (Exception rsErr) {
            }
            try {
                cnx.close();
            } catch (Exception cnxErr) {
            }
        }
    }

    /**
     * This method returns a Connection from a DataSource associated with
     * the current Tenant. The tenant having been previously set via
     * <code>TenantUtils.setThreadTenant()</code>.
     *
     * @return              A JDBC Connection object.
     * @throws SQLException If no tenant was set, no mapped DataSource was found
     *                      or no DataSource could be created from the tenant's DB
     *                      metadata.
     */
    @Override
    public Connection getConnection() throws SQLException {

        TenantMetaData tenantMeta = TenantUtils.getThreadTenant();
        if ( tenantMeta == null ) {
            throw new SQLException("No TenantMetaData found for thread.");
        }

        String tenantID = tenantMeta.getTenantIdentifier();

        if ( tenantMapping.containsKey(tenantID) ) {
            DataSource ds = tenantMapping.get(tenantID);
            Connection cnx = ds.getConnection();
            cnx.setCatalog(tenantID);
            return ( cnx );
        }

        DatabaseConnectionMetaData dbMeta = tenantMeta.getDefaultDatabaseMetaData();
        String jdbcURL = "jdbc:mysql://" + dbMeta.getServerName() + ":3306";
        DataSource ds = addDataSource(jdbcURL, dbMeta.getUserName(), dbMeta.getPassword());
        Connection cnx = ds.getConnection();
        cnx.setCatalog(tenantID);
        return ( cnx );
    }

    /**
     * This method is unsupported. Use <code>addDataSource()</code> and <code>getConnection()</code>
     * instead.
     *
     * @param username
     * @param password
     * @return
     * @throws UnsupportedOperationException Always.
     */
    @Override
    public Connection getConnection(String username, String password) throws SQLException {

        throw new UnsupportedOperationException("Unsupported method. Use addDataSource() + getConnection() instead.");
    }

    /**
     * This method gets the LogWriter (a PrintWriter) for the DataSource.
     *
     * @return  The PrintWriter
     * @throws SQLException
     */
    @Override
    public PrintWriter getLogWriter() throws SQLException {

        return logWriter;
    }

    /**
     * This method sets the LogWriter (a PrinterWriter) for the DataSource.
     * All managed DataSources will use the same PrintWriter.
     *
     * @param out A PrintWriter
     * @throws SQLException
     */
    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {

        logWriter = out;
        for ( DataSource dataSource : dataSources ) {
            dataSource.setLogWriter(out);
        }
    }

    /**
     * This method sets the login timeout for all managed DataSources.
     *
     * @param seconds       The number of seconds to wait for a login.
     * @throws SQLException
     */
    @Override
    public void setLoginTimeout(int seconds) throws SQLException {

        for ( DataSource dataSource : dataSources ) {
            dataSource.setLoginTimeout(seconds);
        }
    }

    /**
     * Returns the login timeout of the managed DataSources.
     *
     * @return  The login timout, in seconds, of the managed DataSources.
     * @throws SQLException
     */
    @Override
    public int getLoginTimeout() throws SQLException {

       if ( dataSources.size() == 0 ) {
           return 0;
       }

       return ( dataSources.get(0).getLoginTimeout() );
    }

    /**
     * Whatwhat?
     *
     * @param iface
     * @return
     * @throws SQLException
     */
    @Override
    public Object unwrap(Class iface) throws SQLException {

        if (iface == MultiTenantDataSource.class) {
            return (this);
        }

        throw new SQLException("Cannot unwrap this to an " + iface);
    }

    /**
     * This class is not a wrapper for a single DataSource, so this
     * will always return false.
     *
     * @param iface     The interface.
     * @return          false.
     * @throws SQLException
     */
    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {

        return (false);
    }

}
