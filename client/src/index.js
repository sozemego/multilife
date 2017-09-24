import p5 from 'p5';
import {createNetworkLayer} from './network';
import {createLoginUi} from './login-ui';
import {createGameUI} from './game-ui';
import {createGame} from './game';
import {notify, on} from './event-bus';
import {LOGGED_IN, NEW_GAME, REPEAT_LOGIN, TO_MAIN_MENU} from './events';

export const sketch = new p5(p => {

	let loginUi;
	let gameUi;
	let game;
	let network;

	p.setup = () => {
		network = createNetworkLayer(WEBSOCKET_HOST);
		network.connect();
		loginUi = createLoginUi();
		loginUi.createLoginView();
		gameUi = createGameUI(p.createCanvas(window.innerWidth, window.innerHeight));
		game = createGame(p);
		p.frameRate(game.getFPS());
		on(TO_MAIN_MENU, () => {
			network.connect();
			game.unregister();
			game = createGame(p);
		});
		on(NEW_GAME, () => {
			network.connect();
			game.unregister();
			game = createGame(p);
			notify(REPEAT_LOGIN);
		})
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