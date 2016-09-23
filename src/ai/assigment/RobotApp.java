package ai.assigment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

public class RobotApp {
	
	public static final int CLEAN = 0;
	public static final int DIRTY = 1;
	public static final int OBSTACLE = 2;
	
	private Pos robotInitPos;				// robot's initial position
	private Set<Pos> dirtInitSet;			// a set of initial positions of dirt
	private String robotInitDir;			// robot's initial direction
	
	/**
	 * update the current state (only the current positions of dirt) to grid
	 */
	private void updateGrid(State state, int[][] grid) {
		for (int y = 1; y < grid.length; y++) {
			for (int x = 1; x < grid.length; x++) {
				Pos position = new Pos(x, y);
				if (state.getDirtSet().contains(position)) {
					// current position should be dirty
					grid[y][x] = DIRTY;
				}
				else if (grid[y][x] != OBSTACLE) {
					// current position should be clean
					grid[y][x] = CLEAN;
				}
			}
		}
	}
	
	/**
	 * trace back the path along the state chain
	 */
	private List<State> traceBackPath(State finalState) {
		List<State> path = new LinkedList<State>();
		State s = finalState;
		while (s != null) {
			path.add(s);
			s = s.getParentState();
		}
		Collections.reverse(path);
		return path;
	}
	
	public int[][] generateGrid(int gridSize, Pos robotPos, List<Pos> obstacleList, List<Pos> dirtList, String direction) {
		this.robotInitPos = robotPos;
		this.robotInitDir = direction;
		dirtInitSet = new HashSet<Pos>(dirtList);
		
		int[][] grid = new int[gridSize + 1][gridSize + 1];
		for (Pos pos : obstacleList) {
			grid[pos.y][pos.x] = OBSTACLE;
		}
		for (Pos pos : dirtList) {
			grid[pos.y][pos.x] = DIRTY;
		}
		return grid;
	}
	
	public List<State> search(int algorithm, int[][] grid) {
		try {
			if (algorithm == 1) {
				return DFS(algorithm, grid);
			}
			else if (algorithm == 2) {
				return BFS(algorithm, grid);
			}
			else if (algorithm == 3) {
				// TODO
				return null;
			}
			else {
				throw new IllegalArgumentException();
			}
		}
		catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}
	
	private List<State> DFS(int algorithm, int[][] grid) {
		// TODO
		return null;
	}
	
	private List<State> BFS(int algorithm, int[][] grid) throws CloneNotSupportedException {
		// instantiate the initial state
		State curState = new State(robotInitPos, dirtInitSet, robotInitDir, 0, State.ACTION_START, null);
		curState.setTimestamp(System.currentTimeMillis());
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
			
			if (grid[curRobotPos.y][curRobotPos.x] == DIRTY) {
				// action: SUCK
				State nextState = (State) curState.clone();
				nextState.getDirtSet().remove(curRobotPos);
				nextState.setCost(nextState.getCost() + State.COST_SUCK)
						.setActionName(State.ACTION_SUCK)
						.setParentState(curState);
				
				if (!fringe.contains(nextState) && !closed.contains(nextState)) {
					// performance tweak for BFS, checking whether reach the goal after each SUCK
					if (nextState.getDirtSet().isEmpty()) {
						nextState.setTimestamp(System.currentTimeMillis());
						return traceBackPath(nextState);
					}
					else {
						fringe.offer(nextState);
					}
				}
			}
			else {
				// action: MOVE
				Pos nextRobotPos = curRobotPos.getNeighbor(curState.getDirection());
				if (nextRobotPos.x >= 1 && nextRobotPos.x < grid.length 
						&& nextRobotPos.y >= 1 && nextRobotPos.y < grid.length 
						&& grid[nextRobotPos.y][nextRobotPos.x] != OBSTACLE) {
					State nextState = (State) curState.clone();
					nextState.setRobotPos(nextRobotPos)
							.setCost(nextState.getCost() + State.COST_MOVE)
							.setActionName(State.ACTION_MOVE)
							.setParentState(curState);
					
					if (!fringe.contains(nextState) && !closed.contains(nextState)) {
						// we know for sure it's impossible to reach final state currently
						fringe.offer(nextState);
					}
				}
				
				// action: RIGHT
				State nextState = (State) curState.clone();
				nextState.turnRight()
						.setCost(nextState.getCost() + State.COST_RIGHT)
						.setActionName(State.ACTION_RIGHT)
						.setParentState(curState);
				if (!fringe.contains(nextState) && !closed.contains(nextState)) {
					// we know for sure it's impossible to reach final state currently
					fringe.offer(nextState);
				}
				
				// action: LEFT
				nextState = (State) curState.clone();
				nextState.turnLeft()
						.setCost(nextState.getCost() + State.COST_LEFT)
						.setActionName(State.ACTION_LEFT)
						.setParentState(curState);
				if (!fringe.contains(nextState) && !closed.contains(nextState)) {
					// we know for sure it's impossible to reach final state currently
					fringe.offer(nextState);
				}
			}
		} // end of while
		throw new IllegalStateException("Not possible");
	}
	
	public void printSolution(List<State> path) {
		State initState = path.get(0);
		State finalState = path.get(path.size() - 1);
		
		for (State node : path) {
			System.out.println(node);
		}
		System.out.println();
		System.out.println("total cost: " + finalState.getCost());
		System.out.println("Time: " + (finalState.getTimestamp() - initState.getTimestamp()) + " ms");
	}
	
	public static void main(String[] args) throws CloneNotSupportedException {
		List<Pos> obstacleList = new ArrayList<Pos>();
		obstacleList.add(new Pos(2, 2));
		obstacleList.add(new Pos(2, 3));
		obstacleList.add(new Pos(3, 2));
		
		List<Pos> dirtList = new ArrayList<Pos>();
		dirtList.add(new Pos(1, 2));
		dirtList.add(new Pos(2, 1));
		dirtList.add(new Pos(2, 4));
		dirtList.add(new Pos(3, 3));
		
		RobotApp app = new RobotApp();
		int[][] grid = app.generateGrid(4, new Pos(4, 3), obstacleList, dirtList, Pos.WEST);
		List<State> path = app.search(2, grid);
		app.printSolution(path);
	}
	
}
