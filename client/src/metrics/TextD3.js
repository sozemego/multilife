/**
 * Wraps a simple d3 functionality, which displays only text.
 */
export default class TextD3 {

	constructor(dom, textFunction) {
		this._dom = dom;
		this._textFunction = textFunction;
	}

	update = (data) => {
		let text = d3.select(this._dom)
			.selectAll("p")
			.data([data], data => Math.random()); //almost always updates to new values

		text.exit().remove();

		text.enter()
			.append("p")
			.text(this._textFunction);

	};

}