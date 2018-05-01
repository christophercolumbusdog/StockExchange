import React from 'react';
import './App.css';
import { withRR4, Nav, NavText } from 'react-sidenav';
import { BrowserRouter as Router, Route } from 'react-router-dom';
import {ExchangeInfo} from "./ExchangeInfo";
import {UserPage} from "./UserPage";
import {ExchangeGraph} from "./ExchangeGraph";

const SideNav = withRR4();


export class SideBarRouter extends React.Component {

    constructor(props) {
        super(props);

    }

    renderDashboard = () => {
        return <div>Welcome to the Exchange web monitor!</div>;
    };

    renderExchangeGraph = () => {
        return <ExchangeGraph />;
    };

    renderExchangeInfo = () => {
        return <ExchangeInfo />;
    };

    renderUsers = () => {
        return <UserPage />
    };


    render() {
        return (
            <Router>
                <div style={{display: 'flex', flexDirection: 'row'}}>
                    <div style={{width: 220, backgroundColor: '#00A8E8', height: "100vh" }}>
                        <SideNav default='dashboard' highlightBgColor='#003459' highlightColor='white'>
                            <Nav id='dashboard'>
                                <NavText className="Nav-menu-item">  Dashboard </NavText>
                            </Nav>
                            <Nav id='exchanges'>
                                <NavText className="Nav-menu-item"> Exchange Info </NavText>
                            </Nav>
                            <Nav id='graph'>
                                <NavText className="Nav-menu-item"> Exchange Graph </NavText>
                            </Nav>
                            <Nav id='users'>
                                <NavText className="Nav-menu-item"> Connected Users </NavText>
                            </Nav>
                        </SideNav>
                    </div>
                    <div style={{padding: 20}}>
                        <Route exact path="/dashboard" render={this.renderDashboard}/>
                        <Route path="/graph" render={this.renderExchangeGraph}/>
                        <Route path="/exchanges" render={this.renderExchangeInfo}/>
                        <Route path="/users" render={this.renderUsers}/>
                    </div>
                </div>
            </Router>
        );
    }
}