package ai.assigment;

import java.util.HashSet;
import java.util.Set;

/**
 * state representation
 */
class State implements Cloneable {
	
	private Pos robotPos;			// current position of the robot
	private Set<Pos> dirtSet;		// a set of positions of dirt not yet been cleaned
	
	// following doesn't belong to the state representation!
	private int direction;			// current robot's direction
	private int cost;				// current accumulative cost
	private State parentState;
	
	public State() {
	}
	
	public State(Pos robotPos, Set<Pos> dirtSet, int direction, int cost, State parentState) {
		this.robotPos = robotPos;
		this.dirtSet = dirtSet;
		this.direction = direction;
		this.cost = cost;
		this.parentState = parentState;
	}
	
	// getters and setters
	public Pos getRobotPos() {
		return robotPos;
	}
	public void setRobotPos(Pos robotPos) {
		this.robotPos = robotPos;
	}
	public Set<Pos> getDirtSet() {
		return dirtSet;
	}
	public void setDirtSet(Set<Pos> dirtSet) {
		this.dirtSet = dirtSet;
	}
	public int getDirection() {
		return direction;
	}
	public void setDirection(int direction) {
		this.direction = direction;
	}
	public int getCost() {
		return cost;
	}
	public void setCost(int cost) {
		this.cost = cost;
	}
	public State getParentState() {
		return parentState;
	}
	public void setParentState(State parentState) {
		this.parentState = parentState;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((dirtSet == null) ? 0 : dirtSet.hashCode());
		result = prime * result + ((robotPos == null) ? 0 : robotPos.hashCode());
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
		if (!(obj instanceof State)) {
			return false;
		}
		State other = (State) obj;
		if (dirtSet == null) {
			if (other.dirtSet != null) {
				return false;
			}
		}
		else if (!dirtSet.equals(other.dirtSet)) {
			return false;
		}
		if (robotPos == null) {
			if (other.robotPos != null) {
				return false;
			}
		}
		else if (!robotPos.equals(other.robotPos)) {
			return false;
		}
		return true;
	}
	
	/*
	 * This is a deep copy except for attribute "parentState" being null.
	 */
	@Override
	protected Object clone() throws CloneNotSupportedException {
		State cloned = new State();
		cloned.setRobotPos((Pos) robotPos.clone());
		
		Set<Pos> clonedDirtSet = new HashSet<Pos>();
		for (Pos dirtPos : dirtSet) {
			clonedDirtSet.add((Pos) dirtPos.clone());
		}
		cloned.setDirtSet(clonedDirtSet);
		
		cloned.setDirection(direction);
		cloned.setCost(cost);
		cloned.setParentState(null);
		return clone();
	}
	
}
