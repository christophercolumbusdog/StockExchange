import io.cyg.trading.client.bots.StandardBot;
import io.cyg.trading.client.connection.ExchangeConnector;
import io.cyg.trading.client.core.SymbolSummary;
import io.cyg.trading.client.core.Transaction;
import io.cyg.trading.client.core.User;
import org.junit.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class StandardBotTest
{


    @Test
    public void standardBotTest() throws Exception {
        ExchangeConnector mockConnector = mock(ExchangeConnector.class);
        User user = new User().setUrl("127.0.0.1").setId(15).setFunds(100).setUsername("Tester");
        when(mockConnector.getSymbolInfo(any(String.class)))
        .thenReturn(new SymbolSummary("TST", 15.00, 16.00))
        .thenReturn(new SymbolSummary("TST", 15.12, 16.12))
        .thenReturn(new SymbolSummary("TST", 15.16, 16.59))
        .thenReturn(new SymbolSummary("TST", 15.49, 16.99))
        .thenReturn(new SymbolSummary("TST", 15.20, 16.20));

        StandardBot bot = new StandardBot(mockConnector, "TST", user);
        StandardBot.startBotThread(bot, "TestBot");

        Thread.sleep(4000);
        bot.stop();

        verify(mockConnector, atLeast(3)).sendTransaction(any(Transaction.class));
    }
}
