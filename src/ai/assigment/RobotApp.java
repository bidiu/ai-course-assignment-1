package ai.assigment;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

/**
 * TODO state should take direction into account
 * 
 * @author sunhe
 * @date Sep 22, 2016
 */
public class RobotApp {
	
	// constants for status of each position in the grid
	public static final int CLEAN = 0;
	public static final int OBSTACLE = 1;
	public static final int DIRTY = 2;
	
	// constants for movement energy costs
	public static final int COST_SUCK = 10;
	public static final int COST_RIGHT = 20;
	public static final int COST_LEFT = 20;
	public static final int COST_FORWARD = 50;
	
	// just store some inputs temporarily
	private Pos robotInitPos;			// robot's initial position
	private Set<Pos> dirtInitSet;		// a set of initial positions of dirt
	private int robotInitDir;			// robot's initial direction
	
	/**
	 * update the current state (several positions of dirt) to grid
	 */
	private void updateGrid(State state, int[][] grid) {
		for (int x = 1; x < grid.length; x++) {
			for (int y = 1; y < grid.length; y++) {
				Pos position = new Pos(x, y);
				if (state.getDirtSet().contains(position)) {
					// current position should be dirty
					grid[x][y] = DIRTY;
				}
				else {
					// current position should be clean
					grid[x][y] = CLEAN;
				}
			}
		}
	}
	
	/**
	 * trace back the path along the state chain
	 */
	private List<State> traceBackPath(State finalState) {
		// TODO
		return null;
	}
	
	/**
	 * @param gridSize
	 * @param robotPos
	 * 		robot's initial position
	 * @param obstacleList
	 * 		a list of positions of obstacles
	 * @param dirtList
	 * 		a list of positions of dirt
	 * @param direction
	 * 		robot's initial direction
	 * @return
	 * 		generated grid
	 */
	public int[][] generateGrid(int gridSize, Pos robotPos, List<Pos> obstacleList, List<Pos> dirtList, int direction) {
		this.robotInitPos = robotPos;
		this.robotInitDir = direction;
		dirtInitSet = new HashSet<Pos>(dirtList);
		
		int[][] grid = new int[gridSize + 1][gridSize + 1];
		for (Pos pos : obstacleList) {
			grid[pos.x][pos.y] = OBSTACLE;
		}
		for (Pos pos : dirtList) {
			grid[pos.x][pos.y] = DIRTY;
		}
		return grid;
	}
	
	public void search(int algorithm, int[][] grid) {
		try {
			if (algorithm == 1) {
				// go DFS
				dfs(algorithm, grid);
			}
			else if (algorithm == 2) {
				// go BFS
				bfs(algorithm, grid);
			}
			else if (algorithm == 3) {
				// TODO
			}
			else {
				throw new IllegalArgumentException();
			}
		}
		catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}
	
	private void dfs(int algorithm, int[][] grid) {
		
	}
	
	private List<State> bfs(int algorithm, int[][] grid) throws CloneNotSupportedException {
		// instantiate the initial state
		State curState = new State(robotInitPos, dirtInitSet, robotInitDir, 0, null);
		if (curState.getDirtSet().isEmpty()) {
			// the initial state is the final one
			return traceBackPath(curState);
		}
		
		Queue<State> fringe = new LinkedList<State>();
		fringe.offer(curState);
		Set<State> closed = new HashSet<State>();
		
		while ((curState = fringe.poll()) != null) {
			closed.add(curState);
			updateGrid(curState, grid);
			Pos curRobotPos = curState.getRobotPos();
			
			if (grid[curRobotPos.x][curRobotPos.y] == DIRTY) {
				// should SUCK, create a new state
				State nextState = (State) curState.clone();
				nextState.getDirtSet().remove(curRobotPos);
				nextState.setCost(nextState.getCost() + COST_SUCK);
				nextState.setParentState(curState);
				
				if (!fringe.contains(nextState) && !closed.contains(nextState)) {
					// performance tweak for BFS, checking whether reach the goal after each SUCK
					if (nextState.getDirtSet().isEmpty()) {
						return traceBackPath(nextState);
					}
					else {
						fringe.offer(nextState);
					}
				}
			}
			/*
			 * ask teacher here, is BFS really blind about which choice is better?
			 * If is, then the code following should be out of the else block.
			 */
			else {
				// should GO AROUND
				for (Pos neighbor : curRobotPos.get4Neighbors()) {
					if (neighbor.x < 1 || neighbor.x > grid.length || 
							neighbor.y < 1 || neighbor.y > grid.length || 
							grid[neighbor.x][neighbor.y] == OBSTACLE) {
						continue;
					}
					State nextState = (State) curState.clone();
					
				}
			}
		}
		throw new IllegalStateException("Not possible");
	}
	
	public void printSolution(List<State> path) {
		// TODO
	}
	
	public static void main(String[] args) {
		
	}
	
}
