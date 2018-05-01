package io.cyg.trading.client.connection;

/**
 * Configuration items needed to connect to the server's Exchange
 */
public class ConnectionConfiguration {
    private String username;
    private String connectionURL;

    public ConnectionConfiguration(String username, String connectionURL) {
        this.username = username;
        this.connectionURL = connectionURL;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getConnectionURL() {
        return connectionURL;
    }

    public void setConnectionURL(String connectionURL) {
        this.connectionURL = connectionURL;
    }
}
