import p5 from 'p5';
import {createNetworkLayer} from './network';
import {createLoginUi} from './login-ui';
import {createGameUI} from './game-ui';
import {createGame} from './game';
import {on} from './event-bus';
import {LOGGED_IN} from './events';

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
		p.windowResized();
		game.draw();
	};

	p.mouseReleased = () => {
		gameUi.onMouseUp();
	};

	p.windowResized = () => {
		p.resizeCanvas(
			game.getWidth() * game.getCellSize(),
			game.getHeight() * game.getCellSize(),
			true
		);
	};
});