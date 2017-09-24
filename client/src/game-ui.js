import {convertIntToHexColor, getKey} from './utils';
import {notify, on} from './event-bus';
import {
	GAME_ENDED,
	LOGGED_IN, NEW_GAME, PLACE_SHAPE, PLAYER_DATA_UPDATED, SHAPE_SELECTED,
	TIME_REMAINING, TO_MAIN_MENU
} from './events';
import {assertInstanceOf, assertIsObject, assertIsString} from './assert';

const shapeMap = {};

let selectedShape = null;
let recentlyClicked = false;

let loggedIn = false;
let playerData = undefined;

const keys = {
	Q: 81,
	W: 87,
	E: 69,
	R: 82,
	A: 65,
	S: 83,
	D: 68
};

/**
 * Parses a given string into a shape. Each row in a shape is delimited with a comma.
 * Character 0 means dead cell, 1 means alive cell.
 * Returns an array of objects, where each object specifies the offset
 * (x, y) form origin (top-left) and specifies if a cell is dead or alive.
 * @param str
 * @returns {Array}
 * @private
 */
const parseShape = str => {
	assertIsString(str);
	const tokens = str.split(',');
	const rows = tokens.length;
	const columns = tokens[0].length;
	const offsets = [];
	for (let i = 0; i < rows; i++) {
		for (let j = 0; j < columns; j++) {
			const bit = tokens[i].charAt(j);
			offsets.push({x: i, y: j, bit});
		}
	}
	return offsets;
};

const styleCanvas = p5Canvas => {
	assertIsObject(p5Canvas);
	p5Canvas.canvas.classList.add('canvas');
};

const renderPlayerPoints = nextPlayerData => {
	playerData = nextPlayerData;

	const dom = document.getElementById('player-points');
	dom.innerHTML = '';

	if (!playerData) {
		return;
	}

	for (const playerId in playerData) {
		if (playerData.hasOwnProperty(playerId)) {
			const color = playerData[playerId].color;
			const name = playerData[playerId].name;
			const points = playerData[playerId].points;

			const listElement = document.createElement('div');
			listElement.classList.add('player-points-element');

			const playerColorElement = document.createElement('span');
			playerColorElement.classList.add('player-points-color');
			playerColorElement.style.backgroundColor = convertIntToHexColor(color);

			listElement.appendChild(playerColorElement);

			const nameElement = document.createElement('span');
			nameElement.classList.add('player-points-name');
			const nameNode = document.createTextNode(name);
			nameElement.appendChild(nameNode);
			listElement.appendChild(nameElement);

			const pointsElement = document.createElement('span');
			pointsElement.classList.add('player-points-points');

			const pointsNode = document.createTextNode(points === undefined ? '0' : points);
			pointsElement.appendChild(pointsNode);

			listElement.appendChild(pointsElement);

			dom.appendChild(listElement);
		}
	}
};

/**
 * Parses remaining time in milliseconds to be in the mm:ss format.
 * @param remainingTime
 * @returns {string}
 * @private
 */
const parseRemainingTime = remainingTime => {
	remainingTime = parseInt(remainingTime);
	let second = Math.max(Math.floor((remainingTime / 1000) % 60), 0);
	let minute = Math.max(Math.floor((remainingTime / (1000 * 60)) % 60), 0);

	second = '' + second;
	second = ('00' + second).substr(second.length);

	minute = '' + minute;
	minute = ('00' + minute).substr(minute.length);

	return minute + ':' + second;
};

/**
 * Handler for when the client receives RemainingTime message.
 * @param msg
 * @private
 */
const onRemainingTime = msg => {
	assertIsObject(msg);
	const remainingTime = msg.remainingTime;

	const dom = document.getElementById('remaining-time');
	dom.innerHTML = '';

	const span = document.createElement('span');
	span.appendChild(document.createTextNode(parseRemainingTime(remainingTime)));
	dom.appendChild(span);
};

const hideRemainingTime = () => {
	const dom = document.getElementById('remaining-time');
	dom.innerHTML = '';
};

const selectShape = keyCode => {
	if (!loggedIn) {
		return;
	}
	selectedShape = shapeMap[keyCode];
	renderAvailableShapes();
	notify(SHAPE_SELECTED, Object.assign({}, selectedShape));
};

const renderAvailableShapes = () => {
	const dom = document.getElementById('available-shapes');
	dom.innerHTML = '';

	if (!loggedIn) {
		return;
	}

	for (const key in shapeMap) {
		if (shapeMap.hasOwnProperty(key)) {

			const shape = shapeMap[key];
			const shapeName = shape.name;
			const button = getKey(keys, key);

			const container = document.createElement('div');
			container.style.width = '150px';
			if (selectedShape && shapeName === selectedShape.name) {
				container.style.backgroundColor = 'rgba(127, 127, 127, 0.3)';
				container.style.border = '1px solid gray';
				container.style.cursor = 'pointer';
			} else {
				container.style.border = '1px solid rgba(0, 0, 0, 0)';
			}

			container.addEventListener('click', () => {
				selectShape(key);
			});

			const textElement = document.createElement('span');
			textElement.style.display = 'inline-block';
			textElement.appendChild(document.createTextNode(button + ' ' + shapeName));

			container.appendChild(textElement);
			dom.appendChild(container);
		}
	}
};

/**
 * Creates shapes that player is able to spawn.
 * @private
 */
