package ai.assigment;

/**
 * the position, or say, coordinate, i.e., (x, y)
 */
public class Pos implements Cloneable {
	
	public static final String NORTH = "North";
	public static final String EAST = "East";
	public static final String SOUTH = "South";
	public static final String WEST = "West";
	
	int x;
	int y;
	
	public Pos(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	/**
	 * get an adjacent neighbor of a specific direction
	 */
	public Pos getNeighbor(String direction) {
		switch (direction) {
		case NORTH:
			return new Pos(x, y - 1);
		case EAST:
			return new Pos(x + 1, y);
		case SOUTH:
			return new Pos(x, y + 1);
		case WEST:
			return new Pos(x - 1, y);
		default:
			throw new IllegalArgumentException();
		}
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + x;
		result = prime * result + y;
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Pos)) {
			return false;
		}
		Pos other = (Pos) obj;
		if (x != other.x) {
			return false;
		}
		if (y != other.y) {
			return false;
		}
		return true;
	}
	
	// for print
	@Override
	public String toString() {
		return "pos(" + x + ", " + y + ")";
	}
	
}
