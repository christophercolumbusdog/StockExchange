import React from 'react';
import './App.css';

const axios = require('axios');

export class ExchangeInfo extends React.Component {
    constructor(props) {
        super(props);

        this.state = {summaries: {}, symbols: [], selection: "None", bidBook: [], askBook: []};
    }

    componentDidMount() {
        this.summaryUpdatesId = setInterval(this.pullSummaryFromServer, 1000);
        this.symbolUpdateId = setInterval(this.pullSymbolListFromServer, 20000);
        this.bookUpdateId = setInterval(this.pullBookInfoFromServer, 1500);

        this.pullSymbolListFromServer();
        this.pullSummaryFromServer();
        this.pullBookInfoFromServer();
    }

    componentWillUnmount() {
        console.log('Exchange Info Will Unmount');
        clearInterval(this.summaryUpdatesId);
        clearInterval(this.symbolUpdateId);
        clearInterval(this.bookUpdateId);
    }


    pullSummaryFromServer = () => {
        let self = this;

        if (this.state.selection === "None") {
            return;
        }

        axios.get("http://localhost:8080/transactions/summary/" + self.state.selection)
            .then(function (response) {
                self.setState((prevState, props) => ({
                    summaries: response.data,
                    symbols: prevState.symbols,
                    selection: prevState.selection,
                    askBook: prevState.askBook,
                    bidBook: prevState.bidBook
                }));
            }).catch(function (error) {
            console.log(error);
        })
    };

    pullBookInfoFromServer = () => {
        let self = this;

        axios.get("http://localhost:8080/transactions/" + self.state.selection)
            .then(function (response) {
                let asks = [];
                let bids = [];

                response.data.forEach((trans) => {
                    if (trans["action"] === 'A') {
                        asks.push(trans);
                    } else {
                        bids.push(trans);
                    }
                });

                asks.sort((a,b) => {
                    return a["price"] - b["price"];
                });

                bids.sort((a,b) => {
                    return a["price"] - b["price"];
                });

                if (bids.length > 10) {
                    bids = bids.slice(bids.length - 10, bids.length)
                }

                if (asks.length > 10) {
                    asks = asks.slice(0, 10)
                }

                self.setState((prevState, props) => ({
                    summaries: prevState.summaries,
                    symbols: prevState.symbols,
                    selection: prevState.selection,
                    askBook: asks,
                    bidBook: bids
                }));
            }).catch(function (error) {
            console.log(error);
        })
    };

    pullSymbolListFromServer = () => {
        let self = this;
        axios.get("http://localhost:8080/transactions/symbols")
            .then(function (response) {
                self.setState((prevState, props) => ({
                    summaries: prevState.summaries,
                    symbols: response.data,
                    selection: prevState.selection,
                    askBook: prevState.askBook,
                    bidBook: prevState.bidBook
                }));
            }).catch(function (error) {
            console.log(error);
        })
    };

    changeSymbolSelection = () => {
        let symbol = document.getElementById("symbol_selection").value;

        this.setState((prevState, props) => ({
            summaries: prevState.summaries,
            symbols: prevState.symbols,
            selection: symbol,
            askBook: prevState.askBook,
            bidBook: prevState.bidBook
        }));
    };

    render() {
        let priceString = "(no information)";
        let askString = "(no information)";
        let bidString = "(no information)";
        let bidAskSpreadString = "(no information)";

        if (this.state.summaries["price"] && Number(this.state.summaries["price"]).toFixed(3) !== "-1.000") {
            priceString = Number(this.state.summaries["price"]).toFixed(3);
        }
        if (this.state.summaries["bidPrice"] && Number(this.state.summaries["bidPrice"]).toFixed(3) !== "-1.000") {
            bidString = Number(this.state.summaries["bidPrice"]).toFixed(3);
        }
        if (this.state.summaries["askPrice"] && Number(this.state.summaries["askPrice"]).toFixed(3) !== "-1.000") {
            askString = Number(this.state.summaries["askPrice"]).toFixed(3);
        }
        if (this.state.summaries["bidAskSpread"] && Number(this.state.summaries["bidAskSpread"]).toFixed(3) !== "-1.000") {
            bidAskSpreadString = Number(this.state.summaries["bidAskSpread"]).toFixed(6);
        }


        return (
            <div style={{display: 'flex', flexDirection: 'row', justifyContent: "space-between", flex: 1}}>
                <div> Please select an exchange:
                    <select style={{marginLeft: 20}} id="symbol_selection" onChange={this.changeSymbolSelection}>
                        <option value="None">None</option>
                        {this.state.symbols.map(function (item) {
                            return <option value={item}>{item}</option>
                        })}
                    </select>
                    <div style={{marginTop: 40, fontSize:30, color: 'green'}}>
                        Current Price: {priceString}
                    </div>
                    <div style={{marginTop: 40}}>
                        Bid Price: {bidString}
                    </div>
                    <div style={{marginTop: 40}}>
                        Ask Price: {askString}
                    </div>
                    <div style={{marginTop: 40}}>
                        Bid/Ask Spread: {bidAskSpreadString}
                    </div>
                </div>
                <div style={{marginLeft: 200}}>
                    <table className={"Main-Table"}>
                        <thead>
                        <tr>
                            <th className={"Table-Entry"}>Price</th>
                            <th className={"Table-Entry"}>Quantity</th>
                        </tr>
                        </thead>
                        <tbody>
                        {this.state.bidBook.map(function (trans) {
                            return (
                                <tr>
                                    <td className={"Table-Entry"}>{Number(trans["price"]).toFixed(4)}</td>
                                    <td className={"Table-Entry"}>{trans["quantity"]}</td>
                                </tr>
                            );
                        })}
                        <tr>
                            <td className={"Table-Entry"} style={{color: 'red', fontWeight: "bold"}}>
                                BID/ASK SPLIT
                            </td>
                            <td className={"Table-Entry"} style={{color: 'red', fontWeight: "bold"}}>
                                ---------------
                            </td>
                        </tr>
                        {this.state.askBook.map(function (trans) {
                            return (
                                <tr>
                                    <td className={"Table-Entry"}>{Number(trans["price"]).toFixed(4)}</td>
                                    <td className={"Table-Entry"}>{trans["quantity"]}</td>
                                </tr>
                            );
                        })}
                        </tbody>
                    </table>
                </div>
            </div>
        );
    }
}