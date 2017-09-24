import {assertIsFunction, assertIsString} from './assert';

const observers = {};

export const on = (event, handler) => {
	assertIsString(event);
	assertIsFunction(handler);

	const eventObservers = observers[event];
	if (!eventObservers) {
		observers[event] = [];
	}
	observers[event].push(handler);
};

export const notify = (event, message) => {
	assertIsString(event);

	const eventObservers = observers[event];
	if (eventObservers) {
		observers[event].forEach(fn => fn(message));
	}
};