package com.frs.alto.jdbc;

/**
 * This Enum holds default information for various database types. It's meant
 * to act as a profile from which certain vendor specific database values
 * can be fetched.
 */
public enum Databases {

    MYSQL ( "com.mysql.jdbc.Driver", 3306, "jdbc:mysql://%s:%d" );

    private final String driverClass;
    private final int defaultPort;
    private final String urlPattern;

    /**
     * Constructor
     * @param driverClass
     * @param defaultPort
     * @param urlPattern
     */
    private Databases(String driverClass, int defaultPort, String urlPattern) {
        this.driverClass = driverClass;
        this.defaultPort = defaultPort;
        this.urlPattern = urlPattern;
    }

    /**
     * Get the class name of the driver used for this type of database.
     * @return
     */
    public String getDriverClass() {
        return ( driverClass );
    }

    /**
     * Get the default port this database listens on.
     * @return
     */
    public int getDefaultPort() {
        return ( defaultPort );
    }

    /**
     * Returns a String.format()-able value for creating a valid
     * URL for this database. For example, 'jdbc:mysql://%s:%d' for
     * the MYSQL type.
     *
     * @return  A JDBC URL template.
     */
    public String getUrlPattern() {
        return ( urlPattern );
    }
}
