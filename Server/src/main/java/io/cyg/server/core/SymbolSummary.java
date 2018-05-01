package io.cyg.server.core;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Symbol summary class that is used to get information such at the symbol being traded,
 * the current highest bid price, and the current lowest ask price
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SymbolSummary
{
    private String symbol;
    private double bidPrice;
    private double askPrice;

    /**
     * All args constructor for a symbol summary
     * @param symbol Symbol to be summarized
     * @param bidPrice Highest bid price
     * @param askPrice Lowest ask price
     */
    public SymbolSummary(String symbol, double bidPrice, double askPrice) {
        this.symbol = symbol;
        this.bidPrice = bidPrice;
        this.askPrice = askPrice;
    }

    /**
     * No args constructor for JSON purposes
     */
    public SymbolSummary() {

    }

    public double getBidPrice() {
        return bidPrice;
    }

    public void setBidPrice(double bidPrice) {
        this.bidPrice = bidPrice;
    }

    public double getAskPrice() {
        return askPrice;
    }

    public void setAskPrice(double askPrice) {
        this.askPrice = askPrice;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public double getBidAskSpread() {
        return askPrice - bidPrice;
    }

    public double getPrice() {
        return ((askPrice+bidPrice)/2);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SymbolSummary summary = (SymbolSummary) o;

        if (Double.compare(summary.bidPrice, bidPrice) != 0) return false;
        if (Double.compare(summary.askPrice, askPrice) != 0) return false;
        return symbol.equals(summary.symbol);
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = symbol.hashCode();
        temp = Double.doubleToLongBits(bidPrice);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(askPrice);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
}
