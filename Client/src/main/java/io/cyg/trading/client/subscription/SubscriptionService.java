package io.cyg.trading.client.subscription;

//https://docs.oracle.com/javase/tutorial/networking/sockets/clientServer.html

import io.cyg.trading.client.core.Transaction;
import io.cyg.trading.client.core.User;

import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class SubscriptionService implements Runnable
{
    private User subscriber = null;
    private Handler handler = null;
    private boolean running = true;

    /**
     * Add a subscriber (User) that is used to get connection information
     * @param user User to subscribe to updates
     */
    public void subscribeUser(User user) {
        subscriber = user;
    }

    /**
     * Attaches a handler to handle transactions updates
     * @param handler Class that implements the handler interface
     */
    public void attachHandler(Handler handler) {
        this.handler = handler;
    }

    /**
     * Listens for updates sent by the server on the LAN
     * @throws Exception If there is any error creating a server socket
     */
    private void listenForUpdates() throws Exception {
        if (subscriber == null) throw new Exception("What are you doing, the user is NULL");

        InetSocketAddress socketAddress =
                new InetSocketAddress(subscriber.getUrl(), (int)subscriber.getId() + 1024);
        ServerSocket serverSocket = new ServerSocket();
        serverSocket.setReuseAddress(true);
        serverSocket.bind(socketAddress);

        while (running) {
            Socket clientConnection = serverSocket.accept();
            Transaction message = ExchangeProtocol.processTransactionNotification(clientConnection.getInputStream());

            if (handler != null) {
                handler.handleTransactionEvent(message);
            } else {
                System.out.println("NO HANDLER ATTACHED TO HANDLE TRANSACTION UPDATES...");
            }
        }
    }

    /**
     * Implements runnable to make this code threadable
     */
    public void run() {
        try {
            listenForUpdates();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Stops the infinite loop and therefore stops execution of this thread
     */
    public void stop() {
        running = false;
    }
}
