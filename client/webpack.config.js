let path = require("path");

module.exports = {
  entry: "./src/game.js",
  output: {
    filename: "game.js",
	path: path.resolve(__dirname, "public")
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
  }
};