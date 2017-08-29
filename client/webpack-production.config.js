const path = require("path");
const CopyWebpackPlugin = require('copy-webpack-plugin');

module.exports = {
	entry: {
		game: "./src/Game.js",
		metrics: "./src/metrics/Metrics.js"
	},
	output: {
		filename: "[name].js",
		path: path.resolve(__dirname, "build")
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
		])
	],
	externals: {
		d3: "d3",
		config: JSON.stringify(require("./prod.config.json"))
	}
};