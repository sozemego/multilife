package soze.multilife.game.rule;

import java.util.Objects;

/**
 * An object containing static methods to retrieve game of life rules.
 */
public class RuleFactory {

  /**
   * Returns a Rule object for a given RuleType.
   */
  private static Rule getRule(RuleType type) {
    return Objects.requireNonNull(type).getRule();
  }

  /**
   * Finds an appropriate RuleType and returns a Rule
   * associated with it.
   */
  public static Rule getRule(String rule) {
    Objects.requireNonNull(rule);
    return getRule(RuleType.valueOf(rule.toUpperCase()));
  }

}
