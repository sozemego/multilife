/**
 * Created by KJurek on 27.02.2017.
 */

class Rules {

    constructor() {
        this.table = {};
        this.constructRuleTable();
    }

    constructRuleTable() {
        this.table["BASIC"] = this.createRule("B3/S23");
        this.table["HIGHLIFE"] = this.createRule("B36/S23");
        this.table["LIFE_WITHOUT_DEATH"] = this.createRule("B3/S0123456789");
        this.table["DIAMOEBA"] = this.createRule("B35678/S5678");
        this.table["MORLEY"] = this.createRule("B368/S245");
        this.table["LIFE_34"] = this.createRule("B34/S34");
        this.table["CORAL"] = this.createRule("B3/S45678");
        this.table["SEEDS"] = this.createRule("B2/S");
        this.table["ANNEAL"] = this.createRule("B4678/S3567");
        this.table["FOUR"] = this.createRule("B36/S125");
        this.table["NO_NAME"] = this.createRule("B25/S4");
        this.table["REPLICATOR"] = this.createRule("B1357/S1357");
    }

    getRule(name) {
        return this.table[name];
    }

    createRule(ruleString) {
        let tokens = ruleString.split("/");
        let birthNumbers = this.extractNumbers(tokens[0]);
        let surviveNumbers = this.extractNumbers(tokens[1]);


        return function(n, alive) {
            if(alive) {
                if(!surviveNumbers.includes(n)) return -1;
            } else {
                if(birthNumbers.includes(n)) return 1;
            }
            return 0;
        }
    }

    extractNumbers(token) {
        let numbers = [];
        let result = token.match(/\d/g);
        if(result) {
            for(let i = 0; i < result.length; i++) {
                numbers.push(parseInt(result[i]));
            }
        }
        return numbers;
    }

}