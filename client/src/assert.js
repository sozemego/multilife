import {throwError} from './utils';

/**
 * Contains basic functions which assert values of arguments
 * are of correct type.
 */


/**
 * Returns type of the argument. Credit to: https://github.com/MathieuTurcotte/node-precond/blob/master/lib/checks.js
 * @param argument
 * @returns {*}
 */
const typeOf = argument => {
	const type = typeof argument;
	if (type === 'object') {
		if (!argument) {
			return 'null';
		} else if (argument instanceof Array) {
			return 'array';
		}
	}
	return type;
};

const assertType = expected => {
	return (argument, message) => {
		const type = typeOf(argument);

		if (expected === type) {
			return argument;
		}

		message = message || `Assertion failed`;
		throwError(`${message}. Expected [${expected}], but got [${argument}]`);
	};
};

export const assertInstanceOf = (argument, type, message) => {
	assertIsFunction(type);
	if(argument instanceof type) {
		return argument;
	}
	message = message || `Assertion failed`;
	throwError(`${message}. Expected [${type}], but got [${argument}]`);
};

export const assertIsArray = assertType('array');
export const assertIsNumber = assertType('number');
export const assertIsBoolean = assertType('boolean');
export const assertIsFunction = assertType('function');
export const assertIsString = assertType('string');
export const assertIsObject = assertType('object');
