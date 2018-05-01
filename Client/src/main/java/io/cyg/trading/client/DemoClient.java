package io.cyg.trading.client;

import io.cyg.trading.client.bots.BotContainer;
import io.cyg.trading.client.bots.HitterBot;
import io.cyg.trading.client.bots.StandardBot;
import io.cyg.trading.client.connection.ConnectionConfiguration;
import io.cyg.trading.client.connection.ExchangeConnector;
import io.cyg.trading.client.connection.ExchangeInitializer;
import io.cyg.trading.client.core.SymbolSummary;
import io.cyg.trading.client.subscription.ExampleHandler;

public class DemoClient
{
    public static void main(String[] args) throws Exception {
        ConnectionConfiguration myConfig =
                new ConnectionConfiguration("initializer", "http://localhost:8080");
        ExchangeConnector myConnector = new ExchangeConnector(myConfig);

        /*
        CREATE CONNECTOR FOR THE MAIN CLASS
         */
        ExchangeInitializer.initializeExchange("CYG", "http://localhost:8080",
                "127.0.0.1", 15.50, 0.50, 50);
        ExchangeInitializer.initializeExchange("TXT", "http://localhost:8080",
                "127.0.0.1", 650, 50, 50);

        /*
        CREATE BOT AND CONNECTOR FOR THE BOT
         */

        BotContainer cygBot1 = new BotContainer("CygOne", "http://localhost:8080",
                "127.0.0.1", 1000.0, "CYG", StandardBot.class, new ExampleHandler());
        BotContainer cygBot2 = new BotContainer("CygTwo", "http://localhost:8080",
                "127.0.0.1", 1000.0, "CYG", StandardBot.class, new ExampleHandler());
        BotContainer cygHitterBot = new BotContainer("CygHitter", "http://localhost:8080",
                "127.0.0.1", 2000.0, "CYG", HitterBot.class, new ExampleHandler());
        BotContainer txtBot = new BotContainer("Txt", "http://localhost:8080",
                "127.0.0.1", 5000.0, "TXT", StandardBot.class, new ExampleHandler());

        cygBot1.startThreads();
        cygBot2.startThreads();
        cygHitterBot.startThreads();
        txtBot.startThreads();

        /*
        START SIMULATION PRINTING
         */
        for (int i = 0; i < 200; i ++) {
            Thread.sleep(900);

            SymbolSummary summary = myConnector.getSymbolInfo("CYG");

            System.out.println("____________________________________________");
            System.out.println("Symbol: " + summary.getSymbol());
            System.out.println("Bid Price: " + summary.getBidPrice());
            System.out.println("Ask Price: " + summary.getAskPrice());
            System.out.println("Selling price: " + summary.getPrice());
        }

        cygBot1.stopThreads();
        cygBot2.stopThreads();
        cygHitterBot.stopThreads();
        txtBot.stopThreads();

        Thread.sleep(1000);
        System.out.println("Done simulation");
    }
}
