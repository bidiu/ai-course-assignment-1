package ai.assigment;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.Math.abs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

/**
 * TODO depth
 * TODO consistent!!!
 * TODO variable for storing initial state
 * TODO is it okay to tweak unblinded search? 
 * 
 * @author sunhe, myan
 * @date Sep 22, 2016
 */
public class RobotApp {
	
	public static final int COST_SUCK = 10;
	public static final int COST_TURN = 20;
	public static final int COST_MOVE = 50;
	
	public static final int CLEAN = 0;
	public static final int DIRTY = 1;
	public static final int OBSTACLE = 2;
	
	// just store the initial state
	private Pos robotInitPos;				// robot's initial position
	private String robotInitDir;			// robot's initial direction
	private Set<Pos> initDirtSet;			// a set of initial positions of dirt
	
	public int[][] generateGrid(int gridSize, Pos robotPos, List<Pos> obstacleList, List<Pos> dirtList, String direction) {
		this.robotInitPos = robotPos;
		this.robotInitDir = direction;
		initDirtSet = new HashSet<Pos>(dirtList);
		
		int[][] grid = new int[gridSize + 1][gridSize + 1];
		for (Pos pos : obstacleList) {
			grid[pos.y][pos.x] = OBSTACLE;
		}
		for (Pos pos : dirtList) {
			grid[pos.y][pos.x] = DIRTY;
		}
		return grid;
	}
	
	/**
	 * Each time, update the current state (only the current positions of dirt) to grid.
	 * Since we have multiple states, but we only use ONE grid.
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
	 * trace back the path of the state chain, from the start to the GOAL
	 */
	private List<State> traceBackPath(State finalState) {
		List<State> path = new LinkedList<State>();
		State s = finalState;
		while (s != null) {
			path.add(s);
			s = s.getParentState();
		}
		Collections.reverse(path);
		return Collections.unmodifiableList(path);
	}
	
	/**
	 * the heuristic function, 
	 * and it should be consistent.
	 */
	private int h(State state) {
		int robotX = state.getRobotPos().x;
		int robotY = state.getRobotPos().y;
		String robotDir = state.getDirection();
		int mostWestIndex = -1, mostEastIndex = -1, mostNorthIndex = -1, mostSouthIndex = -1;
		int moveCost = 0, turnCostX = 0, turnCostY = 0;
		
		for (Pos dirtPos : state.getDirtSet()) {
			if (mostWestIndex == -1 || dirtPos.x < mostWestIndex) {
				mostWestIndex = dirtPos.x;
			}
			if (mostEastIndex == -1 || dirtPos.x > mostEastIndex) {
				mostEastIndex = dirtPos.x;
			}
			if (mostNorthIndex == -1 || dirtPos.y < mostNorthIndex) {
				mostNorthIndex = dirtPos.y;
			}
			if (mostSouthIndex == -1 || dirtPos.y > mostSouthIndex) {
				mostSouthIndex = dirtPos.y;
			}
		}
		
		// think about X axis, move and turn times
		if ((mostWestIndex - robotX) * (mostEastIndex - robotX) >= 0) {
			moveCost += max(abs(mostWestIndex - robotX), abs(mostEastIndex - robotX)) * COST_MOVE;
			if (mostWestIndex != mostEastIndex) {
				if (robotDir.equals(Pos.NORTH) || robotDir.equals(Pos.SOUTH)) {
					turnCostX += 1 * COST_TURN;
				}
				else if (robotDir.equals(Pos.WEST) && mostWestIndex >= robotX 
						|| robotDir.equals(Pos.EAST) && mostEastIndex <= robotX) {
					turnCostX += 2 * COST_TURN;
				}
			}
		}
		else {
			int stepWest = abs(mostWestIndex - robotX);
			int stepEast = abs(mostEastIndex - robotX);
			moveCost += (min(stepWest, stepEast) * 2 + max(stepWest, stepEast)) * COST_MOVE;
			if (robotDir.equals(Pos.NORTH) || robotDir.equals(Pos.SOUTH)) {
				turnCostX += 3 * COST_TURN;
			}
			else {
				turnCostX += 2 * COST_TURN;
			}
		}
		
		// think about Y axis, move and turn times
		if ((mostNorthIndex - robotY) * (mostSouthIndex - robotY) >= 0) {
			moveCost += max(abs(mostNorthIndex - robotY), abs(mostSouthIndex - robotY)) * COST_MOVE;
			if (mostNorthIndex != mostSouthIndex) {
				if (robotDir.equals(Pos.WEST) || robotDir.equals(Pos.EAST)) {
					turnCostY += 1 * COST_TURN;
				}
				else if (robotDir.equals(Pos.NORTH) && mostNorthIndex >= robotY 
						|| robotDir.equals(Pos.SOUTH) && mostSouthIndex <= robotY) {
					turnCostY += 2 * COST_TURN;
				}
			}
		}
		else {
			int stepNorth = abs(mostNorthIndex - robotY);
			int stepSouth = abs(mostSouthIndex - robotY);
			moveCost += (min(stepNorth, stepSouth) * 2 + max(stepNorth, stepSouth)) * COST_MOVE;
			if (robotDir.equals(Pos.WEST) || robotDir.equals(Pos.EAST)) {
				turnCostY += 3 * COST_TURN;
			}
			else {
				turnCostY += 2 * COST_TURN;
			}
		}
		
		// return the estimate cost of cleaning all dirt from the current state
		return moveCost + min(turnCostX, turnCostY) 
				+ state.getDirtSet().size() * COST_SUCK + state.getCost();
	}
	
