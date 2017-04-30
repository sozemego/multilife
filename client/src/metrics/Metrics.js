import React from "react";
import ReactDOM from "react-dom";
import {Divider, MenuItem, MuiThemeProvider, Paper} from "material-ui";
import MetricsSocket from "./MetricsSocket";
import TotalMessagesMetric from "./TotalMessagesMetric";
import injectTapEventPlugin from "react-tap-event-plugin";
import AverageKbMetric from "./AverageKbMetric";
import TypeCountMetric from "./TypeCountMetric";
injectTapEventPlugin();

const styles = {
	container: {
		display: "flex",
		flexWrap: "noWrap",
		width: "100%",
		height: "100%"
	},
	sidebar: {
		width: "15%"
	},
	content: {
		width: "85%",
		display: "flex",
		flexDirection: "column"
	},
	totalBytesSent: {
		width: "100%",
		textAlign: "center"
	}
};

export default class Metrics extends React.Component {

	constructor(props) {
		super(props);
		this.state = {
			selectedView: 0
		}
	}

	render() {
		let {selectedView} = this.state;
		return(
			<MuiThemeProvider>
				<div style={styles.container}>
					<Paper zDepth={2} style={styles.sidebar}>
						<MenuItem onTouchTap={() => this.setState({selectedView: 0})}>Average kb/s</MenuItem>
						<MenuItem onTouchTap={() => this.setState({selectedView: 1})}>Type chart</MenuItem>
					</Paper>
					<div style={styles.content}>
						<div id="total-bytes-sent" style={styles.totalBytesSent}/>
						<Divider />
						<div style={selectedView !== 0 ? {display:"none"} : {}}>
							<p>Average outgoing kb/s</p>
							<div id="average-kbs">

							</div>
						</div>
						<div id="message-type-count-container"  style={selectedView !== 1 ? {display:"none"} : {}}>
							<p>Message type count</p>
							<div id="message-type-count">

							</div>
						</div>
					</div>
				</div>
			</MuiThemeProvider>
		);
	}

}

ReactDOM.render(<Metrics/>, document.getElementById("metrics"));

let socket = new MetricsSocket();
new TotalMessagesMetric(socket);
new AverageKbMetric(socket);
new TypeCountMetric(socket);

