import io.cyg.trading.client.connection.ConnectionConfiguration;
import io.cyg.trading.client.connection.ExchangeConnector;
import io.cyg.trading.client.core.SymbolSummary;
import io.cyg.trading.client.core.Transaction;
import io.cyg.trading.client.core.User;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import static org.mockito.Mockito.*;

public class ExchangeConnectorTest
{
    private static ConnectionConfiguration configuration;
    private static int ownerID;
    private static ExchangeConnector victim;
    private static Client client;

    @BeforeClass
    public static void setup() {
        configuration = new ConnectionConfiguration("tester", "http://localhost:8080");
        ownerID = -1;
        client = mock(Client.class);
        victim = new ExchangeConnector(configuration, client);
    }

    @Test
    public void testGetSymbolSummary() throws Exception {
        WebTarget target = mock(WebTarget.class);
        Invocation.Builder builder = mock (Invocation.Builder.class);
        SymbolSummary response = mock(SymbolSummary.class);

        when(client.target(any(String.class))).thenReturn(target);
        when(target.request(any(String.class))).thenReturn(builder);
        when(builder.get(any(Class.class))).thenReturn(response);

        SymbolSummary res = victim.getSymbolInfo("TST");

        Assert.assertEquals(res, response);
    }

    @Test
    public void testPostTransaction() {
        WebTarget target = mock(WebTarget.class);
        Invocation.Builder builder = mock (Invocation.Builder.class);
        Response response = mock(Response.class);

        when(client.target(any(String.class))).thenReturn(target);
        when(target.request(any(String.class))).thenReturn(builder);
        when(builder.post(any(Entity.class))).thenReturn(response);

        Response res = victim.sendTransaction(new Transaction("TST", 12.12, 60,
                'B', ownerID));

        Assert.assertEquals(res, response);
    }

    @Test
    public void testPostImmediateTransaction() {
        WebTarget target = mock(WebTarget.class);
        Invocation.Builder builder = mock (Invocation.Builder.class);
        Response response = mock(Response.class);

        when(client.target(any(String.class))).thenReturn(target);
        when(target.request(any(String.class))).thenReturn(builder);
        when(builder.post(any(Entity.class))).thenReturn(response);
        when(response.readEntity(boolean.class)).thenReturn(true);

        boolean res = victim.sendImmediateTransaction(new Transaction("TST", 12.12, 60,
                'B', ownerID));

        Assert.assertEquals(true, res);
    }

    @Test
    public void testRegisterUser() {
        WebTarget target = mock(WebTarget.class);
        Invocation.Builder builder = mock (Invocation.Builder.class);
        Response response = mock(Response.class);
        User user = new User().setUsername("Tester").setId(12).setFunds(1200).setUrl("127.0.0.1");

        when(client.target(any(String.class))).thenReturn(target);
        when(target.request(any(String.class))).thenReturn(builder);
        when(builder.post(any(Entity.class))).thenReturn(response);
        when(response.readEntity(User.class)).thenReturn(user);


        User res = victim.registerNewUser(user);

        Assert.assertEquals(res, user);
    }

    @Test
    public void testSubscribeUserToAll() {
        WebTarget target = mock(WebTarget.class);
        Invocation.Builder builder = mock (Invocation.Builder.class);
        Response response = mock(Response.class);
        User user = new User().setUsername("Tester").setId(12).setFunds(1200).setUrl("127.0.0.1");

        when(client.target(any(String.class))).thenReturn(target);
        when(target.request(any(String.class))).thenReturn(builder);
        when(builder.post(any(Entity.class))).thenReturn(response);

        Response res = victim.subscribeUserToAll(user);

        Assert.assertEquals(res, response);
    }
}
