package io.cyg.trading.client.core;

/**
 * User object has is defined by the Server, allowing responses to be properly parsed
 */
public class User
{
    private long id;
    private String username;
    private String url;
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
