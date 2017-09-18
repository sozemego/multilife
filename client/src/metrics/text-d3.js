/**
 * Wraps a simple d3 functionality, which displays only text.
 */
export const textD3 = (dom, textFunction) => {
	if(!(dom instanceof Node)) {
		throw new Error("Needs an instance of Node.");
	}
	if(typeof textFunction !== "function") {
		throw new Error("textFunction needs to be a function.");
	}

	const textD3 = {};

	textD3.update = data => {
		const text = d3.select(dom)
			.selectAll("p")
			.data([data], data => Math.random()); //almost always updates to new values

		text.exit().remove();

		text.enter()
			.insert("p", ":first-child")
			.text(textFunction);
	};

	return textD3;
};