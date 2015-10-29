package template;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
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
						+ (exploredStates.size() - queue.size()));
				System.out.println("Plan size " + parentsPath.actions.size());
				// System.out.println(parentsPath.actions);
				// System.out.println(parentsPath.actions.size());
				// System.out.println("End of plan computation with BFS");
				return new Plan(v.getCurrentCity(), parentsPath.actions).seal();

				// Not in a final state.
			} else {
				State newState = null;
				Path newPath = null;
				
				// Move to all neighbors
				for (City neighbor : current.city.neighbors()) {
					newPath = new Path(parentsPath);
					newState = current.move(neighbor, newPath);
					if (!exploredStates.containsKey(newState)) {
						exploredStates.put(newState, newPath);
						queue.add(newState);
					}
				}
				
				// Pickup all the tasks
				// Deliver all the tasks
				for (Task t : current.getAllTasks()) {
					newState = null;
					newPath = new Path(parentsPath);
					if (current.canPickup(t)) {
						newState = current.pickup(t, newPath);
					} else if (current.canDeliver(t)) {
						newState = current.deliver(t, newPath);
					}
					
					if (newState != null && !exploredStates.containsKey(newState)) {
						exploredStates.put(newState, newPath);
						queue.add(newState);
					}
				}
			}
		}

		// Never gonna happen.
		return null;
	}

	static Plan astar(Vehicle v, TaskSet tasks) {

		Comparator<AStarState> comparator = new StateComparator();
		PriorityQueue<AStarState> opened = new PriorityQueue<AStarState>(50000,
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
			// System.out.println("Plan Length " +
			// exploredStates.get(current).actions.size());

			if (current.isFinalState()) {
				bestPath = new Path(exploredStates.get(current));
				break;

			} else {

				closed.add(current);

				AStarState neighbor=null;
				Path newPath = null;
				for (Task t : current.getAllTasks()) {
					neighbor = null;

					newPath = new Path(exploredStates.get(current));

					if (current.canPickup(t)) {

						// inside pickUp we update the parent's path
						// movementcost(current, neighbor)
						neighbor = current.pickup(t, newPath);
					} else if (current.canDeliver(t)) {
						neighbor = current.deliver(t, newPath);
					}

					if (!closed.contains(neighbor)) {

						boolean add = true;
						if (exploredStates.containsKey(neighbor)) {
							// we have arrived in a previously visited state,
							// but
							// this time with lower cost
							add = false;
							if (exploredStates.get(neighbor).cost > newPath.cost) {

								// update the g cost with the new one (better
								// one)
								exploredStates.put(neighbor, newPath);

								// if (closed.contains(neighbor)) {
								// counter++;
								// // System.out.println("HERE!!");
								// closed.remove(neighbor);
								// }

								opened.remove(neighbor);

								add = true;
							}

						}

						if (neighbor != null && add) {
							exploredStates.put(neighbor, newPath);
							double fValue = newPath.cost
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

	}
}
