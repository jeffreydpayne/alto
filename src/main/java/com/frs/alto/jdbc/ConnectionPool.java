package com.frs.alto.jdbc;

import org.apache.commons.pool.impl.GenericObjectPool;

import java.sql.Connection;

/**
 * Minimalist JDBC connection pool.
 */
public class ConnectionPool extends GenericObjectPool {

    private int maxActive = 5;
    private int maxIdle = 1;
    private long maxWait = 5000;

    /**
     * Constructor.
     * @param hostname  The host name of the database server. Not the JDBC URL.
     * @param username  The username with which to create connections.
     * @param password  The user's password.
     */
    public ConnectionPool(String hostname, String username, String password) {

        super();
        setFactory(new ConnectionFactory(hostname, username, password));
        setMaxActive(maxActive);
        setMaxIdle(maxIdle);
        setMaxWait(maxWait);
        setTestOnBorrow(true);
    }

    /**
     * Checks a Connection out of the pool, if any are available.
     *
     * @return              A JDBC Connection
     * @throws Exception
     */
    public Connection checkOut() throws Exception {

        Connection cnx = (Connection)borrowObject();
        PooledConnection pCnx = new PooledConnection(this, cnx);
        return ( pCnx );
    }

    /**
     * Checks in a previously checked out JDBC Connection.
     *
     * @param conn          A JDBC Connection
     * @throws Exception
     */
    public void checkIn(Connection conn) throws Exception {

        returnObject(conn);
    }

}