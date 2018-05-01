import React from 'react';
import ReactDOM from 'react-dom';
import './index.css';
import App from './App';
import registerServiceWorker from './registerServiceWorker';
import {SideBarRouter} from "./RouterNavBar";


ReactDOM.render(<App />, document.getElementById('root'));
ReactDOM.render(<SideBarRouter />, document.getElementById('side'));
registerServiceWorker();
