import {throwError} from "./utils";

const observers = {};
let logging = true;

export const setLogging = bool => {
	if(typeof bool !== "boolean") {
		throwError("Argument needs to be a boolean, it was: " + bool);
	}
	logging = bool;
};

export const on = (event, handler) => {
	if(!event) {
		throwError("You need to pass a event which is not falsy.");
	}
	if(typeof handler !== "function") {
		throwError("Handler needs to be a function, it was: " + handler);
	}

	const eventObservers = observers[event];
	if(!eventObservers) {
		observers[event] = [];
	}
	observers[event].push(handler);
};

export const notify = (event, message) => {
	if(logging) {
		console.log("Sent ", event, " with following message: ", message)
	}
	if(!event) {
		throwError("You need to pass a event which is not falsy.");
	}
	const eventObservers = observers[event];
	if(eventObservers) {
		if(logging) {
			console.log("There are ", observers[event].length, " observers for event: ", event);
		}
		observers[event].forEach(fn => fn(message));
	} else if(logging) {
		console.log("There are no observers for event", event);
	}
};