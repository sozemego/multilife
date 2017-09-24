# Multilife

Multilife is a multiplayer game of life. Players can place
cells on the board and those cells will interact based on game of life rules (basic B2/S3).

Backend is written in Java, supported by the [Spark micro-framework](http://sparkjava.com/).
[Maven](https://maven.apache.org/) is used as a dependency/build tool.
For storing metrics data, [MongoDB](https://www.mongodb.com/) was used,
but it's not required to install this game.

Frontend is obviously javascript (ES6). For graphics, [p5](https://p5js.org/) is used.
[Webpack](https://webpack.github.io/) was the bundler.
For the metrics page, [React](https://facebook.github.io/react/) and [d3](https://d3js.org/) aided in UI creation.

# Installation

Clone the repository to a directory of your choosing.

### CLIENT

In the /client directory run:
```sh
npm install
```

For dev build, run:
```sh
npm run start
```

For production build, run:
```sh
npm run build
```

### SERVER

In the /server folder run
```sh
compile assembly:single
```

You will find the built .jar file in the /server/target directory. Run it:
```sh
java -jar server-1.0.0-jar-with-dependencies.jar
```

After running it once, app.cfg file will be created. There, you can configure
a few aspects of the game or server. One value has to be supplied by you,
namely externalStaticFilesPath. This is the absolute path to a directory containing the client.

After editing the app.cfg file, you can restart the server. In your browser, go to
```sh
http://localhost:8000
```
and you can play!




