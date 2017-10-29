import {assertIsArray, assertIsNumber, assertIsObject, assertIsString} from './assert';

export const throwError = message => {
  assertIsString(message);
  throw new Error(message);
};

/**
 * Finds and returns the maximum number in an array.
 */
export const findMaxNumber = arr => {
  assertIsArray(arr);
  let max = Number.MIN_SAFE_INTEGER;
  arr.forEach(i => {
    if (i > max) max = i;
  });
  return max;
};

/**
 * Generates and returns a random color in the form of #HEX (e.g. #000000).
 */
export const generateRandomColor = () => '#' + Math.floor(Math.random() * 16777215).toString(16);

/**
 * Generates a specified amount of random colors and returns an array of them.
 * Colors are in the form of #HEX (e.g. #000000).
 */
export const generateRandomColors = amount => {
  assertIsNumber(amount);
  const colors = [];
  for (let i = 0; i < amount; i++) {
    colors.push(generateRandomColor());
  }
  return colors;
};

export const convertIntToHexColor = int => {
  assertIsNumber(int);
  int >>>= 0;
  const b = int & 0xFF,
    g = (int & 0xFF00) >>> 8,
    r = (int & 0xFF0000) >>> 16;
  return 'rgb(' + [r, g, b].join(',') + ')';
};

/**
 * Iterates over key-property pairs of an object
 * and returns the key associated with the given value.
 * @param obj
 * @param value
 * @returns {string}
 */
export const getKey = (obj, value) => {
  assertIsObject(obj);
  for (const key in obj) {
    if (obj.hasOwnProperty(key)) {
      if (obj[key] == value) {
        return key;
      }
    }
  }
};

/**
 * Returns a number of properties given object has.
 * @param obj
 * @returns {Number}
 */
export const getNumberOfProperties = obj => {
  assertIsObject(obj);
  return Object.keys(obj).length;
};