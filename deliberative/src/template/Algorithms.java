package template;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

import logist.plan.Action;
import logist.plan.Plan;
import logist.simulation.Vehicle;
import logist.task.Task;
import logist.task.TaskSet;
import logist.topology.Topology.City;

final class Algorithms {
	
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
		City curr = v.getCurrentCity();
		Queue<Action> currPlan;
		int currCost = Integer.MAX_VALUE;
		
		TaskSet remainingTasks = tasks.clone();
//		int remainingCapacity = v.capacity();
		
		TaskSet carriedTasks = TaskSet.create(new Task[0]);
		
		Queue<City> queue = new LinkedList<>();
		queue.add(v.getCurrentCity());
		
		Queue<Queue<Action>> plans = new LinkedList<Queue<Action>>();
		plans.add(new LinkedList<Action>());
		
		Queue<Action> bestPlan = new LinkedList<>();
		int bestCost = Integer.MAX_VALUE;
		
		
		while(!queue.isEmpty()) {
			curr = queue.poll();
			currPlan = plans.poll();
			
			// Final State
			// Compare current plan with best plan (compare costs)
			// Keep only the best one
			if(remainingTasks.isEmpty() && carriedTasks.isEmpty() && bestCost > currCost) {
				bestPlan = currPlan;
				bestCost = currCost;
				
			// Not in a final state. Need to try all kinds of combinations of possible transitions.	
			} else {
				// Need to be able to mix pickups and deliveries. Break?
				// Need to remove picked up task from remainingTasks too
				for (Task t : remainingTasks) {
					if (t.pickupCity.equals(curr)) {
						
						
					}
				}
				
				for (Task t : carriedTasks) {
					if (t.deliveryCity.equals(curr)) {
						
					}
				}
			}
		}
		
		
		return new Plan(v.getCurrentCity(), new ArrayList<Action>(bestPlan));
	}
	
	static Plan astar(Vehicle v, TaskSet tasks) {
		return null;
	}
}
