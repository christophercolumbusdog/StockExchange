import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cyg.server.core.SymbolSummary;
import io.cyg.server.core.Transaction;
import io.cyg.server.db.TransactionDAO;
import io.cyg.server.db.UserDAO;
import io.cyg.server.resources.TransactionResource;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.testing.junit.ResourceTestRule;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.junit.MockitoJUnitRunner;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class TransactionResourceTest
{
    private static final TransactionDAO TRANSACTION_DAO = mock(TransactionDAO.class);
    private static final UserDAO USER_DAO = mock(UserDAO.class);
    private static final ObjectMapper MAPPER = Jackson.newObjectMapper();

    @ClassRule
    public static final ResourceTestRule resources = ResourceTestRule.builder()
            .addResource(new TransactionResource(TRANSACTION_DAO, USER_DAO))
            .build();
    @Captor
    private ArgumentCaptor<Transaction> transactionCaptor;

    private final SymbolSummary symbolSummary = new SymbolSummary("CYG", 12.12, 21.21);

    private final Transaction transaction1 = new Transaction("CYG", 12.12, 12,
            'B', 1);

    private final Transaction transaction2 = new Transaction("CYG", 21.21, 21,
            'A', 2);

    private final Transaction inBetweenTransaction = new Transaction("CYG", 20.00, 21,
            'A', 3);

    private final Transaction completableTransaction = new Transaction("CYG", 21.22, 20,
            'B', 4);

    private final Transaction partialTransaction = new Transaction("CYG", 21.22, 40,
            'B', 5);

    private final List<Transaction> transactionList = new ArrayList<>();
    private final List<Transaction> oneBidLeft = new ArrayList<>();

    @Before
    public void setup() {
        transactionList.clear();
        oneBidLeft.clear();

        transactionList.add(transaction1);
        transactionList.add(transaction2);

        oneBidLeft.add(transaction1);

        when(TRANSACTION_DAO.findAll()).thenReturn(transactionList).thenReturn(oneBidLeft);
        when(TRANSACTION_DAO.insert(any(Transaction.class))).thenReturn(transaction1);
        when(TRANSACTION_DAO.getCurrentPriceForSymbol(any(String.class))).thenReturn(symbolSummary);
    }

    @After
    public void tearDown() {
        reset(TRANSACTION_DAO);
        reset(USER_DAO);
    }

    @Test
    public void testGetAllTransactions() throws JsonProcessingException {
        assertThat(MAPPER.writeValueAsString(resources.target("/transactions").request().get(List.class)))
                .isEqualTo(MAPPER.writeValueAsString(transactionList));
        verify(TRANSACTION_DAO).findAll();
    }

    @Test
    public void testGetSymbolTransactions() throws JsonProcessingException {
        assertThat(MAPPER.writeValueAsString(resources.target("/transactions/CYG").request().get(List.class)))
                .isEqualTo(MAPPER.writeValueAsString(transactionList));
        verify(TRANSACTION_DAO).findAll();

        assertThat(MAPPER.writeValueAsString(resources.target("/transactions/TST").request().get(List.class)))
                .isEqualTo(MAPPER.writeValueAsString(new ArrayList<>()));
    }

    @Test
    public void testPostTransaction() throws JsonProcessingException {
        Response res = resources.target("/transactions").request(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity(inBetweenTransaction, MediaType.APPLICATION_JSON_TYPE));

        assertThat(res.getStatusInfo()).isEqualTo(Response.Status.OK);
        verify(TRANSACTION_DAO).insert(transactionCaptor.capture());
        assertThat(transactionCaptor.getValue()).isEqualTo(inBetweenTransaction);
    }

    @Test
    public void testPostCompletableBidTransaction() {
        Response res = resources.target("/transactions").request(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity(completableTransaction, MediaType.APPLICATION_JSON_TYPE));

        assertThat(res.getStatusInfo()).isEqualTo(Response.Status.OK);
        verify(TRANSACTION_DAO).update(transactionCaptor.capture());
        assertThat(transactionCaptor.getValue().getQuantity()).isEqualTo(1);

        Transaction returned = res.readEntity(Transaction.class);
        assertThat(returned.getQuantity()).isEqualTo(0);

        verify(USER_DAO, times(2)).updateUserFunds(any(int.class), any(double.class));
    }

    @Test
    public void testPostPartialBidTransaction() {
        Response res = resources.target("/transactions").request(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity(partialTransaction, MediaType.APPLICATION_JSON_TYPE));

        assertThat(res.getStatusInfo()).isEqualTo(Response.Status.OK);
        verify(TRANSACTION_DAO, times(1)).delete(transactionCaptor.capture());
        assertThat(transactionCaptor.getValue().getQuantity()).isEqualTo(21);

        Transaction returned = res.readEntity(Transaction.class);

        assertThat(returned.getQuantity()).isEqualTo(19);
        verify(USER_DAO, times(2)).updateUserFunds(any(int.class), any(double.class));
    }

    @Test
    public void testSymbolSummary() throws JsonProcessingException {
        assertThat(MAPPER.writeValueAsString(resources.target("/transactions/summary/CYG")
                .request()
                .get(SymbolSummary.class)))
                .isEqualTo(MAPPER.writeValueAsString(symbolSummary));

        verify(TRANSACTION_DAO).getCurrentPriceForSymbol("CYG");
    }

    @Test
    public void testImmediateTradeSuccess() {
        Response result = resources.target("/transactions/immediate")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity(completableTransaction, MediaType.APPLICATION_JSON_TYPE));

        boolean success = result.readEntity(boolean.class);
        assertThat(success).isTrue();
        verify(TRANSACTION_DAO).update(transaction2);
        verify(USER_DAO, times(2)).updateUserFunds(any(int.class), any(double.class));
    }

    @Test
    public void testImmediateTradeFailure() {
        Response result = resources.target("/transactions/immediate")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity(partialTransaction, MediaType.APPLICATION_JSON_TYPE));

        boolean success = result.readEntity(boolean.class);
        assertThat(success).isFalse();
        verify(TRANSACTION_DAO, times(0)).update(transaction2);
        verify(TRANSACTION_DAO, times(0)).delete(transaction2);
        verify(USER_DAO, times(0)).updateUserFunds(any(int.class), any(double.class));
    }

    @Test
    public void testGetAllSymbols() {
        Transaction extra1 = new Transaction("HIG", 12.99, 50, 'B', 11);
        Transaction extra2 = new Transaction("GIH", 12.99, 50, 'B', 12);

        transactionList.add(extra1);
        transactionList.add(extra2);

        List response = resources.target("/transactions/symbols")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(List.class);

        assertThat(response.size()).isEqualTo(3);
    }
}
