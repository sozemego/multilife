import {convertIntToHexColor, getKey, throwError} from "./utils";
import {notify, on} from "./event-bus";
import {
	LOGGED_IN, PLACE_SHAPE, PLAYER_DATA_UPDATED, SHAPE_SELECTED,
	TIME_REMAINING
} from "./events";
import {assertIsObject, assertIsString} from './assert';

const shapeMap = {};

let selectedShape = null;
let recentlyClicked = false;

let loggedIn = false;

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
	const tokens = str.split(",");
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
	p5Canvas.canvas.classList.add("canvas");
};

const renderPlayerPoints = playerData => {
	if (!playerData) {
		return;
	}

	const dom = document.getElementById("player-points");
	dom.innerHTML = "";

	for(const playerId in playerData) {
		if (playerData.hasOwnProperty(playerId)) {
			const color = playerData[playerId].color;
			const name = playerData[playerId].name;
			const points = playerData[playerId].points;

			const listElement = document.createElement("div");
			listElement.classList.add("player-points-element");

			const playerColorElement = document.createElement("span");
			playerColorElement.classList.add("player-points-color");
			playerColorElement.style.backgroundColor = convertIntToHexColor(color);

			listElement.appendChild(playerColorElement);

			const nameElement = document.createElement("span");
			nameElement.classList.add("player-points-name");
			const nameNode = document.createTextNode(name);
			nameElement.appendChild(nameNode);
			listElement.appendChild(nameElement);

			const pointsElement = document.createElement("span");
			pointsElement.classList.add("player-points-points");

			const pointsNode = document.createTextNode(points === undefined ? "0" : points);
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

	second = "" + second;
	second = ("00" + second).substr(second.length);

	minute = "" + minute;
	minute = ("00" + minute).substr(minute.length);

	return minute + ":" + second;
};

/**
 * Handler for when the client receives RemainingTime message.
 * @param msg
 * @private
 */
const onRemainingTime = msg => {
	assertIsObject(msg);
	const remainingTime = msg.remainingTime;

	const dom = document.getElementById("remaining-time");
	dom.innerHTML = "";

	const span = document.createElement("span");
	span.appendChild(document.createTextNode(parseRemainingTime(remainingTime)));
	dom.appendChild(span);
};

const keys = {
	Q: 81,
	W: 87,
	E: 69,
	R: 82,
	A: 65,
	S: 83,
	D: 68
};

const selectShape = keyCode => {
	if(!loggedIn) {
		return;
	}
	selectedShape = shapeMap[keyCode];
	renderAvailableShapes();
	notify(SHAPE_SELECTED, Object.assign({}, selectedShape));
};

const renderAvailableShapes = () => {
	if(!loggedIn) {
		return;
	}

	const dom = document.getElementById("available-shapes");
	dom.innerHTML = "";

	for (const key in shapeMap) {
		if (shapeMap.hasOwnProperty(key)) {

			const shape = shapeMap[key];
			const shapeName = shape.name;
			const button = getKey(keys, key);

			const container = document.createElement("div");
			container.addEventListener("click", () => {
				selectShape(key);
			});

			const textElement = document.createElement("span");
			textElement.style.display = "inline-block";
			textElement.appendChild(document.createTextNode(button + " " + shapeName));

			if (selectedShape && shapeName === selectedShape.name) {
				textElement.style.backgroundColor = "red";
			}

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
	shapeMap[keys.Q] = {name: "block", shape: parseShape("11,11")};
	shapeMap[keys.W] = {name: "hive", shape: parseShape("0110,1001,0110")};
	shapeMap[keys.E] = {name: "loaf", shape: parseShape("0010,0101,1001,0110")};
	shapeMap[keys.R] = {name: "tub", shape: parseShape("010,101,010")};
	shapeMap[keys.A] = {name: "blinker", shape: parseShape("1,1,1")};
	shapeMap[keys.S] = {
		name: "floodgate",
		shape: parseShape("1110000,0100000,0000000,0000000,0000001,0000011,0000001")
	};
};

const onLogin = () => {
	loggedIn = true;
	renderAvailableShapes();
};

export const createGameUI = p5Canvas => {
	assertIsObject(p5Canvas);

	styleCanvas(p5Canvas);

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

	p5Canvas.mouseReleased(ui.onMouseUp);

	return ui;
};