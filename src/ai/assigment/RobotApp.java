package ai.assigment;

import static java.lang.Math.abs;
import static java.lang.Math.max;
import static java.lang.Math.min;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Scanner;
import java.util.Set;

/**
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
		if (!direction.equals(Pos.NORTH) && !direction.equals(Pos.EAST) 
				&& !direction.equals(Pos.SOUTH) && !direction.equals(Pos.WEST)) {
			throw new IllegalArgumentException("invalid direction, see Pos.NORTH, etc");
		}
		
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
		return moveCost + max(turnCostX, turnCostY) 
				+ state.getDirtSet().size() * COST_SUCK;
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
			
			// action: SUCK
			if (grid[curRobotPos.y][curRobotPos.x] == DIRTY) {
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
			
			// action: SUCK
			if (grid[curRobotPos.y][curRobotPos.x] == DIRTY) {
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
		System.out.println("Depth: " + path.size());
		System.out.println("Time: " + (finalState.getTimestamp() - initState.getTimestamp()) + " ms");
	}
	
	/**
	 * Compiled and tested on Java 8.
	 * 
	 * @author sunhe, myan
	 * @date Sep 22, 2016
	 */
	public static void main(String[] args) {
		Scanner scanner = new Scanner(System.in);

		System.out.println("Please give the size of the grid.");
		int gridsize = scanner.nextInt();
		
		System.out.println("Please give the initial position of the robot by giving x first then y.");
		int x = scanner.nextInt();
		int y = scanner.nextInt();
		System.out.println("Please give the initial direction of the robot (exact string: \"North\", \"East\", \"South\", or \"West\")");
		scanner.nextLine();
		String direction = scanner.nextLine();
		if (!direction.equals(Pos.NORTH) && !direction.equals(Pos.EAST) 
				&& !direction.equals(Pos.SOUTH) && !direction.equals(Pos.WEST)) {
			System.err.println("Invalid direction: " + direction);
			scanner.close();
			return;
		}
		
        List<Pos> obstacleList = new ArrayList<Pos>();
		System.out.println("Please give me the number of obstacle(s).");
		int numOfObstacles = scanner.nextInt();
		System.out.println("Please give successively the positions of the obstacle(s) by giving first x then y.");
		for (int i = 1; i <= numOfObstacles; i++) {
			obstacleList.add(new Pos(scanner.nextInt(), scanner.nextInt()));
		}
		
		List<Pos> dirtList = new ArrayList<Pos>();
		System.out.println("Please give the initial number of dirt(s).");
		int numOfdirt = scanner.nextInt();
		System.out.println("Please give successively the positions of the dirt(s) by giving first x then y.");
		for (int i = 1; i <= numOfdirt; i++) {
			dirtList.add(new Pos(scanner.nextInt(), scanner.nextInt()));
		}
		
		RobotApp app = new RobotApp();
		// here, generate grid
		int[][] grid = app.generateGrid(gridsize, new Pos(x, y), obstacleList, dirtList, direction);
		System.out.println("\nThe grid looks like: ");
		for (int i = 1; i <= gridsize; i++) {
			for (int j = 1; j <= gridsize; j++) {
				if (i == y && j == x) {
					System.out.print(direction.charAt(0));
					continue;
				}
				switch (grid[i][j]) {
				case CLEAN:
					System.out.print(".");
					break;
				case DIRTY:
					System.out.print("@");
					break;
				case OBSTACLE:
					System.out.print("#");
				}
			}
			System.out.println();
		}
		System.out.println("\n'N', 'E','S', or 'W' is robot's direction; '#' is obstacle; '@' is dirty.\n");
		
		// here, start searching
		List<State> path = app.search(2, grid);
		app.printSolution(path);
		
		scanner.close();
	}
	
}
