package soze.multilife.simulation;

import soze.multilife.simulation.rule.Rule;

/**
 * Created by soze on 2/21/2017.
 */
public class Cell {

	private final int x;
	private final int y;
	private boolean alive = false;
	private final Rule rule;
	private long ownerId = 0;

	public Cell(int x, int y, Rule rule) {
		this.x = x;
		this.y = y;
		this.rule = rule;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public Rule getRule() {
		return rule;
	}

	public boolean isAlive() {
		return alive;
	}

	public void setIsAlive(boolean alive) {
		this.alive = alive;
	}

	public long getOwnerId() {
		return ownerId;
	}

	public void setOwnerId(long ownerId) {
		this.ownerId = ownerId;
	}

}
