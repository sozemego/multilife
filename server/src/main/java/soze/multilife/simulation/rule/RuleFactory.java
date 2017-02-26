package soze.multilife.simulation.rule;

import java.util.Objects;

/**
 * An object containing static methods to retrieve game of life rules.
 */
public class RuleFactory {

  /**
   * Returns a Rule object for a given RuleType.
   *
   * @param type
   * @return
   */
  public static Rule getRule(RuleType type) {
	return Objects.requireNonNull(type).getRule();
  }

  /**
   * Finds an appropriate RuleType and returns a Rule
   * associated with it.
   *
   * @param rule
   * @return
   */
  public static Rule getRule(String rule) {
	return getRule(RuleType.valueOf(rule.toUpperCase()));
  }

}
