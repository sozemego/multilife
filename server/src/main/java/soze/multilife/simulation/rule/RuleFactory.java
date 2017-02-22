package soze.multilife.simulation.rule;

/**
 * Created by soze on 2/21/2017.
 */
public class RuleFactory {

  private static final Rule basicRule = new BasicRule();
  private static final Rule highlifeRule = new HighlifeRule();
  private static final Rule lifeWithoutDeathRule = new LifeWithoutDeathRule();
  private static final Rule diamoebaRule = new DiamoebaRule();

  public static Rule getRule(RuleType type) {
    if(type == RuleType.BASIC) {
      return basicRule;
    }
    if(type == RuleType.HIGHLIFE) {
      return highlifeRule;
    }
    if(type == RuleType.LIFE_WITHOUT_DEATH) {
      return lifeWithoutDeathRule;
    }
    if(type == RuleType.DIAMOEBA) {
      return diamoebaRule;
    }
    return basicRule;
  }

  public static Rule getRule(String rule) {
    return getRule(RuleType.valueOf(rule.toUpperCase()));
  }

}
