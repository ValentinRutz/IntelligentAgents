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
	}
	
	public ActionWrapper(ActionWrapper aw) {
		ID = aw.getID();
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
	
	public abstract boolean checkTime(Solution s);

	public int getWeight() {
		return weight;
	}

	public void setWeight(int weight) {
		this.weight = weight;
	}
	
	public abstract ActionWrapper getPickup();
	public abstract ActionWrapper getDelivery();

	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ID;
		result = prime * result + ((this==getPickup()) ? 0 : 1);
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
		ActionWrapper other = (ActionWrapper) obj;
		if (ID != other.ID)
			return false;

		return (this==getPickup() && other==other.getPickup()) || (this==getDelivery() && other==other.getDelivery());
	}
}