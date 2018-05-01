import com.fasterxml.jackson.databind.ObjectMapper;
import io.cyg.server.core.SymbolSummary;
import io.cyg.server.core.Transaction;
import io.cyg.server.core.User;
import io.dropwizard.jackson.Jackson;
import org.junit.Test;

import static io.dropwizard.testing.FixtureHelpers.*;
import static org.assertj.core.api.Assertions.assertThat;

public class DeserializationTests
{
    private static final ObjectMapper MAPPER = Jackson.newObjectMapper();

    @Test
    public void deserializeSymbolSummaryFromJSON() throws Exception {
        final SymbolSummary summary = new SymbolSummary("CYG", 42.42, 68.68);

        assertThat(MAPPER.readValue(fixture("fixtures/symbolSummary.json"), SymbolSummary.class))
                .isEqualTo(summary);
    }

    @Test
    public void deserializeUserFromJSON() throws Exception {
        final User user = new User();
        user.setUsername("cygnus2022");
        user.setUrl("www.google.com");
        user.setFunds(1234.56);

        assertThat(MAPPER.readValue(fixture("fixtures/user.json"), User.class))
                .isEqualTo(user);
    }

    @Test
    public void deserializeTransactionFromJSON() throws Exception {
        final Transaction transaction = new Transaction("CYG", 54.32, 22,
                'A', 123456789);

        assertThat(MAPPER.readValue(fixture("fixtures/transaction.json"), Transaction.class))
                .isEqualTo(transaction);
    }
}
