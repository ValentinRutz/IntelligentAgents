package template;

import static logist.task.TaskSet.union;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import logist.plan.Action;
import logist.plan.Plan;
import logist.task.Task;
import logist.task.TaskSet;
import logist.topology.Topology.City;

class State {
	private City city;
	private Plan plan;
	private int cost;
	
	private TaskSet remainingTasks;
	private TaskSet carriedTasks;
	private int capacity;
	
	public State(City city, Plan plan, int cost, TaskSet remainingTasks, TaskSet carriedTasks, int capacity) {
		this.city = city;
		Iterator<Action> it = plan.iterator();
		List<Action> actions = new ArrayList<Action>();
		while(it.hasNext()) {
			actions.add(it.next());
		}
		this.plan = new Plan(city, actions);
		this.cost = cost;
		this.remainingTasks = remainingTasks.clone();
		this.carriedTasks = carriedTasks.clone();
		this.capacity = capacity;
	}

	Plan getPlan() {
		return plan;
	}
	
	int getCost() {
		return cost;
	}
	
	City getCity() {
		return city;
	}

	boolean isFinalState(int bestCost) {
		return remainingTasks.isEmpty() && carriedTasks.isEmpty() && cost < bestCost;
	}
	
	TaskSet getAllTasks() {
		return union(remainingTasks, carriedTasks);
	}

	State pickup(Task t) {
		State next = clone();
		next.plan.appendPickup(t);
		next.carriedTasks.add(t);
		next.remainingTasks.remove(t);
		next.capacity -= t.weight;
		next.cost += t.pathLength();
		return next;
	}

	boolean couldPickup(Task t) {
		return t.deliveryCity.equals(city) && capacity - t.weight >= 0;
	}
	
	State deliver(Task t) {
		State next = clone();
		for (City c : next.city.pathTo(t.deliveryCity)) {
			next.plan.appendMove(c);
		}
		next.plan.appendDelivery(t);
		next.carriedTasks.remove(t);
		next.capacity += t.weight;
		next.city = t.deliveryCity;
		return null;
	}
	
	boolean couldDeliver(Task t) {
		return carriedTasks.contains(t);
	}

	public State clone() {
		return new State(city, plan, cost, remainingTasks, carriedTasks, capacity);
	}
}
