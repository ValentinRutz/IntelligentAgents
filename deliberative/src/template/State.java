package template;

import static logist.task.TaskSet.union;
import static logist.task.TaskSet.copyOf;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import logist.plan.Action;
import logist.plan.Plan;
import logist.task.Task;
import logist.task.TaskSet;
import logist.topology.Topology.City;

// Describes a state and makes the transitions if they are possible
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
		while (it.hasNext()) {
			actions.add(it.next());
		}
		this.plan = new Plan(city, actions);
		this.cost = cost;
		this.remainingTasks = copyOf(remainingTasks);
		this.carriedTasks = copyOf(carriedTasks);
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

	Task getRndTask() {
		Object[] arrayOfTasks = getAllTasks().toArray();
		assert (arrayOfTasks.length != 0);
		Random rnd = new Random();
		int idxTaskToGet = rnd.nextInt(arrayOfTasks.length);
		return (Task) arrayOfTasks[idxTaskToGet];
	}

	State pickup(Task t) {
		State next = clone();
		next = next.move(t.pickupCity);
		next.plan.appendPickup(t);
		next.carriedTasks.add(t);
		next.remainingTasks.remove(t);
		next.capacity -= t.weight;
		return next;
	}

	boolean couldPickup(Task t) {
		return remainingTasks.contains(t) && capacity - t.weight >= 0;
	}

	State deliver(Task t) {
		State next = clone();
		next = next.move(t.deliveryCity);
		next.plan.appendDelivery(t);
		next.carriedTasks.remove(t);
		next.capacity += t.weight;
		return next;
	}

	boolean couldDeliver(Task t) {
		return carriedTasks.contains(t);
	}

	public State clone() {
		return new State(city, plan, cost, remainingTasks, carriedTasks, capacity);
	}

	State move(City pickupCity) {
		if(!pickupCity.equals(city)) {
			State next = clone();
			City currentCity = next.city;
			for (City c : city.pathTo(pickupCity)) {
				next.plan.appendMove(c);
				next.cost += currentCity.distanceTo(c);
				currentCity = c;
			}
			next.city = pickupCity;
			return next;
		}
		return this;
	}
}
