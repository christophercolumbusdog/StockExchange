import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cyg.server.SubscriptionManager;
import io.cyg.server.core.User;
import io.cyg.server.db.UserDAO;
import io.cyg.server.resources.UserResource;
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
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class UserResourceTest
{
    private static final UserDAO dao = mock(UserDAO.class);
    private static final ObjectMapper MAPPER = Jackson.newObjectMapper();
    private static final SubscriptionManager MANAGER = mock(SubscriptionManager.class);

    @ClassRule
    public static final ResourceTestRule resources = ResourceTestRule.builder()
            .addResource(new UserResource(dao, MANAGER))
            .build();
    @Captor
    private ArgumentCaptor<User> userCaptor;

    private final User user1 = new User()
            .setUsername("cygnus2022")
            .setUrl("www.google.com")
            .setFunds(1234.56)
            .setId(123456);

    private final User user2 = new User()
            .setUsername("otherDude9")
            .setUrl("www.wolves.com")
            .setFunds(6543.21)
            .setId(654321);

    private final List<User> userList = new ArrayList<>();

    @Before
    public void setup() {
        userList.add(user1);
        userList.add(user2);
        when(dao.findAll()).thenReturn(userList);
        when(dao.findById(123456L)).thenReturn(Optional.of(user1)); //Will be used later when resource supports it
        when(dao.create(any(User.class))).thenReturn(user1);
    }

    @After
    public void tearDown() {
        reset(dao);
    }

    @Test
    public void testGetAllUsers() throws JsonProcessingException {
        assertThat(MAPPER.writeValueAsString(resources.target("/users").request().get(List.class)))
                .isEqualTo(MAPPER.writeValueAsString(userList));
        verify(dao).findAll();
    }

    @Test
    public void testPostUser() throws JsonProcessingException {
        Response res = resources.target("/users").request(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity(user1, MediaType.APPLICATION_JSON_TYPE));

        assertThat(res.getStatusInfo()).isEqualTo(Response.Status.OK);
        verify(dao).create(userCaptor.capture());
        assertThat(userCaptor.getValue()).isEqualTo(user1);
    }

    @Test
    public void testSubscribeToAllUpdates() {
        Response res = resources.target("/users/subscribe").request(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity(user1, MediaType.APPLICATION_JSON_TYPE));

        assertThat(res.getStatusInfo()).isEqualTo(Response.Status.OK);
        verify(MANAGER).addSubscriber(userCaptor.capture());
        assertThat(userCaptor.getValue()).isEqualTo(user1);
    }

}
