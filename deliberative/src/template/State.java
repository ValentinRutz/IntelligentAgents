package template;

import static logist.task.TaskSet.copyOf;
import static logist.task.TaskSet.union;

import java.util.ArrayList;
import java.util.List;

import logist.plan.Action;
import logist.plan.Action.Delivery;
import logist.plan.Action.Move;
import logist.plan.Action.Pickup;
import logist.task.Task;
import logist.task.TaskSet;
import logist.topology.Topology.City;

import java.util.Comparator;

// Describes a state and makes the transitions if they are possible
class Path {
	protected List<Action> actions;
	protected double cost;

	public Path() {
		// TODO Auto-generated constructor stub
		actions = new ArrayList<Action>();
		cost = 0;
	}

	public Path(List<Action> actions, double cost) {
		// TODO Auto-generated constructor stub
		this.actions = new ArrayList<Action>(actions.size());
		this.actions.addAll(actions);
		this.cost = cost;
	}

	public Path(Path p) {
		// TODO Auto-generated constructor stub
		this.actions = new ArrayList<Action>(p.actions.size());
		this.actions.addAll(p.actions);
		this.cost = p.cost;
	}

}

abstract class State {
	protected City city;

	protected TaskSet remainingTasks;
	protected TaskSet carriedTasks;
	protected int capacity;

	public State(City city, TaskSet remainingTasks, TaskSet carriedTasks,
			int capacity) {
		assert (remainingTasks != null && carriedTasks != null && capacity >= 0);
		this.city = city;
		// this.actions = new ArrayList<Action>(actions.size());
		// this.actions.addAll(actions);
		// this.cost = cost;
		this.remainingTasks = copyOf(remainingTasks);
		this.carriedTasks = copyOf(carriedTasks);
		this.capacity = capacity;
	}

	City getCity() {
		return city;
	}

	/*
	 * boolean isBetterFinalState(double bestCost) { return
	 * remainingTasks.isEmpty() && carriedTasks.isEmpty() && cost <= bestCost; }
	 */
	boolean isFinalState() {
		return remainingTasks.isEmpty() && carriedTasks.isEmpty();
	}

	TaskSet getAllTasks() {
		return union(remainingTasks, carriedTasks);
	}

	abstract State pickup(Task t, Path p);

	boolean canPickup(Task t) {
		return remainingTasks.contains(t) && t.pickupCity.equals(city) && capacity - t.weight >= 0;
	}

	abstract State deliver(Task t, Path p);

	boolean canDeliver(Task t) {
		return carriedTasks.contains(t) && t.deliveryCity.equals(city);
	}

	public abstract State clone();

	protected abstract State move(City endCity, Path p);

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		StringBuffer sb = new StringBuffer();
		sb.append(city.toString());
		sb.append(" Carried ");
		for (Task task : carriedTasks) {
			sb.append(task.id);
			sb.append(" ");
		}

		sb.append(" Remaining ");
		for (Task task : remainingTasks) {
			sb.append(task.id);
			sb.append(" ");
		}
		// sb.append(remainingTasks.toString());

		return sb.toString();
	}

	// TODO Auto-generated methods
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((carriedTasks == null) ? 0 : carriedTasks.hashCode());
		result = prime * result + ((city == null) ? 0 : city.hashCode());
		result = prime * result
				+ ((remainingTasks == null) ? 0 : remainingTasks.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		State other = (State) obj;
		if (carriedTasks == null) {
			if (other.carriedTasks != null)
				return false;
		} else if (!carriedTasks.equals(other.carriedTasks))
			return false;
		if (city == null) {
			if (other.city != null)
				return false;
		} else if (!city.equals(other.city))
			return false;
		if (remainingTasks == null) {
			if (other.remainingTasks != null)
				return false;
		} else if (!remainingTasks.equals(other.remainingTasks))
			return false;
		return true;
	}
}

class BFSState extends State {

	public BFSState(City city, TaskSet remainingTasks, TaskSet carriedTasks, int capacity) {
		super(city, remainingTasks, carriedTasks, capacity);
	}

	@Override
	State pickup(Task t, Path p) {
		State next = clone();
		p.actions.add(new Pickup(t));
		next.carriedTasks.add(t);
		next.remainingTasks.remove(t);
		next.capacity -= t.weight;
		return next;
	}

	@Override
	State deliver(Task t, Path p) {
		State next = clone();
		p.actions.add(new Delivery(t));
		next.carriedTasks.remove(t);
		next.capacity += t.weight;
		return next;
	}

	@Override
	public State clone() {
		return new BFSState(city, remainingTasks, carriedTasks, capacity);
	}

	@Override
	protected State move(City neighbor, Path p) {
		State next = clone();
		if (!neighbor.equals(city)) {
			p.actions.add(new Move(neighbor));
			next.city = neighbor;
		}
		return next;
	}
}

class AStarState extends State {
	
	protected double hValue;

	public AStarState(City city, TaskSet remainingTasks, TaskSet carriedTasks, int capacity) {
		super(city, remainingTasks, carriedTasks, capacity);
	}
	
	public double gethValue() {
		return hValue;
	}

	public void sethValue() {
		// set F to the cost of the currently most expensive task
		double maxCost = 0;

		for (Task t : remainingTasks) {
			double currCost = city.distanceTo(t.pickupCity);
			currCost += t.pickupCity.distanceTo(t.deliveryCity);
			if (currCost > maxCost)
				maxCost = currCost;

		}

		for (Task t : carriedTasks) {
			double currCost = city.distanceTo(t.deliveryCity);
			if (currCost > maxCost)
				maxCost = currCost;

		}

		this.hValue = maxCost;

	}

	State pickup(Task t, Path p) {
		State next = clone();
		next.move(t.pickupCity, p);
		p.actions.add(new Pickup(t));
		next.carriedTasks.add(t);
		next.remainingTasks.remove(t);
		next.capacity -= t.weight;
		return next;
	}

	State deliver(Task t, Path p) {
		assert (carriedTasks.contains(t) && !remainingTasks.contains(t));
		State next = clone();
		next.move(t.deliveryCity, p);
		p.actions.add(new Delivery(t));
		next.carriedTasks.remove(t);
		assert (!(carriedTasks.contains(t) || remainingTasks.contains(t)));
		next.capacity += t.weight;
		return next;
	}

	public State clone() {
		return new AStarState(city, remainingTasks, carriedTasks, capacity);
	}

	protected State move(City endCity, Path p) {
		if (!endCity.equals(city)) {
			City currentCity = city;
			for (City c : city.pathTo(endCity)) {
				p.actions.add(new Move(c));
				p.cost += currentCity.distanceTo(c);
				currentCity = c;
			}
			city = endCity;
		}
		return this;
	}
}

class StateComparator implements Comparator<AStarState> {
	@Override
	public int compare(AStarState o1, AStarState o2) {
		if (o1.gethValue() < o2.gethValue()) {
			return -1;
		} else if (o1.gethValue() == o2.gethValue()) {
			return 0;
		} else
			return 1;
	}
}
