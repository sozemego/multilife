/**
 * Finds and returns the maximum element of an array.
 */
export const findMaxNumber = arr => {
	let max = 0;
	arr.forEach(i => {
		if(i > max) max = i;
	});
	return max;
};

/**
 * Generates and returns a random color in the form of #HEX (e.g. #000000).
 */
export const generateRandomColor = () => '#'+Math.floor(Math.random()*16777215).toString(16);

/**
 * Generates a specified amount of random colors and returns an array of them.
 * Colors are in the form of #HEX (e.g. #000000).
 */
export const generateRandomColors = amount => {
	let colors = [];
	for(let i = 0; i < amount; i++) {
		colors.push(generateRandomColor());
	}
	return colors;
};