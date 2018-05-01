package io.cyg.server.db;

import io.cyg.server.core.User;
import io.dropwizard.hibernate.AbstractDAO;
import org.hibernate.SessionFactory;

import java.util.List;
import java.util.Optional;

/**
 * Database Access Object for accessing the User database.
 */
public class UserDAO extends AbstractDAO<User>
{
    /**
     * Default constructor needs to initialize the AbstractDAO for functionality
     * @param sessionFactory Session factory from the application config for the DB
     */
    public UserDAO(SessionFactory sessionFactory) {
        super(sessionFactory);
    }

    /**
     * Find a particular user by their user ID
     * @param id ID of the user to get
     * @return An optional user
     */
    public Optional<User> findById(Long id) {
        Optional<User> res = Optional.ofNullable(get(id));
        return res;
    }

    public User updateUserFunds(int id, double fundDelta) {
        long userID = (long) id;

        Optional<User> res = Optional.ofNullable(get(userID));

        if (res.isPresent()) {
            User user = res.get();
            user.setFunds(user.getFunds() + fundDelta);
            return persist(user);
        }
        return null;
    }

    /**
     * Finds a user by the specified username instead of by the ID
     * @return A user that matches the specified username
     */
    public User findByUsername() {
        return uniqueResult(namedQuery("io.cyg.server.core.User.findByUsername"));
    }

    /**
     * Creates a new user in the database
     * @param person User to insert into the database
     * @return The user that was inserted (in this case, the passed in user)
     */
    public User create(User person) {
        return persist(person);
    }

    /**
     * Finds all of the users that exist in the database
     * @return A list of all users in the database
     */
    public List<User> findAll() {
        return (List<User>) list(namedQuery("io.cyg.server.core.User.findAll"));
    }
}
