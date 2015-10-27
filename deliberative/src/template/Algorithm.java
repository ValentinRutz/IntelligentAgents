package template;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

import logist.plan.Plan;
import logist.simulation.Vehicle;
import logist.task.Task;
import logist.task.TaskSet;
import logist.topology.Topology.City;

// All algorithms should be implemented here. Add your algorithm to the enum as well
enum Algorithm {
	BFS, ASTAR, NAIVE;

	// Given algorithm. Kinda stupid but works.
	static Plan naivePlan(Vehicle vehicle, TaskSet tasks) {
		City current = vehicle.getCurrentCity();
		Plan plan = new Plan(current);

		for (Task task : tasks) {
			// move: current city => pickup location
			for (City city : current.pathTo(task.pickupCity))
				plan.appendMove(city);

			plan.appendPickup(task);

			// move: pickup location => delivery location
			for (City city : task.path())
				plan.appendMove(city);

			plan.appendDelivery(task);

			// set current city
			current = task.deliveryCity;
		}
		return plan;
	}

	// BFS search for best plan
	static Plan bfs(Vehicle v, TaskSet tasks) {
		System.out.println("Beginning of plan computation with BFS");

		Map<State, Path> exploredStates = new HashMap<State, Path>();
		TaskSet initialCarriedTasks = (v.getCurrentTasks() == null) ? TaskSet.create(new Task[0]) : v.getCurrentTasks();
		State current = new BFSState(v.getCurrentCity(), tasks, initialCarriedTasks, v.capacity());

		Queue<State> queue = new LinkedList<State>();
		queue.add(current);
		Path p = new Path();
		exploredStates.put(current, p);

		while (!queue.isEmpty()) {
			current = queue.poll();
			Path parentsPath = exploredStates.get(current);

			// Final State since we use BFS, we arrived with the minimal number
			// of actions to that state so we can directly return
			if (current.isFinalState()) {
				System.out.println(parentsPath.actions);
				System.out.println(parentsPath.actions.size());
				System.out.println("End of plan computation with BFS");
				return new Plan(v.getCurrentCity(), parentsPath.actions).seal();

				// Not in a final state.
			} else {
				State newState = null;
				Path newPath = null;
				List<City> pathTo = null;
				Set<City> relevantNeighbors = new HashSet<City>();

				// Move to all neighbors that are relevant
				for (Task t : current.remainingTasks) {
					newState = null;
					newPath = new Path(parentsPath);
					pathTo = current.city.pathTo(t.pickupCity);
					// Keep that neighbor in mind
					if (!current.city.equals(t.pickupCity) && current.city.hasNeighbor(pathTo.get(0)))
						relevantNeighbors.add(pathTo.get(0));
					// Pickup all the tasks
					else if (current.canPickup(t)) {
						newState = current.pickup(t, newPath);
						if (!(newState == null || exploredStates.containsKey(newState))) {
							exploredStates.put(newState, newPath);
							queue.add(newState);
						}
					}
				}

				for (Task t : current.carriedTasks) {
					newState = null;
					newPath = new Path(parentsPath);
					pathTo = current.city.pathTo(t.deliveryCity);
					if (!current.city.equals(t.deliveryCity) && current.city.hasNeighbor(pathTo.get(0)))
						relevantNeighbors.add(pathTo.get(0));
					
					// Deliver all the tasks
					else if (current.canDeliver(t)) {
						newState = current.deliver(t, newPath);
						if (!(newState == null || exploredStates.containsKey(newState))) {
							exploredStates.put(newState, newPath);
							queue.add(newState);
						}
					}
				}

				for (City neighbor : relevantNeighbors) {
					newPath = new Path(parentsPath);
					newState = current.move(neighbor, newPath);
					if (!(newState == null || exploredStates.containsKey(newState))) {
						exploredStates.put(newState, newPath);
						queue.add(newState);
					}
					newState = null;
				}
			}
		}

		// Never gonna happen.
		return null;
	}

	static Plan astar(Vehicle v, TaskSet tasks) {
		Comparator<AStarState> comparator = new StateComparator();
		PriorityQueue<AStarState> opened = new PriorityQueue<AStarState>(10, comparator);
		Set<State> closed = new HashSet<State>();

		// List<Action> l = new ArrayList<Action>();
		TaskSet initialCarriedTasks = (v.getCurrentTasks() == null) ? TaskSet.create(new Task[0]) : v.getCurrentTasks();
		AStarState current = new AStarState(v.getCurrentCity(), tasks, initialCarriedTasks, v.capacity());
		current.sethValue();
		opened.add(current);

		while (!opened.peek().isFinalState()) {
			current = opened.poll();
			closed.add(current);
			State neighbor = null;
			for (Task t : current.getAllTasks()) {
				/*
				 * if (current.canPickup(t)) { neighbor = current.pickup(t); }
				 * else if (current.canDeliver(t)) { neighbor =
				 * current.deliver(t); }
				 */
				if (opened.contains(neighbor)) {

				}

			}

		}

		// OPEN = priority queue containing START
		// CLOSED = empty set
		// while lowest rank in OPEN is not the GOAL:
		// current = remove lowest rank item from OPEN
		// add current to CLOSED
		// for neighbors of current:
		// cost = g(current) + movementcost(current, neighbor)
		// if neighbor in OPEN and cost less than g(neighbor):
		// remove neighbor from OPEN, because new path is better
		// if neighbor in CLOSED and cost less than g(neighbor): **
		// remove neighbor from CLOSED
		// if neighbor not in OPEN and neighbor not in CLOSED:
		// set g(neighbor) to cost
		// add neighbor to OPEN
		// set priority queue rank to g(neighbor) + h(neighbor)
		// set neighbor's parent to current
		//
		// reconstruct reverse path from goal to start
		// by following parent pointers
		//
		return null;
	}
}
