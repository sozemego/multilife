export default class Rules {

  constructor() {
	this.table = {};
	this.constructRuleTable();
  }

  constructRuleTable() {
	let {table} = this;
	table["BASIC"] = this.createRule("B3/S23");
	table["HIGHLIFE"] = this.createRule("B36/S23");
	table["LIFE_WITHOUT_DEATH"] = this.createRule("B3/S0123456789");
	table["DIAMOEBA"] = this.createRule("B35678/S5678");
	table["MORLEY"] = this.createRule("B368/S245");
	table["LIFE_34"] = this.createRule("B34/S34");
	table["CORAL"] = this.createRule("B3/S45678");
	table["SEEDS"] = this.createRule("B2/S");
	table["ANNEAL"] = this.createRule("B4678/S3567");
	table["FOUR"] = this.createRule("B36/S125");
	table["NO_NAME"] = this.createRule("B25/S4");
	table["REPLICATOR"] = this.createRule("B1357/S1357");
  }

  getRule(name) {
	return this.table[name];
  }

  createRule(ruleString) {
	let tokens = ruleString.split("/");
	let birthNumbers = this.extractNumbers(tokens[0]);
	let surviveNumbers = this.extractNumbers(tokens[1]);


	return function (n, alive) {
	  if (alive) {
		if (!surviveNumbers.includes(n)) return -1;
	  } else {
		if (birthNumbers.includes(n)) return 1;
	  }
	  return 0;
	}
  }

  extractNumbers(token) {
	let numbers = [];
	let result = token.match(/\d/g);
	if (result) {
	  for (let i = 0; i < result.length; i++) {
		numbers.push(parseInt(result[i]));
	  }
	}
	return numbers;
  }

}