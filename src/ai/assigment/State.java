package ai.assigment;

import java.util.HashSet;
import java.util.Set;

/**
 * State representation, or say, Node
 */
public class State implements Cloneable {
	
	// for print
	public static final String ACTION_START = "start";
	public static final String ACTION_SUCK = "suck";
	public static final String ACTION_MOVE = "move";
	public static final String ACTION_RIGHT = "right";
	public static final String ACTION_LEFT = "left";
	
	private Pos robotPos;				// current position of the robot
	private String direction;			// current robot's direction
	private Set<Pos> dirtSet;			// a set of positions of dirt not yet been cleaned
	
	// note, following doesn't belong to the state representation!
	private int cost;					// current accumulative real cost
	private String actionName;			// just for print
	private State parentState;
	private long timestamp;				// only initial and final states have
	private int f;						// f(n) = g(n) + h(n), n == this
	
	public State() {
	}
	
	public State(Pos robotPos, Set<Pos> dirtSet, String direction, int cost, String actionName, State parentState) {
		this.robotPos = robotPos;
		this.dirtSet = dirtSet;
		this.direction = direction;
		this.cost = cost;
		this.actionName = actionName;
		this.parentState = parentState;
	}
	
	// getters and setters
	public Pos getRobotPos() {
		return robotPos;
	}
	public State setRobotPos(Pos robotPos) {
		this.robotPos = robotPos;
		return this;
	}
	public Set<Pos> getDirtSet() {
		return dirtSet;
	}
	public State setDirtSet(Set<Pos> dirtSet) {
		this.dirtSet = dirtSet;
		return this;
	}
	public String getDirection() {
		return direction;
	}
	public State setDirection(String direction) {
		this.direction = direction;
		return this;
	}
	public int getCost() {
		return cost;
	}
	public State setCost(int cost) {
		this.cost = cost;
		return this;
	}
	public String getActionName() {
		return actionName;
	}
	public State setActionName(String actionName) {
		this.actionName = actionName;
		return this;
	}
	public State getParentState() {
		return parentState;
	}
	public State setParentState(State parentState) {
		this.parentState = parentState;
		return this;
	}
	public long getTimestamp() {
		return timestamp;
	}
	public State setTimestamp(long timestamp) {
		this.timestamp = timestamp;
		return this;
	}
	public int getF() {
		return f;
	}
	public void setF(int f) {
		this.f = f;
	}

	public State turnLeft() {
		switch (direction) {
		case Pos.NORTH:
			direction = Pos.WEST;
			break;
		case Pos.EAST:
			direction = Pos.NORTH;
			break;
		case Pos.SOUTH:
			direction = Pos.EAST;
			break;
		case Pos.WEST:
			direction = Pos.SOUTH;
		}
		return this;
	}
	
	public State turnRight() {
		switch (direction) {
		case Pos.NORTH:
			direction = Pos.EAST;
			break;
		case Pos.EAST:
			direction = Pos.SOUTH;
			break;
		case Pos.SOUTH:
			direction = Pos.WEST;
			break;
		case Pos.WEST:
			direction = Pos.NORTH;
		}
		return this;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((direction == null) ? 0 : direction.hashCode());
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
		if (direction == null) {
			if (other.direction != null) {
				return false;
			}
		} else if (!direction.equals(other.direction)) {
			return false;
		}
		if (dirtSet == null) {
			if (other.dirtSet != null) {
				return false;
			}
		} else if (!dirtSet.equals(other.dirtSet)) {
			return false;
		}
		if (robotPos == null) {
			if (other.robotPos != null) {
				return false;
			}
		} else if (!robotPos.equals(other.robotPos)) {
			return false;
		}
		return true;
	}

	/*
	 * This is a deep copy except for fields "actionName", "parentState" 
	 * and "timestamp" being null or 0.
	 */
	@Override
	protected Object clone() throws CloneNotSupportedException {
		State cloned = (State) super.clone();
		cloned.setRobotPos(new Pos(robotPos.x, robotPos.y));
		
		if (dirtSet != null) {
			Set<Pos> clonedDirtSet = new HashSet<Pos>();
			for (Pos dirtPos : dirtSet) {
				clonedDirtSet.add(new Pos(dirtPos.x, dirtPos.y));
			}
			cloned.setDirtSet(clonedDirtSet);
		}
		
		cloned.setActionName(null);
		cloned.setParentState(null);
		cloned.setTimestamp(0L);
		return cloned;
	}
	
	// for print
	@Override
	public String toString() {
		return robotPos + ", " + direction + ", " + actionName;
	}
	
}
