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
class Path{
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



class State {
	private City city;

	private TaskSet remainingTasks;
	private TaskSet carriedTasks;
	private int capacity;
	
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

	private double hValue;

	public double gethValue() {
		return hValue;
	}

	public void sethValue() {
		//set F to the cost of the currently most expensive task
		double maxCost = 0;
		
		for(Task t : remainingTasks){
			double currCost = city.distanceTo(t.pickupCity);
			currCost += t.pickupCity.distanceTo(t.deliveryCity);
			if(currCost > maxCost) 
				maxCost = currCost;
			
		}
		
		for(Task t : carriedTasks){
			double currCost = city.distanceTo(t.deliveryCity);
			if(currCost > maxCost) 
				maxCost = currCost;
			
		}
		
		this.hValue = maxCost;
		
	}

	public State(City city, TaskSet remainingTasks, TaskSet carriedTasks,
			int capacity) {
		assert (remainingTasks != null && carriedTasks != null && capacity >= 0);
		this.city = city;
	//	this.actions = new ArrayList<Action>(actions.size());
	//	this.actions.addAll(actions);
	//	this.cost = cost;
		this.remainingTasks = copyOf(remainingTasks);
		this.carriedTasks = copyOf(carriedTasks);
		this.capacity = capacity;
	}




	City getCity() {
		return city;
	}

/*	boolean isBetterFinalState(double bestCost) {
		return remainingTasks.isEmpty() && carriedTasks.isEmpty() && cost <= bestCost;
	}
*/	
	boolean isFinalState() {
		return remainingTasks.isEmpty() && carriedTasks.isEmpty();
	}


	TaskSet getAllTasks() {
		return union(remainingTasks, carriedTasks);
	}

	State pickup(Task t,Path p) {
		State next = clone();
		next.move(t.pickupCity,p);
		p.actions.add(new Pickup(t));
		next.carriedTasks.add(t);
		next.remainingTasks.remove(t);
		next.capacity -= t.weight;
		return next;
	}

	boolean canPickup(Task t) {
		return remainingTasks.contains(t) && capacity - t.weight >= 0;
	}

	State deliver(Task t, Path p) {
		assert (carriedTasks.contains(t) && !remainingTasks.contains(t));
		State next = clone();
		next.move(t.deliveryCity,p);
		p.actions.add(new Delivery(t));
		next.carriedTasks.remove(t);
		assert (!(carriedTasks.contains(t) || remainingTasks.contains(t)));
		next.capacity += t.weight;
		return next;
	}

	boolean canDeliver(Task t) {
		return carriedTasks.contains(t);
	}

	public State clone() {
		return new State(city, remainingTasks, carriedTasks, capacity);
	}

	private State move(City endCity, Path p) {
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

	
	// TODO Auto-generated methods
	
		
	
}



class StateComparator implements Comparator<State>
{
	@Override
	public int compare(State o1, State o2) {
		// TODO Auto-generated method stub
		
		
		return (int)(o1.gethValue() - o2.gethValue());
	}
}
