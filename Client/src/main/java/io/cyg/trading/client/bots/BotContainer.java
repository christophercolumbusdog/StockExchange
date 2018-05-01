package io.cyg.trading.client.bots;

import io.cyg.trading.client.connection.ConnectionConfiguration;
import io.cyg.trading.client.connection.ExchangeConnector;
import io.cyg.trading.client.core.User;
import io.cyg.trading.client.subscription.Handler;
import io.cyg.trading.client.subscription.SubscriptionService;

public class BotContainer
{
    private String username;
    private Bot bot;
    private SubscriptionService subscriptionService;
    private ConnectionConfiguration connectionConfiguration;
    private ExchangeConnector connector;
    private User user;
    private Handler handler;
    private Thread subscriptionThread;
    private Thread botThread;

    public BotContainer(String botName, String serverURL, String userURL, double startingFunds, String symbol,
                        Class<?> botType, Handler handler) {
        username = botName + "Bot";

        connectionConfiguration = new ConnectionConfiguration(username, serverURL);
        connector = new ExchangeConnector(connectionConfiguration);
        user = new User().setUrl(userURL).setFunds(startingFunds).setUsername(username);

        if (botType == StandardBot.class) {
            bot = new StandardBot(connector, symbol, user);
        } else if (botType == HitterBot.class) {
            bot = new HitterBot(connector, symbol, user);
        } else {
            // Create default bot
            bot = new StandardBot(connector, symbol, user);
        }

        this.handler = handler;

        subscriptionService = new SubscriptionService();
        subscriptionService.attachHandler(this.handler);
        subscriptionService.subscribeUser(bot.getUser());

        subscriptionThread = new Thread(subscriptionService, username + "SubscriptionThread");

        botThread = new Thread(bot, username + "Thread");
    }

    public void startThreads() {
        subscriptionThread.start();
        botThread.start();
    }

    public void stopThreads() {
        subscriptionService.stop();
        bot.stop();
    }
}
