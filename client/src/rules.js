/**
 * Parses a string into a game of life rule.
 * The string should have the following format:
 * birthdigits/survivedigits
 * eg. B3/S23, B356/S89
 * Non-digit characters are discarded, but can be used for ease of
 * reading.
 * Returns a function which accepts as first argument number of
 * alive neighbours and state (alive/dead) as second argument.
 * This function returns an integer. 0 means that state of the cell
 * should not change, -1 means cell dies, 1 means cell comes to life.
 * @param ruleString
 * @returns {Function}
 */
import {assertIsString} from './assert';

const ruleCreator = ruleString => {
	assertIsString(ruleString);
	const tokens = ruleString.split('/');
	const birthNumbers = extractNumbers(tokens[0]);
	const surviveNumbers = extractNumbers(tokens[1]);

	return function (n, alive) {
		if (alive) {
			if (!surviveNumbers.includes(n)) return -1;
		} else {
			if (birthNumbers.includes(n)) return 1;
		}
		return 0;
	};
};

const extractNumbers = token => {
	assertIsString(token);
	const numbers = [];
	const result = token.match(/\d/g);
	if (result) {
		for (let i = 0; i < result.length; i++) {
			numbers.push(parseInt(result[i]));
		}
	}
	return numbers;
};

export const basicRule = ruleCreator('B3/S23');