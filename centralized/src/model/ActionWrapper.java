package model;

import java.util.List;
import java.util.Map;

import logist.plan.Action;
import logist.task.Task;
import logist.topology.Topology.City;

public abstract class ActionWrapper {
	private final int ID;
	private int weight;
	private City city;
	private Action action;
	
	private ActionWrapper counterpart;
	
	public ActionWrapper(Task t) {
		ID = t.id;
		weight = t.weight;
	}
	
	public ActionWrapper(ActionWrapper aw) {
		ID = aw.getID();
		weight = aw.getWeight();
	}
	
	public int getWeight() {
		return weight;
	}

	public int getID() {
		return ID;
	}

	public City getCity() {
		return city;
	}

	public void setCity(City city) {
		this.city = city;
	}

	public Action getAction() {
		return action;
	}

	public void setAction(Action action) {
		this.action = action;
	}

	public ActionWrapper getCounterpart() {
		return counterpart;
	}

	public void setCounterpart(ActionWrapper counterpart) {
		this.counterpart = counterpart;
	}
	
	public abstract void copy(List<ActionWrapper> l, Map<Integer, ActionWrapper> added);
}