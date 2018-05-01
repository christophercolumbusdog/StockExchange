package io.cyg.trading.client.connection;

import io.cyg.trading.client.core.SymbolSummary;
import io.cyg.trading.client.core.Transaction;
import io.cyg.trading.client.core.User;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class ExchangeConnector {
    private ConnectionConfiguration configuration;
    private Client client;

    public ExchangeConnector(ConnectionConfiguration configuration) {
        this.configuration = configuration;
        this.client = ClientBuilder.newClient();
    }

    public ExchangeConnector(ConnectionConfiguration configuration, Client client) {
        this.configuration = configuration;
        this.client = client;
    }

    /**
     * Send a transaction to the server to make a trade
     * @param transaction Transaction for a trade
     * @return Response based on if the trade was successful
     */
    public Response sendTransaction(Transaction transaction) {
        Response res = client
                .target(configuration.getConnectionURL() + "/transactions")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(transaction, MediaType.APPLICATION_JSON));

        return res;
    }

    /**
     * Send an immediate transaction to the server to make a trade
     * @param transaction Transaction for an immediate trade
     * @return boolean based on if the trade was successfully completed
     */
    public boolean sendImmediateTransaction(Transaction transaction) {
        Response res = client
                .target(configuration.getConnectionURL() + "/transactions/immediate")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(transaction, MediaType.APPLICATION_JSON));

        return res.readEntity(boolean.class);
    }

    /**
     * Register a new user with the server. ID of the user must NOT be set.
     * @param user User to create on the server
     * @return The user that was created, complete with the new UserID
     */
    public User registerNewUser(User user) {
        Response res = client
                .target(configuration.getConnectionURL() + "/users")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(user, MediaType.APPLICATION_JSON));

        return res.readEntity(User.class);
    }

    /**
     * Subscribes the user to any transaction updates the server will push out.
     * @param user User to subscribe
     * @return A response from the server
     */
    public Response subscribeUserToAll(User user) {
        Response res = client
                .target(configuration.getConnectionURL() + "/users/subscribe")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(user, MediaType.APPLICATION_JSON));

        return res;
    }

    /**
     * Sends a "GET" request to the url, with a /symbol at the end corresponding
     * to the symbol in question
     * @param symbol Symbol to find the info for
     * @return Summary of the current prices for the symbol
     */
    public SymbolSummary getSymbolInfo(String symbol) throws Exception {
        SymbolSummary symbolSummary = client
                .target(configuration.getConnectionURL() + "/transactions/summary/" + symbol)
                .request(MediaType.APPLICATION_JSON)
                .get(SymbolSummary.class);

        if (symbolSummary != null) {
            return symbolSummary;
        } else {
            throw new Exception("Could not connect and find summary");
        }
    }
}
