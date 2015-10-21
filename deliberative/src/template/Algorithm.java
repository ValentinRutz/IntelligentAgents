package template;

import java.util.LinkedList;
import java.util.Queue;

import logist.plan.Plan;
import logist.simulation.Vehicle;
import logist.task.Task;
import logist.task.TaskSet;
import logist.topology.Topology.City;

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
		State current = new State(v.getCurrentCity(), new Plan(v.getCurrentCity()), Integer.MAX_VALUE, tasks,
				TaskSet.create(new Task[0]), v.capacity());

		Queue<State> queue = new LinkedList<>();
		queue.add(current);
		
		Plan bestPlan = current.getPlan();
		int bestCost = current.getCost();

		while (!queue.isEmpty()) {
			current = queue.poll();

			// Final State
			// Compare current plan with best plan (compare costs)
			// Keep only the best one
			if (current.isFinalState(bestCost)) {
				bestPlan = current.getPlan();
				bestCost = current.getCost();

				// Not in a final state. Need to try all kinds of combinations
				// of possible transitions.
			} else {
				// Need to be able to mix pickups and deliveries. Break?
				// Need to remove picked up task from remainingTasks too
				for (Task t : current.getAllTasks()) {
					if (current.couldPickup(t)) {
						queue.add(current.pickup(t));
					} else if (current.couldDeliver(t)) {
						queue.add(current.deliver(t));
					}
				}
			}
		}

		return bestPlan.seal();
	}

	static Plan astar(Vehicle v, TaskSet tasks) {
		return null;
	}
}
