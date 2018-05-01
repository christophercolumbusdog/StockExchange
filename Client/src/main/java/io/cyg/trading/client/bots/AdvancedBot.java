package io.cyg.trading.client.bots;

import io.cyg.trading.client.connection.ExchangeConnector;
import io.cyg.trading.client.core.Transaction;
import io.cyg.trading.client.core.User;
import io.cyg.trading.client.subscription.Handler;

import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class AdvancedBot implements Bot, Handler
{
    private volatile boolean running = true;
    private User user;
    private ExchangeConnector connector;
    private String tradingSymbol;
    private BlockingQueue<Transaction> receivedTransactions;
    private TreeMap<Double, Transaction> askBook;
    private TreeMap<Double, Transaction> bidBook;

    private List<Double> askHistory;
    private List<Double> bidHistory;

    public AdvancedBot(ExchangeConnector exchangeConnector, String tradingSymbol, User user) {
        this.connector = exchangeConnector;
        this.tradingSymbol = tradingSymbol;

        this.user = exchangeConnector.registerNewUser(user);
        exchangeConnector.subscribeUserToAll(this.user);

        receivedTransactions = new ArrayBlockingQueue<Transaction>(200);
    }
    public void stop() {
        running = false;
    }

    public User getUser() {
        return null;
    }

    public void run() {
        while(running) {
            try {
                Transaction incomingTransaction = receivedTransactions.poll(100, TimeUnit.MINUTES);

                if (incomingTransaction.getAction() == 'A') {
                    askBook.put(incomingTransaction.getPrice(), incomingTransaction);
                } else {
                    bidBook.put(incomingTransaction.getPrice(), incomingTransaction);
                }


            } catch (Exception ignored) {

            }
        }
    }

    public void handleTransactionEvent(Transaction transaction) {
        try {
            receivedTransactions.put(transaction);
        } catch (InterruptedException e) {
            System.out.println("Skipping transaction, queue put was interrupted...");
            e.printStackTrace();
        }
    }
}