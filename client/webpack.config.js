const path = require("path");
const CopyWebpackPlugin = require('copy-webpack-plugin');
const webpack = require("webpack");

module.exports = {
	entry: {
		index: "./src/index.js",
		metrics: "./src/metrics/Metrics.js"
	},
	output: {
		filename: "[name].js",
		path: path.resolve(__dirname, "../server/src/main/resources/public")
	},
	devtool: "inline-source-map",
	module: {
		rules: [
			{
				test: /\.js$/,
				exclude: /node_modules/,
				use: [
					{
						loader: "babel-loader"
					}
				]
			},
			{
				test: /\.svg$/,
				exclude: /node_modules/,
				use: [
					{
						loader: "file-loader",
						query: {
							name: 'static/media/[name].[hash:8].[ext]'
						}
					}
				]
			}
		]
	},
	plugins: [
		new CopyWebpackPlugin([
			{from: "src/index.html"},
			{from: "src/metrics/metrics.html"},
			{from: "src/main.css"},
			{from: "src/login_background.gif"}
		]),
		new webpack.DefinePlugin({
			WEBSOCKET_HOST: JSON.stringify("ws://127.0.0.1:8000/game"),
			METRICS_WEBSOCKET_HOST: JSON.stringify("ws://127.0.0.1:8000/metrics-live")
		}),
	],
	externals: {
		d3: "d3"
	}
};