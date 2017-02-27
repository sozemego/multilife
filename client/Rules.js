/**
 * Created by KJurek on 27.02.2017.
 */

class Rules {

    constructor() {
        this.table = {};
        this.constructRuleTable();
    }

    constructRuleTable() {
        this.table["BASIC"] = this.getBasic;
    }

    getRule(name) {
        return this.table[name];
    }

    getBasic(n, state) {
        if(state) {
            if(n < 2 || n > 3) return -1;
        } else {
            if(n === 3) return 1;
        }
        return 0;
    }


}