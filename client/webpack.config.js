let path = require("path");
var CopyWebpackPlugin = require('copy-webpack-plugin');

module.exports = {
	entry: {
		game: "./src/Game.js",
		metrics: "./src/metrics/Metrics.js"
	},
	output: {
		filename: "[name].js",
		path: path.resolve(__dirname, "../server/src/main/resources/public")
	},
	devtool: "source-map",
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
			}
		]
	},
	plugins: [
		new CopyWebpackPlugin([
			{from: "src/index.html"},
			{from: "src/metrics/metrics.html"},
			{from: "src/main.css"},
			{from: "src/login_background.gif"}
		])
	],
	externals: {
		d3: "d3"
	}
};