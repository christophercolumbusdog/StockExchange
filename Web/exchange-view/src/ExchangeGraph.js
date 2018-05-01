import React from "react";
import {LineChart} from "react-easy-chart";

const axios = require('axios');

export class ExchangeGraph extends React.Component {
    constructor(props) {
        super(props);

        let dateString = ExchangeGraph.getDateString();

        this.state = {summaries: [[{x: dateString, y: 0}]], symbols: [], selection: "None"};
    }

    static getDateString() {
        let date = new Date();
        let minutes = date.getMinutes() < 10 ? "0" + date.getMinutes() : date.getMinutes();
        return date.getHours() + ":" + minutes + ":" + date.getSeconds();
    }

    componentDidMount() {
        console.log('Exchange Graph Mounted');
        this.symbolUpdateId = setInterval(this.pullSymbolListFromServer, 20000);
        this.summaryUpdatesId = setInterval(this.pullSummaryFromServer, 1000);

        this.pullSymbolListFromServer();
    }

    componentWillUnmount() {
        console.log('Exchange Graph Will Unmount');
        clearInterval(this.symbolUpdateId);
        clearInterval(this.summaryUpdatesId);
    }

    pullSummaryFromServer = () => {
        let self = this;

        if (this.state.selection === "None" || this.state.selection === "none") {
            return;
        }

        axios.get("http://localhost:8080/transactions/summary/" + self.state.selection)
            .then(function (response) {
                let currentPrice = response.data["price"];

                let dateString = ExchangeGraph.getDateString();

                let data = {
                    x: dateString,
                    y: currentPrice
                };


                console.log(self.state.summaries);

                self.setState((prevState, props) => {
                    if (prevState.summaries[0].length > 20) {
                        prevState.summaries[0].shift();
                    }
                    if (prevState.summaries[0][0]['y'] === 0) {
                        prevState.summaries[0].shift();
                    }

                    prevState.summaries[0].push(data);

                    return ({
                        summaries: prevState.summaries,
                        symbols: prevState.symbols,
                        selection: prevState.selection,
                    });
                });
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
                    selection: prevState.selection
                }));
            }).catch(function (error) {
            console.log(error);
        })
    };

    changeSymbolSelection = () => {
        let symbol = document.getElementById("symbol_selection").value;

        let dateString = ExchangeGraph.getDateString();

        this.setState((prevState, props) => ({
            summaries: [[{x: dateString, y: 0}]],
            symbols: prevState.symbols,
            selection: symbol
        }));
    };

    render() {
        return (
            <div>
                <div> Please select an exchange:
                    <select style={{marginLeft: 20}} id="symbol_selection" onChange={this.changeSymbolSelection}>
                        <option value="None">None</option>
                        {this.state.symbols.map(function (item) {
                            return <option value={item}>{item}</option>
                        })}
                    </select>
                </div>
                <div>
                    <LineChart
                        data={this.state.summaries}
                        datePattern={'%H:%M:%S'}
                        xType={'time'}
                        width={600}
                        height={300}
                        axisLabels={{x: 'Hour', y: 'Price'}}
                        interpolate={'cardinal'}
                        axes
                        grid
                    />
                </div>
            </div>
        );
    }
}