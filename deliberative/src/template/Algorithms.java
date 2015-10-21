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
	
	static Plan bfs(Vehicle v, TaskSet tasks) {
		City curr;
		TaskSet remainingTasks = tasks.clone();
		TaskSet carriedTasks = TaskSet.create(new Task[0]);
		int remainingCapacity = v.capacity();
		Queue<City> queue = new LinkedList<>();
		queue.add(v.getCurrentCity());
		Queue<Queue<Action>> plans = new LinkedList<Queue<Action>>();
		Queue<Action> bestPlan = new LinkedList<>();
		int bestCost = Integer.MAX_VALUE;
		plans.add(new LinkedList<Action>());
		
		while(!queue.isEmpty()) {
			curr = queue.poll();
			for (Task t : tasks) {
				if(t.pickupCity.equals(curr)) {
					
					
				}
			}
			
			for (Task t : carriedTasks) {
				
			}
		}
		
		
		return new Plan(v.getCurrentCity(), new ArrayList<Action>(bestPlan));
	}
	
	static Plan astar(Vehicle v, TaskSet tasks) {
		return null;
	}

}
