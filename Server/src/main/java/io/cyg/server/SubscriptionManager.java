package io.cyg.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.cyg.server.core.Transaction;
import io.cyg.server.core.User;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.lifecycle.Managed;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Manages all the subscriptions of different users to server updates
 */
public class SubscriptionManager implements Runnable, Managed
{
    private List<User> subscribers;
    private final ObjectMapper MAPPER = Jackson.newObjectMapper();
    private BlockingQueue<Transaction> pendingUpdates;
    private volatile boolean running = true;
    private Thread executionThread;

    /**
     * Create the needed BlockingQueue that will allow for transactions from the server
     * processing to be sent to the subscribers
     */
    public SubscriptionManager() {
        subscribers = new ArrayList<>();
        pendingUpdates = new ArrayBlockingQueue<>(1024);
        executionThread = null;

        System.out.println("SUBSCRIPTION MANAGER INITIALIZED!");
    }

    /**
     * Add a new user to the list of subscribers
     * @param user User that wishes to subscribe
     */
    public void addSubscriber(User user) {
        subscribers.add(user);
    }

    /**
     * Add a completed transaction to the queue of items to be sent to the
     * clients
     * @param transaction Completed transaction
     */
    public void addCompletedTransaction(Transaction transaction) {
        pendingUpdates.add(transaction);
    }

    /**
     * Implements the Managed interface to allow the Dropwizard server to control
     * starting and ending the thread
     * @throws Exception If the thread cannot be started
     */
    @Override
    public void start() throws Exception {
        running = true;
        executionThread = new Thread(this, "subMan");
        executionThread.start();
        System.out.println("SUBSCRIPTION MANAGER THREAD STARTED!");
    }

    /**
     * Stops the currently running thread by switching out the condition
     */
    public void stop() {
        running = false;
    }

    /**
     * Implements runnable to allow the subscription manager to run in a thread
     */
    @Override
    public void run() {
        while (running) {
            try {
                Transaction completedTransaction = pendingUpdates.poll(100, TimeUnit.MINUTES);
                if (completedTransaction == null) {
                    continue;
                }

                subscribers.forEach(user -> {
                    try {
                        Socket socket = new Socket(user.getUrl(), (int)(user.getId() + 1024));
                        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                        out.print(MAPPER.writeValueAsString(completedTransaction));
                        out.flush();
                        System.out.println("SENT VALUE: " + MAPPER.writeValueAsString(completedTransaction));
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
