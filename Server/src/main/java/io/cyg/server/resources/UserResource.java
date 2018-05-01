package io.cyg.server.resources;

import io.cyg.server.SubscriptionManager;
import io.cyg.server.core.User;
import io.cyg.server.db.LockProvider;
import io.cyg.server.db.UserDAO;
import io.dropwizard.hibernate.UnitOfWork;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.concurrent.Semaphore;

/**
 * Resource for the "user" endpoint
 */
@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
public class UserResource
{
    private final UserDAO userDAO;
    private final SubscriptionManager manager;

    private static Semaphore lock = LockProvider.getLock();

    /**
     * Initialize the resource with an instantiated DAO
     *
     * This dependency injection makes testing easier
     * @param dao Transaction DAO that has been initialized
     */
    public UserResource(UserDAO dao, SubscriptionManager manager) {
        this.userDAO = dao;
        this.manager = manager;
    }

    /**
     * GET endpoint to get all the users in the database
     * @return A list of all users
     */
    @GET
    @UnitOfWork(value = "hibernate.db_connector")
    public List<User> getAllUsers() {
        try {
            lock.acquire();

            return userDAO.findAll();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        } finally {
            lock.release();
        }
    }

    /**
     * POST endpoint to put a new user into the database
     * @param user User to add to the database
     * @return The user that was added to the database
     */
    @POST
    @UnitOfWork(value = "hibernate.db_connector")
    public User createUser(User user) {
        try {
            lock.acquire();
            return userDAO.create(user);
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        } finally {
            lock.release();
        }
    }

    @POST
    @Path("/subscribe")
    public User subscribeToAllUpdates(User user) {
        try {
            lock.acquire();

            manager.addSubscriber(user);
            return user;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        } finally {
            lock.release();
        }
    }
}
