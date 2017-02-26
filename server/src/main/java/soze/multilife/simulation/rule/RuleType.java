package soze.multilife.simulation.rule;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Contains all available types of rules for game of life.
 * New rules can be added using by adding an enum and providing
 * an appropriate String, the rule will be parsed automatically.
 */
public enum RuleType {

  BASIC("B3/S23"),
  HIGHLIFE("B36/S23"),
  LIFE_WITHOUT_DEATH("B3/S0123456789"),
  DIAMOEBA("B35678/S5678"),
  MORLEY("B368/S245"),
  LIFE_34("B34/S34"),
  CORAL("B3/S45678 "),
  SEEDS("B2/S"),
  ANNEAL("B4678/S3567"),
  FOUR("B36/S125"),
  NO_NAME("B25/S4"),
  REPLICATOR("B1357/S1357");

  private final Rule rule;

  RuleType(String ruleString) {
	this.rule = parseRuleString(ruleString);
  }

  /**
   * Parses a game of life rule string.
   * Those rules should be in the form of B[0-9]{1,9}/S[0-9]{1,9}.
   * Basically, digits after B mean number of alive neighbours that cause a dead
   * cell to come alive. Digits after S mean a number of alive neighbours that
   * let a cell stay alive (it dies otherwise). This method returns an
   * anonymous object, using a lambda to create it.
   *
   * @param ruleString
   * @return
   */
  private Rule parseRuleString(String ruleString) {
	String[] tokens = ruleString.split("/");
	Set<Integer> birthNumbers = extractNumbers(tokens[0]);
	Set<Integer> surviveNumbers = extractNumbers(tokens[1]);

	return (n, state) -> {
	  if (state) {
		if (!surviveNumbers.contains(n)) return -1;
	  } else {
		if (birthNumbers.contains(n)) return 1;
	  }
	  return 0;
	};
  }

  /**
   * Extracts all digits from a given text
   * and returns a List of those numbers (Integer).
   *
   * @param text
   * @return
   */
  private Set<Integer> extractNumbers(String text) {
	Pattern pattern = Pattern.compile("\\d");
	Matcher matcher = pattern.matcher(text);
	Set<Integer> numbers = new HashSet<>();
	while (matcher.find()) {
	  String group = matcher.group();
	  numbers.add(Integer.parseInt(group));
	}
	return numbers;
  }

  public Rule getRule() {
	return rule;
  }

}