	/**
	 * the DFS algorithm
	 */
	private List<State> DFS(int algorithm, int[][] grid) throws CloneNotSupportedException {
		// instantiate the initial state
		State curState = new State(robotInitPos, initDirtSet, robotInitDir, 0, State.ACTION_START, null);
		curState.setTimestamp(System.currentTimeMillis());
		if (curState.getDirtSet().isEmpty()) {
			// the initial state is the final one
			return traceBackPath(curState);
		}
		
		LinkedList<State> fringe = new LinkedList<State>();
		fringe.addFirst(curState);
		Set<State> closed = new HashSet<State>();
		
		while ((curState = fringe.removeFirst()) != null) {
			closed.add(curState);
			updateGrid(curState, grid);
			Pos curRobotPos = curState.getRobotPos();
			
			if (grid[curRobotPos.y][curRobotPos.x] == DIRTY) {
				// action: SUCK
				State nextState = (State) curState.clone();
				nextState.getDirtSet().remove(curRobotPos);
				nextState.setCost(nextState.getCost() + COST_SUCK)
						.setActionName(State.ACTION_SUCK)
						.setParentState(curState);
				
				if (!fringe.contains(nextState) && !closed.contains(nextState)) {
					if (nextState.getDirtSet().isEmpty()) {
						// reach the final state
						nextState.setTimestamp(System.currentTimeMillis());
						return traceBackPath(nextState);
					}
					else {
						fringe.addFirst(nextState);
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
							.setCost(nextState.getCost() + COST_MOVE)
							.setActionName(State.ACTION_MOVE)
							.setParentState(curState);
					
					if (!fringe.contains(nextState) && !closed.contains(nextState)) {
						fringe.addFirst(nextState);
					}
				}
				
				// action: RIGHT
				State nextState = (State) curState.clone();
				nextState.turnRight()
						.setCost(nextState.getCost() + COST_TURN)
						.setActionName(State.ACTION_RIGHT)
						.setParentState(curState);
				if (!fringe.contains(nextState) && !closed.contains(nextState)) {
					fringe.addFirst(nextState);
				}
				
				// action: LEFT
				nextState = (State) curState.clone();
				nextState.turnLeft()
						.setCost(nextState.getCost() + COST_TURN)
						.setActionName(State.ACTION_LEFT)
						.setParentState(curState);
				if (!fringe.contains(nextState) && !closed.contains(nextState)) {
					fringe.addFirst(nextState);
				}
			}
		} // end of while
		throw new IllegalStateException("Not possible");
	}
	
	/**
	 * the BFS algorithm
	 */
	private List<State> BFS(int algorithm, int[][] grid) throws CloneNotSupportedException {
		// instantiate the initial state
		State curState = new State(robotInitPos, initDirtSet, robotInitDir, 0, State.ACTION_START, null);
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
				nextState.setCost(nextState.getCost() + COST_SUCK)
						.setActionName(State.ACTION_SUCK)
						.setParentState(curState);
				
				if (!fringe.contains(nextState) && !closed.contains(nextState)) {
					if (nextState.getDirtSet().isEmpty()) {
						// reach the final state
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
							.setCost(nextState.getCost() + COST_MOVE)
							.setActionName(State.ACTION_MOVE)
							.setParentState(curState);
					
					if (!fringe.contains(nextState) && !closed.contains(nextState)) {
						fringe.offer(nextState);
					}
				}
				
				// action: RIGHT
				State nextState = (State) curState.clone();
				nextState.turnRight()
						.setCost(nextState.getCost() + COST_TURN)
						.setActionName(State.ACTION_RIGHT)
						.setParentState(curState);
				if (!fringe.contains(nextState) && !closed.contains(nextState)) {
					fringe.offer(nextState);
				}
				
				// action: LEFT
				nextState = (State) curState.clone();
				nextState.turnLeft()
						.setCost(nextState.getCost() + COST_TURN)
						.setActionName(State.ACTION_LEFT)
						.setParentState(curState);
				if (!fringe.contains(nextState) && !closed.contains(nextState)) {
					fringe.offer(nextState);
				}
			}
		} // end of while
		throw new IllegalStateException("Not possible");
	}
	
