package com.frs.alto.jdbc;

import org.apache.commons.pool.impl.GenericObjectPool;

import java.sql.Connection;

/**
 * Minimalist JDBC connection pool.
 */
public class ConnectionPool extends GenericObjectPool {

    /**
     * Constructor.
     * @param hostname  The host name of the database server. Not the JDBC URL.
     * @param username  The username with which to create connections.
     * @param password  The user's password.
     * @param maxActive The maximum number of active connections in the pool.
     * @param maxIdle   The maximum number of in-active connections in the pool.
     * @param maxWait   The maximum time, in millis, to wait for a login.
     */
    public ConnectionPool(Databases dbProfile, String hostname, String username, String password,
                          int maxActive, int maxIdle, long maxWait) {

        super();
        setFactory(new ConnectionFactory(dbProfile, hostname, username, password));
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