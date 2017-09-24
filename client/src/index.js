import p5 from "p5";
import {createNetworkLayer} from "./network";
import {createLoginUi} from "./login-ui";
import {createGameUI} from "./game-ui";
import {createGame} from "./game";

export const sketch = new p5(p => {

	let loginUi;
	let gameUi;
	let game;

	p.setup = () => {
		createNetworkLayer(WEBSOCKET_HOST);
		loginUi = createLoginUi();
		loginUi.createLoginView();
		gameUi = createGameUI(p.createCanvas(window.innerWidth, window.innerHeight));
		game = createGame(p);
		p.frameRate(game.getFPS());
	};

	p.keyPressed = (event) => {
		gameUi.keyPressed(event.keyCode);
	};

	p.draw = () => {
		game.draw();
	};

	p.mouseReleased = () => {
		gameUi.onMouseUp();
	};

	p.windowResized = () => {
		p.resizeCanvas(
			Math.max(window.innerWidth, game.getWidth() * game.getCellSize()),
			Math.max(window.innerHeight, game.getHeight() * game.getCellSize())
		);
	};
});