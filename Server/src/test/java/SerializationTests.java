import com.fasterxml.jackson.databind.ObjectMapper;
import io.cyg.server.core.Transaction;
import io.cyg.server.core.User;
import io.dropwizard.jackson.Jackson;
import io.cyg.server.core.SymbolSummary;
import org.junit.Test;

import static io.dropwizard.testing.FixtureHelpers.*;
import static org.assertj.core.api.Assertions.assertThat;

public class SerializationTests
{
    private static final ObjectMapper MAPPER = Jackson.newObjectMapper();

    @Test
    public void serializeSymbolSummaryToJSON() throws Exception {
        final SymbolSummary summary = new SymbolSummary("CYG", 42.42, 68.68);

        final String expected = MAPPER.writeValueAsString(
                MAPPER.readValue(fixture("fixtures/symbolSummary.json"), SymbolSummary.class));

        assertThat(MAPPER.writeValueAsString(summary)).isEqualTo(expected);
    }

    @Test
    public void serializeUserToJSON() throws Exception {
        final User user = new User();
        user.setUsername("cygnus2022");
        user.setUrl("www.google.com");
        user.setFunds(1234.56);

        final String expected = MAPPER.writeValueAsString(
                MAPPER.readValue(fixture("fixtures/user.json"), User.class));

        assertThat(MAPPER.writeValueAsString(user)).isEqualTo(expected);
    }

    @Test
    public void serializeTransactionToJSON() throws Exception {
        final Transaction transaction = new Transaction("CYG", 54.32, 22,
                'A', 123456789);

        final String expected = MAPPER.writeValueAsString(
                MAPPER.readValue(fixture("fixtures/transaction.json"), Transaction.class));

        assertThat(MAPPER.writeValueAsString(transaction)).isEqualTo(expected);
    }
}
