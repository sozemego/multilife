/**
 * Part of the application responsible for displaying various metrics.
 */
import React from "react";
import ReactDOM from "react-dom";
import {Divider, MenuItem, MuiThemeProvider, Paper, SvgIcon} from "material-ui";
import MetricsSocket from "./MetricsSocket";
import TotalMessagesMetric from "./TotalMessagesMetric";
import injectTapEventPlugin from "react-tap-event-plugin";
import AverageKbMetric from "./AverageKbMetric";
import TypeCountMetric from "./TypeCountMetric";
import LineIcon from "./line-chart.svg";
import BarChart from "./bar-chart-7.svg";
import PlayerCountMetric from "./PlayerCountMetric";
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
	sideBarIcon: {
		width: "24px",
		height: "24px"
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
						<MenuItem onTouchTap={() => this.setState({selectedView: 0})}
								  leftIcon={<img src={LineIcon} alt="Maxim Basinski, http://www.flaticon.com/authors/maxim-basinski"/>}>
							Average kb/s
						</MenuItem>
						<MenuItem onTouchTap={() => this.setState({selectedView: 1})}
								  leftIcon={<img src={BarChart} alt="Maxim Basinski, http://www.flaticon.com/authors/maxim-basinski"/>}>
							Type chart
						</MenuItem>
						<MenuItem onTouchTap={() => this.setState({selectedView: 2})}
								  leftIcon={<img src={LineIcon} alt="Maxim Basinski, http://www.flaticon.com/authors/maxim-basinski"/>}>
							Player count
						</MenuItem>
					</Paper>
					<div style={styles.content}>
						<div id="total-bytes-sent" style={styles.totalBytes}/>
						<div id="total-bytes-received" style={styles.totalBytes}/>
						<Divider />
						<div id="average-kbs-container" style={selectedView !== 0 ? {display:"none"} : styles.averageKbsContainer}>
							<div id="average-kbs">

							</div>
						</div>
						<div style={selectedView !== 1 ? {display:"none"} : styles.typeCountTitle}>
							<p>Message type count</p>
							<div id="message-type-count">

							</div>
						</div>
						<div style={selectedView !== 2 ? {display:"none"} : styles.typeCountTitle}>
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

let socket = new MetricsSocket();
new TotalMessagesMetric(socket);
new AverageKbMetric(socket);
new TypeCountMetric(socket);
new PlayerCountMetric(socket);

