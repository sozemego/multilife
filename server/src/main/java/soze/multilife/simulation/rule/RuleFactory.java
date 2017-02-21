package soze.multilife.simulation.rule;

/**
 * Created by soze on 2/21/2017.
 */
public class RuleFactory {

	private static final Rule basicRule = new BasicRule();
	private static final Rule highlifeRule = new HighlifeRule();

	public static Rule getRule(RuleType type) {
		if(type == RuleType.BASIC) {
			return basicRule;
		}
		if(type == RuleType.HIGHLIFE) {
			return highlifeRule;
		}
		return basicRule;
	}

}
