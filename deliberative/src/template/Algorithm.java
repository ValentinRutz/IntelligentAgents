package template;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import logist.plan.Action;
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
		List<Action> l = new ArrayList<Action>();
		Set<State> cycle = new HashSet<State>();
		TaskSet initialCarriedTasks = (v.getCurrentTasks() == null) ? TaskSet.create(new Task[0]) : v.getCurrentTasks();
		State current = new State(v.getCurrentCity(), l, 0, tasks, initialCarriedTasks, v.capacity());

		Queue<State> queue = new LinkedList<State>();
		queue.add(current);

		List<Action> bestPlan = new ArrayList<Action>();
		int bestCost = Integer.MAX_VALUE;

		while (!queue.isEmpty()) {
			current = queue.poll();

			// Final State
			// Compare current plan with best plan (compare costs)
			// Keep only the best one
			if (current.isBetterFinalState(bestCost)) {
				bestPlan = current.getActions();
				bestCost = current.getCost();

				// Not in a final state. Try to act on all tasks
			} else if (!cycle.contains(current)) {
				State newState = null;
				for (Task t : current.getAllTasks()) {
					if (current.canPickup(t)) {
						newState = current.pickup(t);
					} else if (current.canDeliver(t)) {
						newState = current.deliver(t);
					}
					queue.add(newState);
					cycle.add(current);
				}
			}
		}
		System.out.println(bestPlan);
		assert (!bestPlan.isEmpty());
		System.out.println("End of plan computation");
		return new Plan(v.getCurrentCity(), bestPlan).seal();
	}

	static Plan astar(Vehicle v, TaskSet tasks) {
		return null;
	}
}
