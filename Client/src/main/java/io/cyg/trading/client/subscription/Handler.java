package io.cyg.trading.client.subscription;

import io.cyg.trading.client.core.Transaction;

/**
 * Interface to allow for subscription to server updates to be handled by a handler
 */
public interface Handler
{
    /**
     * Entry point to handle transaction updates that are pushed from the server
     * @param transaction Transaction coming from the server
     */
    public void handleTransactionEvent(Transaction transaction);
}