const initShapes = () => {
	// INIT BASIC RULES
	shapeMap[keys.Q] = {name: 'block', shape: parseShape('11,11')};
	shapeMap[keys.W] = {name: 'hive', shape: parseShape('0110,1001,0110')};
	shapeMap[keys.E] = {name: 'loaf', shape: parseShape('0010,0101,1001,0110')};
	shapeMap[keys.R] = {name: 'tub', shape: parseShape('010,101,010')};
	shapeMap[keys.A] = {name: 'blinker', shape: parseShape('1,1,1')};
	shapeMap[keys.S] = {
		name: 'floodgate',
		shape: parseShape('1110000,0100000,0000000,0000000,0000001,0000011,0000001')
	};
};

const onLogin = () => {
	loggedIn = true;
	hideScores();
	hideOverlay();
	renderAvailableShapes();
};

const placeCanvasInContainer = p5Canvas => {
	assertIsObject(p5Canvas);
	const canvas = p5Canvas.canvas;
	const canvasParent = canvas.parentNode;
	canvasParent.removeChild(canvas);
	document.getElementById('canvas-container').appendChild(canvas);
};

const onGameEnd = () => {
	showOverlay();
	renderScores();
	renderEndOfGameButtons();
};

const showOverlay = () => {
	document.getElementById('gray-overlay').classList.remove('gray-overlay-hidden');
};

const hideOverlay = () => {
	document.getElementById('gray-overlay').classList.add('gray-overlay-hidden');
};

const renderScores = () => {
	if(!playerData) {
		return;
	}

	const sortedPlayerData = getPlayerDataSortedByPoints();

	const scoresContainer = document.getElementById('scores-container');
	scoresContainer.classList.remove('scores-hidden');
	const scoresElement = document.getElementById('scores');
	scoresElement.innerHTML = '';

	for (const playerId in sortedPlayerData) {
		if (sortedPlayerData.hasOwnProperty(playerId)) {

			const color = sortedPlayerData[playerId].color;
			const name = sortedPlayerData[playerId].name;
			const points = sortedPlayerData[playerId].points;

			const listElement = document.createElement('div');
			listElement.classList.add('player-points-element');

			const playerColorElement = document.createElement('span');
			playerColorElement.classList.add('player-points-color');
			playerColorElement.style.backgroundColor = convertIntToHexColor(color);

			listElement.appendChild(playerColorElement);

			const nameElement = document.createElement('span');
			nameElement.classList.add('player-points-name');
			const nameNode = document.createTextNode(name);
			nameElement.appendChild(nameNode);
			listElement.appendChild(nameElement);

			const pointsElement = document.createElement('span');
			pointsElement.classList.add('player-points-points');

			const pointsNode = document.createTextNode(points === undefined ? '0' : points);
			pointsElement.appendChild(pointsNode);

			listElement.appendChild(pointsElement);

			scoresElement.appendChild(listElement);
		}
	}
};

const getPlayerDataSortedByPoints = () => {
	const sortedPlayerData = {};

	const tempArray = [];
	for (const playerId in playerData) {
		if (playerData.hasOwnProperty(playerId)) {

			tempArray.push({
				playerId,
				color: playerData[playerId].color,
				name: playerData[playerId].name,
				points: playerData[playerId].points
			})
		}
	}
	tempArray.sort((a, b) => b.points - a.points);
	tempArray.forEach(e => {
		sortedPlayerData[e.playerId] = {
			color: e.color,
			name: e.name,
			points: e.points
		};
	});

	return sortedPlayerData;
};

const renderEndOfGameButtons = () => {

	const buttonsElement = document.getElementById('scores-buttons');
	buttonsElement.innerHTML = '';

	const backToMenuButton = document.createElement('button');
	backToMenuButton.classList.add('score-button');
	backToMenuButton.appendChild(document.createTextNode('BACK'));
	backToMenuButton.addEventListener('click', () => notify(TO_MAIN_MENU));
	buttonsElement.appendChild(backToMenuButton);

	const nextGameButton = document.createElement('button');
	nextGameButton.classList.add('score-button');
	nextGameButton.appendChild(document.createTextNode('NEXT GAME'));
	nextGameButton.addEventListener('click', () => notify(NEW_GAME));
	buttonsElement.appendChild(nextGameButton);
};

const onToMainMenu = () => {
	loggedIn = false;
	hideScores();
	hideOverlay();
	renderPlayerPoints(undefined);
	renderAvailableShapes();
	hideRemainingTime();
};

export const hideScores = () => {
	document.getElementById('scores-container').classList.add('scores-hidden');
	document.getElementById('scores').innerHTML = '';
	document.getElementById('scores-buttons').innerHTML = '';
};

export const createGameUI = p5Canvas => {
	assertIsObject(p5Canvas);

	styleCanvas(p5Canvas);
	placeCanvasInContainer(p5Canvas);

	const ui = {};

	ui.onMouseUp = () => {
		if (!loggedIn) {
			return;
		}

		if (selectedShape) {
			recentlyClicked = true;
			notify(PLACE_SHAPE);
		}
	};

	ui.keyPressed = keyCode => {
		selectShape(keyCode);
	};

	initShapes();

	on(PLAYER_DATA_UPDATED, renderPlayerPoints);
	on(TIME_REMAINING, onRemainingTime);
	on(LOGGED_IN, onLogin);
	on(GAME_ENDED, onGameEnd);
	on(TO_MAIN_MENU, onToMainMenu);

	p5Canvas.mouseReleased(ui.onMouseUp);

	return ui;
};