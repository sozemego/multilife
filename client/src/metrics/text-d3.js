/**
 * Wraps a simple d3 functionality, which displays only text.
 */
import {assertInstanceOf, assertIsFunction} from '../assert';

export const textD3 = (dom, textFunction) => {
  assertInstanceOf(dom, Node);
  assertIsFunction(textFunction);

  const textD3 = {};

  textD3.update = data => {
    const text = d3.select(dom)
      .selectAll('p')
      .data([data], data => Math.random()); //almost always updates to new values

    text.exit().remove();

    text.enter()
      .insert('p', ':first-child')
      .text(textFunction);
  };

  return textD3;
};