import com.fasterxml.jackson.databind.ObjectMapper;
import io.cyg.trading.client.core.Transaction;
import io.cyg.trading.client.core.User;
import io.cyg.trading.client.subscription.ExampleHandler;
import io.cyg.trading.client.subscription.Handler;
import io.cyg.trading.client.subscription.SubscriptionService;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class SubscriptionServiceTest
{
    private final static ObjectMapper MAPPER = new ObjectMapper();
    private volatile Transaction result;

    @Test
    public void testSubscriptionReceiveMessage() throws IOException, InterruptedException {
        SubscriptionService subscriptionService = new SubscriptionService();
        Thread subscriptionThread = new Thread(subscriptionService, "subService");
        Transaction sentTransaction = new Transaction("TST", 12.95, 15, 'B', 16);
        User user = new User().setUrl("127.0.0.1").setId(15);
        Handler handler = new Handler()
        {
            public void handleTransactionEvent(Transaction transaction) {
                result = transaction;
            }
        };

        subscriptionService.subscribeUser(user);
        subscriptionService.attachHandler(handler);
        subscriptionThread.start();

        Thread.sleep(500);

        Socket socket = new Socket(user.getUrl(), (int)user.getId() + 1024);
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        out.print(MAPPER.writeValueAsString(sentTransaction));
        out.flush();
        socket.close();

        Thread.sleep(100);

        Assert.assertEquals(sentTransaction, result);
    }
}
