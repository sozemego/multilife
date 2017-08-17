package soze.multilife.game;

/**
 * A class representing one cell in the simulation.
 */
public class Cell {

	private final int x;
	private final int y;
	private boolean alive = false;
	private int ownerId = 0;

	public Cell(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public void setAlive(boolean alive) {
		this.alive = alive;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public boolean isAlive() {
		return alive;
	}

	public void setIsAlive(boolean alive) {
		this.alive = alive;
	}

	public int getOwnerId() {
		return ownerId;
	}

	public void setOwnerId(int ownerId) {
		this.ownerId = ownerId;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Cell cell = (Cell) o;

		if (x != cell.x) return false;
		return y == cell.y;
	}

	@Override
	public int hashCode() {
		int result = x;
		result = 31 * result + y;
		return result;
	}

}
