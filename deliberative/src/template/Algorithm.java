package template;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
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
		double bestCost = Integer.MAX_VALUE;

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
		 Comparator<State> comparator = new StateComparator();
	        PriorityQueue<State> opened = 
	            new PriorityQueue<State>(10, comparator);
	        Set<State> closed = new HashSet<State>();
	        
	    	List<Action> l = new ArrayList<Action>();
			TaskSet initialCarriedTasks = (v.getCurrentTasks() == null) ? TaskSet.create(new Task[0]) : v.getCurrentTasks();
			State current = new State(v.getCurrentCity(), l, 0, tasks, initialCarriedTasks, v.capacity());
			current.sethValue();
			opened.add(current);
			
			while( !opened.peek().isFinalState()){
				current = opened.poll();
				closed.add(current);
				State neighbor=null;
				for (Task t : current.getAllTasks()) {
					if (current.canPickup(t)) {
						neighbor = current.pickup(t);
					} else if (current.canDeliver(t)) {
						neighbor = current.deliver(t);
					}
					
					if(opened.contains(neighbor)){
						
					}
					
				}
				
				
				
			}
	        
//	        OPEN = priority queue containing START
//	        		CLOSED = empty set
//	        		while lowest rank in OPEN is not the GOAL:
//	        		  current = remove lowest rank item from OPEN
//	        		  add current to CLOSED
//	        		  for neighbors of current:
//	        		    cost = g(current) + movementcost(current, neighbor)
//	        		    if neighbor in OPEN and cost less than g(neighbor):
//	        		      remove neighbor from OPEN, because new path is better
//	        		    if neighbor in CLOSED and cost less than g(neighbor): **
//	        		      remove neighbor from CLOSED
//	        		    if neighbor not in OPEN and neighbor not in CLOSED:
//	        		      set g(neighbor) to cost
//	        		      add neighbor to OPEN
//	        		      set priority queue rank to g(neighbor) + h(neighbor)
//	        		      set neighbor's parent to current
//
//	        		reconstruct reverse path from goal to start
//	        		by following parent pointers
//	     
		return null;
	}
}
