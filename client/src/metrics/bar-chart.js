import {findMaxNumber, generateRandomColors} from '../utils';
import {assertIsObject} from '../assert';

const transformTypeCountToArray = typeCount => {
	assertIsObject(typeCount);
	const arr = [];
	for (const key in typeCount) {
		arr.push({type: key, count: typeCount[key]});
	}
	return arr;
};

export const barChart = dom => {
	assertIsObject(dom);

	const barChart = {};

	const getMaxWidth = () => {
		return dom.offsetWidth * 0.9;
	};

	barChart.update = data => {
		assertIsObject(data);
		const transformedData = transformTypeCountToArray(data);
		const max = findMaxNumber(transformedData.map(d => d.count));
		const length = d3.scaleLinear().domain([0, max]).range([0, getMaxWidth()]);

		const colors = d3.scaleOrdinal(generateRandomColors(10));

		const chart = d3.select(dom)
			.selectAll('div')
			.data(transformedData, d => d.type);

		chart
			.style('margin', '2px')
			.style('font-size', '1em')
			.style('height', '40px')
			.style('line-height', '40px')
			.style('border-style', 'solid')
			.text(d => {
				return d.type + '(' + d.count + ')';
			})
			.transition()
			.duration(500)
			.style('width', d => length(d.count) + 'px');

		chart.enter()
			.append('div')
			.style('background-color', d => colors(d.type))
			.style('width', 25 + 'px')
			.transition()
			.duration(500)
			.style('width', d => length(d.count) + 'px');

		chart.exit().remove();
	};

	return barChart;
};