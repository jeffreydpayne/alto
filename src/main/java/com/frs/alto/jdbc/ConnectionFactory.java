package com.frs.alto.jdbc;

import org.apache.commons.pool.PoolableObjectFactory;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.util.Enumeration;

public class ConnectionFactory implements PoolableObjectFactory {

    private Databases dbProfile = null;
    private String hostname = null;
    private String username = null;
    private String password = null;

    /**
     * Constructor. Provide a database host name, user name and password. The host name
     * should be just that, and not a JDBC URL.
     *
     * @param hostname      The DNS name or address of the database server
     * @param username      A valid user for this database
     * @param password      The user's password.
     */
    public ConnectionFactory(Databases dbProfile, String hostname, String username, String password)  {

        this.dbProfile = dbProfile;
        this.hostname = hostname;
        this.username = username;
        this.password = password;
    }

    /**
     * This method is called when the Connection is activated.
     *
     * @param arg0
     * @throws Exception
     */
    public void activateObject(Object arg0) throws Exception {

        // no-op
    }

    /**
     * This method is called when the connection has been destroyed.
     *
     * @param obj
     * @throws Exception
     */
    public void destroyObject(Object obj) throws Exception {

        ((Connection)obj).close();
    }

    /**
     * This method is called by the pool to create an object. In this case,
     * a <code>java.sql.Connection</code>. We also check DriverManager to
     * see if the driver for the current database profile has been registered.
     *
     * @return
     * @throws Exception
     */
    public Object makeObject() throws Exception {

        String jdbcURL = String.format(dbProfile.getUrlPattern(), hostname, dbProfile.getDefaultPort());
        boolean isDriverRegistered = false;

        Enumeration<Driver> drivers = DriverManager.getDrivers();
        while ( drivers.hasMoreElements() ) {
            Driver driver = drivers.nextElement();
            if ( driver.getClass().getName().equals(dbProfile.getDriverClass()) ) {
                isDriverRegistered = true;
                break;
            }
        }

        if ( !isDriverRegistered ) {
            try {
                Class.forName(dbProfile.getDriverClass());
            } catch (ClassNotFoundException cnfErr) {
                cnfErr.printStackTrace();
            }
        }

        return DriverManager.getConnection(jdbcURL, username, password);
    }

    /**
     * Called when the object is being returned to the pool.
     *
     * @param conn
     * @throws Exception
     */
    public void passivateObject(Object conn) throws Exception {

        // no-op
    }

    /**
     * Called during checkout to give us a chance to validate the
     * object before it's returned to the caller.
     *
     * @param conn
     * @return
     */
    public boolean validateObject(Object conn) {
        try {
            Connection connection = (Connection)conn;
            if ( !connection.isClosed() ) {
                return ( true );
            }
        } catch (Exception validErr) {

        }
        return ( false );
    }

}