package io.cyg.trading.client.connection;

import io.cyg.trading.client.core.Transaction;
import io.cyg.trading.client.core.User;

public class ExchangeInitializer
{

    public static void initializeExchange(String symbol, String connectionURL,
                                          String userURL, double startPrice, double offset, int quantity) {
        ConnectionConfiguration myConfig =
                new ConnectionConfiguration("initializer", connectionURL);
        ExchangeConnector myConnector = new ExchangeConnector(myConfig);

        User masterCyg = new User().setUrl(userURL).setFunds(0.0).setUsername("Master" + symbol);

        masterCyg = myConnector.registerNewUser(masterCyg);

        myConnector.sendTransaction(new Transaction(symbol, startPrice - offset, quantity,
                'B', (int)masterCyg.getId()));
        myConnector.sendTransaction(new Transaction(symbol, startPrice + offset, quantity,
                'A', (int)masterCyg.getId()));

        System.out.println(symbol + " has been initialized...");
    }
}
