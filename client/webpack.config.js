let path = require("path");
var CopyWebpackPlugin = require('copy-webpack-plugin');

module.exports = {
  entry: "./src/Game.js",
  output: {
    filename: "game.js",
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
	  { from: "src/index.html" },
	  { from: "src/main.css" },
	  { from: "src/login_background.gif"}
	])
  ]
};