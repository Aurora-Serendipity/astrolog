package com.astrolog.config;

public class DBConfig {
    private String driverClass;
    private String jdbcUrl;
    private String username;
    private String password;
    private int maxConnections = 10;
    private int minConnections = 2;
    private int initialConnections = 5;

    public DBConfig() {}

    public DBConfig(String driverClass, String jdbcUrl, String username, String password,
                    int maxConnections, int minConnections, int initialConnections) {
        this.driverClass = driverClass;
        this.jdbcUrl = jdbcUrl;
        this.username = username;
        this.password = password;
        this.maxConnections = maxConnections;
        this.minConnections = minConnections;
        this.initialConnections = initialConnections;
    }

    public String getDriverClass() { return driverClass; }
    public void setDriverClass(String driverClass) { this.driverClass = driverClass; }

    public String getJdbcUrl() { return jdbcUrl; }
    public void setJdbcUrl(String jdbcUrl) { this.jdbcUrl = jdbcUrl; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public int getMaxConnections() { return maxConnections; }
    public void setMaxConnections(int maxConnections) { this.maxConnections = maxConnections; }

    public int getMinConnections() { return minConnections; }
    public void setMinConnections(int minConnections) { this.minConnections = minConnections; }

    public int getInitialConnections() { return initialConnections; }
    public void setInitialConnections(int initialConnections) { this.initialConnections = initialConnections; }

    @Override
    public String toString() {
        return "DBConfig{jdbcUrl='" + jdbcUrl + "', maxConnections=" + maxConnections + "}";
    }
}
