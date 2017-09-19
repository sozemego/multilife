import {sketch} from "./Game";

export const rectRenderFunction = (x, y, width, height, color) => {
	sketch.fill(color);
	sketch.rect(x, y, width, height);
};

export const ellipseRenderFunction = (x, y, width, height, color) => {
	sketch.fill(color);
	sketch.ellipse(x + width / 2, y + height / 2, width, height);
};