import com.fasterxml.jackson.databind.ObjectMapper;
import io.cyg.server.SubscriptionManager;
import io.cyg.server.core.Transaction;
import io.cyg.server.core.User;
import io.dropwizard.jackson.Jackson;
import org.junit.Assert;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class SubscriptionManagerTest
{
    private static final ObjectMapper MAPPER = Jackson.newObjectMapper();

    @Test
    public void testSingleUserNotification() throws IOException {
        SubscriptionManager manager = new SubscriptionManager();
        Thread managerThread = new Thread(manager, "subMan");
        User user = new User().setUrl("127.0.0.1").setId(15);
        manager.addSubscriber(user);
        managerThread.start();

        InetAddress address = InetAddress.getByName(user.getUrl());
        ServerSocket serverSocket = new ServerSocket(1024+15, 50, address);

        Transaction transaction = new Transaction("CYG", 55.98, 22, 'B', 15);

        manager.addCompletedTransaction(transaction);
        Socket clientConnection = serverSocket.accept();

        BufferedReader inputReader = new BufferedReader(new InputStreamReader(clientConnection.getInputStream()));
        StringBuilder message = new StringBuilder();
        String inputLine;
        while((inputLine = inputReader.readLine()) != null) {
            message.append(inputLine);
        }
        Transaction result = MAPPER.readValue(message.toString(), Transaction.class);

        Assert.assertEquals(transaction, result);

        manager.stop();
    }
}
