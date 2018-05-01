import React from "react";
import {LineChart} from "react-easy-chart";
import {ExchangeGraph} from "./ExchangeGraph";

const axios = require('axios');

export class UserPage extends React.Component {
    constructor(props) {
        super(props);

        let dateString = ExchangeGraph.getDateString();

        this.state = { users: [], fundHistory: [[{x: dateString, y: 0}]], selection: "None" };
    }

    componentDidMount() {
        console.log('User Page Will Mount');
        this.updateUserListId = setInterval(this.pullCurrentUsersList, 4000);
        this.userUpdateId = setInterval(this.pullUserFromServer, 1000);

        this.pullCurrentUsersList();
    }

    componentWillUnmount() {
        console.log('User Page Will Unmount');
        clearInterval(this.updateUserListId);
        clearInterval(this.userUpdateId);
    }

    pullCurrentUsersList = () => {
        let self = this;
        axios.get("http://localhost:8080/users")
            .then(function (response) {
                self.setState((prevState, props) => ({
                    users: response.data,
                    fundHistory: prevState.fundHistory,
                    selection: prevState.selection
                }));

                console.log(self.state);
            }).catch(function (error) {
            console.log(error);
        })
    };


    changeUserSelection = () => {
        let symbol = document.getElementById("user_selection").value;

        let dateString = ExchangeGraph.getDateString();

        this.setState((prevState, props) => ({
            fundHistory: [[{x: dateString, y: 0}]],
            users: prevState.users,
            selection: symbol
        }));
    };

    pullUserFromServer = () => {
        let self = this;

        if (this.state.selection === "None" || this.state.selection === "none") {
            return;
        }

        axios.get("http://localhost:8080/users")
            .then(function (response) {
                let updatedUser = response.data.filter(x => x["username"] === self.state.selection)[0];

                let currentFunds = updatedUser["funds"];

                let dateString = ExchangeGraph.getDateString();

                let data = {
                    x: dateString,
                    y: currentFunds
                };


                self.setState((prevState, props) => {
                    if (prevState.fundHistory[0].length > 20) {
                        prevState.fundHistory[0].shift();
                    }
                    if (prevState.fundHistory[0][0]['y'] === 0) {
                        prevState.fundHistory[0].shift();
                    }

                    prevState.fundHistory[0].push(data);

                    return ({
                        fundHistory: prevState.fundHistory,
                        users: prevState.users,
                        selection: prevState.selection,
                    });
                });
            }).catch(function (error) {
            console.log(error);
        })
    };

    render() {

        return(
            <div>
                <div style={{marginBottom: 40, fontSize: 40}}>
                    Registered Users
                </div>
                <table className={"Main-Table"}>
                    <thead>
                    <tr>
                        <th className={"Table-Entry"}>Username</th>
                        <th className={"Table-Entry"}>Connection URL</th>
                        <th className={"Table-Entry"}>Funds</th>
                    </tr>
                    </thead>
                    <tbody>
                    {this.state.users.map(function (user) {
                        return (
                            <tr>
                                <td className={"Table-Entry"}>{user["username"]}</td>
                                <td className={"Table-Entry"}>{user["url"]}</td>
                                <td className={"Table-Entry"}>{Number(user["funds"]).toFixed(3)}</td>
                            </tr>
                        );
                    })}
                    </tbody>
                </table>
                <div style={{marginTop: 50}}> Please select an User:
                    <select style={{marginLeft: 20}} id="user_selection" onChange={this.changeUserSelection}>
                        <option value="None">None</option>
                        {this.state.users.map(function (item) {
                            return <option value={item["username"]}>{item["username"]}</option>
                        })}
                    </select>
                </div>
                <div>
                    <LineChart
                        data={this.state.fundHistory}
                        datePattern={'%H:%M:%S'}
                        xType={'time'}
                        width={600}
                        height={300}
                        axisLabels={{x: 'Hour', y: 'Funds'}}
                        interpolate={'cardinal'}
                        axes
                        grid
                    />
                </div>
            </div>
        )
    }

}