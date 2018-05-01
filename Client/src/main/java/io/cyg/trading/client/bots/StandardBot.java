package io.cyg.trading.client.bots;

import io.cyg.trading.client.connection.ExchangeConnector;
import io.cyg.trading.client.core.SymbolSummary;
import io.cyg.trading.client.core.Transaction;
import io.cyg.trading.client.core.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Bot implementation that will do simple bid and asks on the exchange based on the price
 */
public class StandardBot implements Bot
{
    private ExchangeConnector connector;
    private String tradingSymbol;
    private List<SymbolSummary> pastSummaries;
    private volatile boolean exit = false;
    private User user;

    /**
     * Creates a new bot with all dependencies injected
     * @param exchangeConnector Connector that can interface with the server
     * @param tradingSymbol Symbol to be trading
     * @param user User associated with the bot
     */
    public StandardBot(ExchangeConnector exchangeConnector, String tradingSymbol, User user) {
        this.connector = exchangeConnector;
        this.tradingSymbol = tradingSymbol;
        this.pastSummaries = new ArrayList<SymbolSummary>();
        this.user = exchangeConnector.registerNewUser(user);
        exchangeConnector.subscribeUserToAll(this.user);
    }

    /**
     * Method used to start a standard bot as a separate thread
     * @param bot Bot that has been previously initialized
     * @param threadID Thread ID to give the new thread
     * @return Thread object corresponding to the created thread
     */
    public static Thread startBotThread(StandardBot bot, String threadID) {
        Thread thread = new Thread(bot, threadID);
        thread.start();
        return thread;
    }

    /**
     * Implements Runnable to allow the bot to start trading on a new thread
     */
    public void run() {
        try {
            while (!exit) {
                Thread.sleep(1000 + Math.round(Math.random()*100));

                SymbolSummary summary = connector.getSymbolInfo(tradingSymbol);

                if (pastSummaries.size() < 4) {
                    pastSummaries.add(summary);
                } else {
                    pastSummaries.remove(0);
                    pastSummaries.add(summary);
                }

                double priceTrend = 0;
                double bidTrend = 0;
                double askTrend = 0;
                for (int i = pastSummaries.size() - 1; i >= 1; i--) {
                    priceTrend += pastSummaries.get(i).getPrice() - pastSummaries.get(i-1).getPrice();
                    bidTrend += pastSummaries.get(i).getBidPrice() - pastSummaries.get(i-1).getBidPrice();
                    askTrend += pastSummaries.get(i).getAskPrice() - pastSummaries.get(i-1).getAskPrice();
                }

                double bidPriceTarget;
                double askPriceTarget;

                double decision = Math.random();

                if ((priceTrend >= 0 && decision > 0.3) || (priceTrend < 0 && decision < 0.3)) { // Price is going up on average!!!
                    bidPriceTarget = summary.getBidPrice() + (Math.random()*(summary.getPrice()/50));
                    askPriceTarget = summary.getAskPrice() + (Math.random()*(summary.getPrice()/50));
                } else {
                    bidPriceTarget = summary.getBidPrice() - (Math.random()*(summary.getPrice()/50));
                    askPriceTarget = summary.getAskPrice() - (Math.random()*(summary.getPrice()/50));
                }

                connector.sendTransaction(new Transaction(tradingSymbol,
                        bidPriceTarget,
                        (int) (20 + Math.round(Math.random() * 10)),
                        'B',
                        (int)user.getId()));
                connector.sendTransaction(new Transaction(tradingSymbol,
                        askPriceTarget,
                        (int) (20 + Math.round(Math.random() * 10)),
                        'A',
                        (int)user.getId()));
            }
        } catch (Exception e) {
            System.out.println("Exception occurred... " + e.getMessage());
        }
    }

    /**
     * Stops the thread by setting the exit value to break the infinite loop
     */
    public void stop() {
        exit = true;
    }

    /**
     * Gets the user associated with the bot
     * @return User associated with the bot
     */
    public User getUser() {
        return user;
    }
}
