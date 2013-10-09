package com.frs.alto.jdbc;

import org.apache.commons.pool.PoolableObjectFactory;

import java.sql.Connection;
import java.sql.DriverManager;

public class ConnectionFactory implements PoolableObjectFactory {

    private static final String MYSQL_DRIVER = "com.mysql.jdbc.Driver";
    private static final String MYSQL_URL_TEMPLATE = "jdbc:mysql://%s:3306";

    private String hostname = null;
    private String username = null;
    private String password = null;

    /**
     * Initialize the driver. Once.
     */
    static {
        try {
            Class.forName(MYSQL_DRIVER);
        } catch (ClassNotFoundException cnfErr) {
            cnfErr.printStackTrace();
        }
    }

    /**
     * Constructore. Provide a database host name, user name and password. The host name
     * should be just that, and not a JDBC URL.
     *
     * @param hostname      The DNS name or address of the database server
     * @param username      A valid user for this database
     * @param password      The user's password.
     */
    public ConnectionFactory(String hostname, String username, String password)  {

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
     * a <code>java.sql.Connection</code>.
     *
     * @return
     * @throws Exception
     */
    public Object makeObject() throws Exception {
        return DriverManager.getConnection(
                String.format(MYSQL_URL_TEMPLATE, hostname),
                username,
                password);
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