	/**
	 * the A* algorithm
	 */
	public List<State> AStar(int algorithm, int[][] grid) throws CloneNotSupportedException {
		// instantiate the initial state
		State curState = new State(robotInitPos, initDirtSet, robotInitDir, 0, State.ACTION_START, null);
		curState.setTimestamp(System.currentTimeMillis());
		curState.setF(0 + h(curState));
		if (curState.getDirtSet().isEmpty()) {
			// the initial state is the final one
			return traceBackPath(curState);
		}
		
		Queue<State> fringe = new PriorityQueue<State>(new Comparator<State>() {
			
			public int compare(State state1, State state2) {
				return state1.getF() - state2.getF();
			};
		});
		fringe.offer(curState);
		Set<State> closed = new HashSet<State>();
		
		while ((curState = fringe.poll()) != null) {
			closed.add(curState);
			updateGrid(curState, grid);
			Pos curRobotPos = curState.getRobotPos();
			
			// action: SUCK
			if (grid[curRobotPos.y][curRobotPos.x] == DIRTY) {
				State nextState = (State) curState.clone();
				nextState.getDirtSet().remove(curRobotPos);
				nextState.setCost(nextState.getCost() + COST_SUCK)
						.setActionName(State.ACTION_SUCK)
						.setParentState(curState)
						.setF(nextState.getCost() + h(nextState));
				
				if (!fringe.contains(nextState) && !closed.contains(nextState)) {
					if (nextState.getDirtSet().isEmpty()) {
						// reach the final state
						nextState.setTimestamp(System.currentTimeMillis());
						return traceBackPath(nextState);
					}
					else {
						fringe.offer(nextState);
					}
				}
			}

			// action: MOVE
			Pos nextRobotPos = curRobotPos.getNeighbor(curState.getDirection());
			if (nextRobotPos.x >= 1 && nextRobotPos.x < grid.length 
					&& nextRobotPos.y >= 1 && nextRobotPos.y < grid.length 
					&& grid[nextRobotPos.y][nextRobotPos.x] != OBSTACLE) {
				State nextState = (State) curState.clone();
				nextState.setRobotPos(nextRobotPos)
						.setCost(nextState.getCost() + COST_MOVE)
						.setActionName(State.ACTION_MOVE)
						.setParentState(curState)
						.setF(nextState.getCost() + h(nextState));
				
				if (!fringe.contains(nextState) && !closed.contains(nextState)) {
					fringe.offer(nextState);
				}
			}
			
			// action: RIGHT
			State nextState = (State) curState.clone();
			nextState.turnRight()
					.setCost(nextState.getCost() + COST_TURN)
					.setActionName(State.ACTION_RIGHT)
					.setParentState(curState)
					.setF(nextState.getCost() + h(nextState));
			if (!fringe.contains(nextState) && !closed.contains(nextState)) {
				fringe.offer(nextState);
			}
			
			// action: LEFT
			nextState = (State) curState.clone();
			nextState.turnLeft()
					.setCost(nextState.getCost() + COST_TURN)
					.setActionName(State.ACTION_LEFT)
					.setParentState(curState)
					.setF(nextState.getCost() + h(nextState));
			if (!fringe.contains(nextState) && !closed.contains(nextState)) {
				fringe.offer(nextState);
			}
		}
		throw new IllegalStateException("Not possible");
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
				return AStar(algorithm, grid);
			}
			else {
				throw new IllegalArgumentException();
			}
		}
		catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
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
	
	/**
	 * Compiled and tested on Java 8.
	 * 
	 * @author sunhe, myan
	 * @date Sep 22, 2016
	 */
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
		List<State> path = app.search(3, grid);
		app.printSolution(path);
	}
	
}
