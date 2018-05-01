package io.cyg.trading.client.subscription;

import io.cyg.trading.client.core.Transaction;

public class ExampleHandler implements Handler
{
    /**
     * Simple handler that will simply print out the transaction
     * @param transaction Transaction coming from the server
     */
    public void handleTransactionEvent(Transaction transaction) {
        System.out.println("FOUND A MESSAGE: Transaction for symbol " + transaction.getSymbol());
    }
}
