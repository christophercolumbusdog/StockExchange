package io.cyg.trading.client.bots;

import io.cyg.trading.client.connection.ExchangeConnector;
import io.cyg.trading.client.core.SymbolSummary;
import io.cyg.trading.client.core.Transaction;
import io.cyg.trading.client.core.User;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class HitterBot implements Bot
{
    private volatile boolean running = true;
    private ExchangeConnector connector;
    private String exchangeSymbol;
    private User user;

    private int tradeAttempts = 0;
    private final int tradeLimit = 20;
    private final int ownTradeThreshold = 10;

    private List<Double> smallWindowHistory;
    private List<Transaction> ownTrades;

    public HitterBot(ExchangeConnector exchangeConnector, String tradingSymbol, User user) {
        this.exchangeSymbol = tradingSymbol;
        this.connector = exchangeConnector;

        this.user = connector.registerNewUser(user);
        this.connector.subscribeUserToAll(this.user);

        smallWindowHistory = new ArrayList<>();
        ownTrades = new ArrayList<>();
    }

    public void stop() {
        running = false;
    }

    public User getUser() {
        return this.user;
    }

    public void run() {
        while (running) {
            try {
                Thread.sleep(1000 + Math.round(Math.random()*100));
                digestNewSummary();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void digestNewSummary() throws Exception {
        SymbolSummary summary = connector.getSymbolInfo(exchangeSymbol);

        if (smallWindowHistory.size() < 3) {
            smallWindowHistory.add(summary.getPrice());
            return;
        } else {
            smallWindowHistory.remove(0);
            smallWindowHistory.add(summary.getPrice());
        }

        double priceMovement = 0;
        priceMovement += smallWindowHistory.get(2) - smallWindowHistory.get(1);
        priceMovement += smallWindowHistory.get(1) - smallWindowHistory.get(0);

        if (priceMovement > 0) { // BUY BUY BUY
            assessBuyOptions(summary);
        } else if (priceMovement < 0) { // SELL SELL SELL
            assessSellOptions(summary);
        }
    }

    private void assessBuyOptions(SymbolSummary summary) {
        double targetBuyPrice = summary.getAskPrice();

        if (ownTrades.size() == 0) {
            makeTrade(targetBuyPrice, 'B', 10, null);

        } else { // I have completed transactions
            List<Transaction> completedSells = ownTrades.stream()
                    .filter(transaction -> transaction.getAction() == 'A')
                    .collect(Collectors.toList());

            if (completedSells.size() == 0) {
                if (ownTrades.size() < ownTradeThreshold) { //Within threshold to add more trades
                    makeTrade(targetBuyPrice, 'B', 10, null);
                }
            } else {
                for (Transaction transaction : completedSells) {
                    if (transaction.getPrice() > targetBuyPrice) {
                        makeTrade(targetBuyPrice, 'B', transaction.getQuantity(), transaction);
                        tradeAttempts = 0;
                        break;
                    } else if (tradeAttempts >= tradeLimit) {
                        makeTrade(targetBuyPrice, 'B', transaction.getQuantity(), transaction);
                        tradeAttempts = 0;
                    }
                }
                tradeAttempts++;
            }
        }
    }

    private void assessSellOptions(SymbolSummary summary) {
        double targetSellPrice = summary.getBidPrice();

        if (ownTrades.size() == 0) {
            makeTrade(targetSellPrice, 'A', 10, null);
        } else { // I have completed transactions
            List<Transaction> completedBuys = ownTrades.stream()
                    .filter(transaction -> transaction.getAction() == 'B')
                    .collect(Collectors.toList());

            if (completedBuys.size() == 0) {
                if (ownTrades.size() < ownTradeThreshold) { //Within threshold to add more trades
                    makeTrade(targetSellPrice, 'A', 10, null);
                }
            } else {
                for (Transaction transaction : completedBuys) {
                    if (transaction.getPrice() < targetSellPrice) {
                        makeTrade(targetSellPrice, 'A', transaction.getQuantity(), transaction);
                        tradeAttempts = 0;
                        break;
                    } else if (tradeAttempts >= tradeLimit) {
                        makeTrade(targetSellPrice, 'A', transaction.getQuantity(), transaction);
                        tradeAttempts = 0;
                    }
                }
                tradeAttempts++;
            }
        }
    }

    private void makeTrade(double price, char action, int quantity, Transaction old) {
        Transaction desiredTransaction = new Transaction(exchangeSymbol, price,
                quantity, action, (int)user.getId());

        if (connector.sendImmediateTransaction(desiredTransaction)) {
            if (old ==  null) {
                ownTrades.add(desiredTransaction);
            } else {
                ownTrades.remove(old);
            }
            System.out.println("Successful HIT on " + exchangeSymbol + " with action " + action);
        }
    }
}
