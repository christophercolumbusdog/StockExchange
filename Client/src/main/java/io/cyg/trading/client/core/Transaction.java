package io.cyg.trading.client.core;

/**
 * Transaction corresponding to the transaction defined on the server allowing
 * responses to be properly parsed into this object
 */
public class Transaction
{
    private long id;
    private String symbol;
    private int quantity;
    private double price;
    private char action;
    private int ownerID;

    /**
     * All args constructor for a transaction
     * @param symbol Symbol to be traded
     * @param price Price to execute the action at
     * @param quantity Number of "shares" to trade
     * @param action B = bid, A = ask
     * @param ownerID ID of the user making the transaction
     */
    public Transaction(String symbol, double price, int quantity, char action, int ownerID) {
        this.quantity = quantity;
        this.symbol = symbol;
        this.action = action;
        this.price = price;
        this.ownerID = ownerID;
    }

    /**
     * No args constructor for JSON purposes
     */
    public Transaction() {

    }

    public int getQuantity() {
        return quantity;
    }

    public String getSymbol() {
        return symbol;
    }

    public char getAction() {
        return action;
    }

    public double getPrice() {
        return price;
    }

    public long getId() {
        return id;
    }

    public int getOwnerID() {
        return ownerID;
    }

    public Transaction setId(long id) {
        this.id = id;
        return this;
    }

    public Transaction setSymbol(String symbol) {
        this.symbol = symbol;
        return this;
    }

    public Transaction setQuantity(int quantity) {
        this.quantity = quantity;
        return this;
    }

    public Transaction setPrice(double price) {
        this.price = price;
        return this;
    }

    public Transaction setAction(char action) {
        this.action = action;
        return this;
    }

    public Transaction setOwnerID(int ownerID) {
        this.ownerID = ownerID;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Transaction that = (Transaction) o;

        if (id != that.id) return false;
        if (quantity != that.quantity) return false;
        if (Double.compare(that.price, price) != 0) return false;
        if (action != that.action) return false;
        if (ownerID != that.ownerID) return false;
        return symbol.equals(that.symbol);
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = (int) (id ^ (id >>> 32));
        result = 31 * result + symbol.hashCode();
        result = 31 * result + quantity;
        temp = Double.doubleToLongBits(price);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (int) action;
        result = 31 * result + ownerID;
        return result;
    }
}
