import React from "react";
import ReactDOM from "react-dom";
import {Divider, MuiThemeProvider, RaisedButton} from "material-ui";
import {metricsSocket} from "./metrics-socket";
import {totalMessagesMetric} from "./total-messages-metric";
import injectTapEventPlugin from "react-tap-event-plugin";
import AverageKbMetric from "./AverageKbMetric";
import OutgoingTypeCountMetric from "./OutgoingTypeCountMetric";
import IncomingTypeCountMetric from "./IncomingTypeCountMetric";

import PlayerCountMetric, {playerCountMetric} from "./player-count-metric";
import {Sidebar} from "./sidebar";
injectTapEventPlugin();

const styles = {
	container: {
		display: "flex",
		flexWrap: "noWrap",
		width: "100%",
		height: "100%"
	},
	content: {
		width: "85%",
		display: "flex",
		flexDirection: "column"
	},
	totalBytes: {
		width: "100%",
		textAlign: "center"
	},
	averageKbsContainer: {
		textAlign: "center"
	},
	typeCountContainer: {

	},
	typeCountTitle: {
		textAlign: "center"
	}
};

class Metrics extends React.Component {

	constructor(props) {
		super(props);
		this.state = {
			selectedView: 0
		}
	}

	onNavigationClicked = (selectedView) => {
		this.setState({selectedView});
	};

	render() {
		let {selectedView} = this.state;
		return(
			<MuiThemeProvider>
				<div style={styles.container}>
					<Sidebar onNavigationClicked={this.onNavigationClicked}/>
					<div style={styles.content}>
						<div id="total-bytes-sent" style={styles.totalBytes}/>
						<div id="total-bytes-received" style={styles.totalBytes}/>
						<Divider />
						<div id="average-kbs-container" style={selectedView !== 0 ? {display:"none"} : styles.averageKbsContainer}>
						  <div>
							<div>
							  <RaisedButton label={"NOW"} style={{margin: "2px"}} onTouchTap={() => averageKb.setLive()}/>
							  <RaisedButton label={"TODAY"} style={{margin: "2px"}} onTouchTap={() => averageKb.setDays(1)}/>
							  <RaisedButton label={"LAST 2 DAYS"} style={{margin: "2px"}} onTouchTap={() => averageKb.setDays(2)}/>
							  <RaisedButton label={"LAST 7 DAYS"} style={{margin: "2px"}} onTouchTap={() => averageKb.setDays(7)}/>
							  <RaisedButton label={"LAST 31 DAYS"} style={{margin: "2px"}} onTouchTap={() => averageKb.setDays(31)}/>
							</div>
							<div id="average-kbs-outgoing">

							</div>
						  </div>
						  <div>
							<div id="average-kbs-incoming">

							</div>
						  </div>

						</div>
						<div style={selectedView !== 1 ? {display:"none"} : styles.typeCountTitle}>
							<p>Message type count</p>
							<div id="message-type-count-outgoing">

							</div>
						</div>
						<div style={selectedView !== 2 ? {display:"none"} : styles.typeCountTitle}>
							<p>Message type count</p>
							<div id="message-type-count-incoming">

							</div>
						</div>
						<div style={selectedView !== 3 ? {display:"none"} : styles.typeCountTitle}>
							<div id="player-count">

							</div>
						</div>
					</div>
				</div>
			</MuiThemeProvider>
		);
	}

}

ReactDOM.render(<Metrics/>, document.getElementById("metrics"));

let socket = metricsSocket();
socket.init(METRICS_WEBSOCKET_HOST);

totalMessagesMetric(socket);
playerCountMetric(socket);

const averageKb = new AverageKbMetric(socket);
new OutgoingTypeCountMetric(socket);
new IncomingTypeCountMetric(socket);


