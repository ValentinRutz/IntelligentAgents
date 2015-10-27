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
		TaskSet initialCarriedTasks = (v.getCurrentTasks() == null) ? TaskSet
				.create(new Task[0]) : v.getCurrentTasks();
		State current = new BFSState(v.getCurrentCity(), tasks,
				initialCarriedTasks, v.capacity());

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

				System.out.println("BFS Explored states "
						+ exploredStates.size());
				System.out.println("BFS opened states "
						+ (exploredStates.size()-queue.size()));
				System.out.println("Plan size "+parentsPath.actions.size());
				// System.out.println(parentsPath.actions);
				// System.out.println(parentsPath.actions.size());
				// System.out.println("End of plan computation with BFS");
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
					if (!current.city.equals(t.pickupCity)
							&& current.city.hasNeighbor(pathTo.get(0)))
						relevantNeighbors.add(pathTo.get(0));
					// Pickup all the tasks
					else if (current.canPickup(t)) {
						newState = current.pickup(t, newPath);
						if (!(newState == null || exploredStates
								.containsKey(newState))) {
							exploredStates.put(newState, newPath);
							queue.add(newState);
						}
					}
				}

				for (Task t : current.carriedTasks) {
					newState = null;
					newPath = new Path(parentsPath);
					pathTo = current.city.pathTo(t.deliveryCity);
					if (!current.city.equals(t.deliveryCity)
							&& current.city.hasNeighbor(pathTo.get(0)))
						relevantNeighbors.add(pathTo.get(0));

					// Deliver all the tasks
					else if (current.canDeliver(t)) {
						newState = current.deliver(t, newPath);
						if (!(newState == null || exploredStates
								.containsKey(newState))) {
							exploredStates.put(newState, newPath);
							queue.add(newState);
						}
					}
				}

				for (City neighbor : relevantNeighbors) {
					newPath = new Path(parentsPath);
					newState = current.move(neighbor, newPath);
					if (!(newState == null || exploredStates
							.containsKey(newState))) {
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
		PriorityQueue<AStarState> opened = new PriorityQueue<AStarState>(10,
				comparator);
		Set<AStarState> closed = new HashSet<AStarState>();

		// List<Action> l = new ArrayList<Action>();
		TaskSet initialCarriedTasks = (v.getCurrentTasks() == null) ? TaskSet
				.create(new Task[0]) : v.getCurrentTasks();

		AStarState current = new AStarState(v.getCurrentCity(), tasks,
				initialCarriedTasks, v.capacity());
		opened.add(current);

		Map<State, Path> exploredStates = new HashMap<State, Path>();
		exploredStates.put(current, new Path());

		Path bestPath = null;
		int counter = 0;
		while (!opened.isEmpty()) {
			current = opened.poll();

			if (current.isFinalState()) {
				bestPath = new Path(exploredStates.get(current));
				break;

			} else {

				closed.add(current);

				for (Task t : current.getAllTasks()) {
					AStarState neighbor = null;

					Path parentPath = new Path(exploredStates.get(current));

					if (current.canPickup(t)) {

						// inside pickUp we update the parent's path
						// movementcost(current, neighbor)
						neighbor = current.pickup(t, parentPath);
					} else if (current.canDeliver(t)) {
						neighbor = current.deliver(t, parentPath);
					}

					if (!closed.contains(neighbor)) {

						boolean add=true;
						if (exploredStates.containsKey(neighbor)) {
							// we have arrived in a previously visited state,
							// but
							// this time with lower cost
							add=false;
							if (exploredStates.get(neighbor).cost > parentPath.cost) {

								// update the g cost with the new one (better
								// one)
								exploredStates.put(neighbor, parentPath);

//								if (closed.contains(neighbor)) {
//									counter++;
//									// System.out.println("HERE!!");
//									closed.remove(neighbor);
//								}
								
								
								if (!closed.contains(neighbor)) {
									opened.remove(neighbor);
								}
								add=true;
							}

						}
						
						
						if (neighbor != null && add ) {
							exploredStates.put(neighbor, parentPath);
							double fValue = parentPath.cost
									+ neighbor.gethValue();
							neighbor.setfValue(fValue);
							opened.add(neighbor);

						}

					}

				}
			}

		}

		if (counter > 0) {
			System.out.println("Number of here's " + counter);
		}
		System.out.println("A-Star explored states " + exploredStates.size());
		System.out.println("A-star closed states " + closed.size());
		System.out.println("Plan size " + bestPath.actions.size());

		return new Plan(v.getCurrentCity(), bestPath.actions).seal();

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
	}
}
