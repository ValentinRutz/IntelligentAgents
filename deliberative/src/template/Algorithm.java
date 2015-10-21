package template;

import java.util.LinkedList;
import java.util.Queue;

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
		System.out.println("Beginning of plan computation");
		State current = new State(v.getCurrentCity(), new Plan(v.getCurrentCity()), 0, tasks, v.getCurrentTasks(),
				v.capacity());

		Queue<State> queue = new LinkedList<State>();
		queue.add(current);

		Plan bestPlan = current.getPlan();
		int bestCost = current.getCost();

		while (!queue.isEmpty()) {
			current = queue.poll();
			assert (current != null);

			// Final State
			// Compare current plan with best plan (compare costs)
			// Keep only the best one
			if (current.isFinalState(bestCost)) {
				System.out.println("Arrived at a final state!");
				bestPlan = current.getPlan();
				bestCost = current.getCost();

				// Not in a final state. Try to act on all tasks
			} else {
				for (Task t : current.getAllTasks()) {
					if (current.couldPickup(t)) {
						queue.add(current.pickup(t));
					} else if (current.couldDeliver(t)) {
						queue.add(current.deliver(t));
					}
				}
			}
		}
		System.out.println("End of plan computation");
		return bestPlan.seal();
	}

	static Plan astar(Vehicle v, TaskSet tasks) {
		return null;
	}
}
