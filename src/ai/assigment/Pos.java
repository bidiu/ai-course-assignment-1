package ai.assigment;

import java.util.ArrayList;
import java.util.List;

/**
 * the position, i.e., coordinate - (x, y)
 */
class Pos implements Cloneable {
	
	// constants for directions
	public static final int NORTH = 0;
	public static final int EAST = 1;
	public static final int SOUTH = 2;
	public static final int WEST = 3;
	
	int x;
	int y;
	
	public Pos(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	public int getNeighborDirection(Pos neighbor) {
		if (x == neighbor.x) {
			if (y + 1 == neighbor.y) {
				return EAST;
			}
			else if (y - 1 == neighbor.y) {
				return WEST;
			}
			else {
				throw new IllegalArgumentException("they are not neighbors");
			}
		}
		else if (y == neighbor.y) {
			if (x + 1 == neighbor.x) {
				return SOUTH;
			}
			else if (x - 1 == neighbor.x) {
				return NORTH;
			}
			else {
				throw new IllegalArgumentException("they are not neighbors");
			}
		}
		else {
			throw new IllegalArgumentException("they are not neighbors");
		}
	}
	
	public Pos getNorthNeighbor() {
		return new Pos(x - 1, y);
	}
	public Pos getSouthNeighbor() {
		return new Pos(x + 1, y);
	}
	public Pos getWestNeighbor() {
		return new Pos(x, y - 1);
	}
	public Pos getEastNeighbor() {
		return new Pos(x, y + 1);
	}
	public List<Pos> get4Neighbors() {
		List<Pos> neighbors = new ArrayList<Pos>();
		neighbors.add(getNorthNeighbor());
		neighbors.add(getSouthNeighbor());
		neighbors.add(getWestNeighbor());
		neighbors.add(getEastNeighbor());
		return neighbors;
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
	
	@Override
	protected Object clone() throws CloneNotSupportedException {
		return new Pos(x, y);
	}
	
}
