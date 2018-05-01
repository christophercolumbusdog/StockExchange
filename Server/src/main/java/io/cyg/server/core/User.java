package io.cyg.server.core;

import javax.persistence.*;

/**
 * User class to model a user of the exchange
 */
@Entity
@Table(name = "users")
@NamedQueries({
        @NamedQuery(
                name = "io.cyg.server.core.User.findAll",
                query = "SELECT u FROM User u"
        ),
        @NamedQuery(
                name = "io.cyg.server.core.User.findById",
                query = "SELECT u FROM User u WHERE u.id = :id"
        ),
        @NamedQuery(
                name = "io.cyg.server.core.User.findByUsername",
                query = "SELECT u FROM User u WHERE u.username = :username"
        )
})
public class User
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "username", nullable = false)
    private String username;

    @Column(name = "url", nullable = false)
    private String url;

    @Column(name = "funds", nullable = false)
    private double funds;

    public long getId() {
        return id;
    }

    public User setId(long id) {
        this.id = id;
        return this;
    }

    public String getUsername() {
        return username;
    }

    public User setUsername(String username) {
        this.username = username;
        return this;
    }

    public String getUrl() {
        return url;
    }

    public User setUrl(String url) {
        this.url = url;
        return this;
    }

    public double getFunds() {
        return funds;
    }

    public User setFunds(double funds) {
        this.funds = funds;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;

        if (id != user.id) return false;
        if (Double.compare(user.funds, funds) != 0) return false;
        if (!username.equals(user.username)) return false;
        return url.equals(user.url);
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = (int) (id ^ (id >>> 32));
        result = 31 * result + username.hashCode();
        result = 31 * result + url.hashCode();
        temp = Double.doubleToLongBits(funds);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
}
