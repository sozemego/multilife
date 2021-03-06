const path = require('path');
const CopyWebpackPlugin = require('copy-webpack-plugin');
const webpack = require('webpack');

module.exports = {
	entry: {
		index: './src/index.js',
		metrics: './src/metrics/Metrics.js'
	},
	output: {
		filename: '[name].js',
		path: path.resolve(__dirname, 'build')
	},
	module: {
		rules: [
			{
				test: /\.js$/,
				exclude: /node_modules/,
				use: [
					{
						loader: 'babel-loader'
					}
				]
			},
			{
				test: /\.svg$/,
				exclude: /node_modules/,
				use: [
					{
						loader: 'file-loader',
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
			{from: 'src/index.html'},
			{from: 'src/metrics/metrics.html'},
			{from: 'src/main.css'},
			{from: 'src/login_background.gif'}
		]),
		new webpack.DefinePlugin({
			WEBSOCKET_HOST: JSON.stringify('ws://138.68.89.151:8000/game'),
			METRICS_WEBSOCKET_HOST: JSON.stringify('ws://138.68.89.151:8000/metrics-live'),
			'process.env': {
				NODE_ENV: JSON.stringify('production')
			}
		})
	],
	externals: {
		d3: 'd3',
		p5: 'p5'
	}
};