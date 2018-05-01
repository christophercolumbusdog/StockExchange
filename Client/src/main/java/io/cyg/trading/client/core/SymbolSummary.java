package io.cyg.trading.client.core;

/**
 * Corresponding to the SymbolSummary on the server, allows the client
 * to parse the response of the server into this summary object
 */
public class SymbolSummary
{
    private String symbol;
    private double bidPrice;
    private double askPrice;
    private double price;
    private double bidAskSpread;


    public SymbolSummary(String symbol, double bidPrice, double askPrice) {
        this.symbol = symbol;
        this.bidPrice = bidPrice;
        this.askPrice = askPrice;
    }

    public SymbolSummary() {

    }

    public SymbolSummary calculateAndSetExtras() {
        bidAskSpread = Math.abs(bidPrice - askPrice);
        price = (bidPrice + askPrice) / 2.0;
        return this;
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

    public void setPrice(double price) {
        this.price = price;
    }

    public void setBidAskSpread(double bidAskSpread) {
        this.bidAskSpread = bidAskSpread;
    }

    public double getPrice() {
        return price;
    }

    public double getBidAskSpread() {
        return bidAskSpread;
    }
}

