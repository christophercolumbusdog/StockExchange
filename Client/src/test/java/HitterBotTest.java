import io.cyg.trading.client.bots.HitterBot;
import io.cyg.trading.client.connection.ExchangeConnector;
import io.cyg.trading.client.core.SymbolSummary;
import io.cyg.trading.client.core.Transaction;
import io.cyg.trading.client.core.User;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.*;

public class HitterBotTest
{
    private HitterBot victim;
    private ExchangeConnector connector;
    private User user;

    private SymbolSummary summary1;
    private SymbolSummary summary2;
    private SymbolSummary summary3;
    private SymbolSummary summary4;
    private SymbolSummary summary5;

    @Before
    public void setup() throws Exception {
        connector = mock(ExchangeConnector.class);
        user = mock(User.class);

        when(connector.registerNewUser(any(User.class))).thenReturn(user);

        when(user.getId()).thenReturn(123456L);

        victim = new HitterBot(connector, "TST", user);
    }

    private void setupAskScenario() throws Exception {
        summary1 = new SymbolSummary("TST", 10.0, 20.0).calculateAndSetExtras();
        summary2 = new SymbolSummary("TST", 9.0, 19.0).calculateAndSetExtras();
        summary3 = new SymbolSummary("TST", 8.0, 18.0).calculateAndSetExtras();
        summary4 = new SymbolSummary("TST", 7.0, 17.0).calculateAndSetExtras();
        summary5 = new SymbolSummary("TST", 6.0, 16.0).calculateAndSetExtras();

        when(connector.getSymbolInfo("TST")).thenReturn(summary1).thenReturn(summary2).thenReturn(summary3)
                .thenReturn(summary4).thenReturn(summary5);
    }

    private void setupBidScenario() throws Exception {
        summary1 = new SymbolSummary("TST", 10.0, 20.0).calculateAndSetExtras();
        summary2 = new SymbolSummary("TST", 11.0, 21.0).calculateAndSetExtras();
        summary3 = new SymbolSummary("TST", 12.0, 22.0).calculateAndSetExtras();
        summary4 = new SymbolSummary("TST", 13.0, 23.0).calculateAndSetExtras();
        summary5 = new SymbolSummary("TST", 14.0, 24.0).calculateAndSetExtras();

        when(connector.getSymbolInfo("TST")).thenReturn(summary1).thenReturn(summary2).thenReturn(summary3)
                .thenReturn(summary4).thenReturn(summary5);
    }

    @Test
    public void testWaitForThree() throws Exception {
        setupBidScenario();

        victim.digestNewSummary();
        victim.digestNewSummary();
        victim.digestNewSummary();

        verify(connector, times(0)).sendImmediateTransaction(any(Transaction.class));
    }

    @Test
    public void testFirstBid() throws Exception {
        setupBidScenario();

        for (int i = 0; i < 4; i++) {
            victim.digestNewSummary();
        }

        verify(connector, times(1)).sendImmediateTransaction(
                new Transaction("TST", 23.00, 10, 'B', 123456));
    }

    @Test
    public void testFirstAsk() throws Exception {
        setupAskScenario();

        for (int i = 0; i < 4; i++) {
            victim.digestNewSummary();
        }

        verify(connector, times(1)).sendImmediateTransaction(
                new Transaction("TST", 7.00, 10, 'A', 123456));
    }
}
