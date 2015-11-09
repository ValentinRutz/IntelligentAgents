	package model;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import logist.simulation.Vehicle;

public class Solution {
	private Map<Integer, List<ActionWrapper>> solution;
	private Map<ActionWrapper, Integer> time;
	private Map<ActionWrapper, Vehicle> vehicle;
	
	public Solution() {
		setSolution(new HashMap<Integer, List<ActionWrapper>>());
		setTime(new HashMap<ActionWrapper, Integer>());
		setVehicle(new HashMap<ActionWrapper, Vehicle>());
	}
	
	public Solution(Solution otherSolution) {
		for (Map.Entry<Integer, List<ActionWrapper>> entry : otherSolution.entrySet()) {
			List<ActionWrapper> tmpL = new LinkedList<ActionWrapper>();
			Map<Integer, ActionWrapper> added = new HashMap<Integer, ActionWrapper>();
			for (ActionWrapper aw : entry.getValue()) {
				aw.copy(tmpL, added);
			}
			solution.put(entry.getKey(), tmpL);
		}
	}

	public Map<Integer, List<ActionWrapper>> getSolution() {
		return solution;
	}

	public void setSolution(Map<Integer, List<ActionWrapper>> solution) {
		this.solution = solution;
	}
	
	private Set<Map.Entry<Integer, List<ActionWrapper>>> entrySet() {
		return solution.entrySet();
	}

	public void put(int key, List<ActionWrapper> value) {
		solution.put(key, value);
	}

	public List<ActionWrapper> get(Integer id) {
		return solution.get(id);
	}

	public int remainingCapacity(Vehicle v) throws NegativeCapacityException {
		int remainingCapacity = v.capacity();
		List<ActionWrapper> tasks = get(v.id());
		for (ActionWrapper aw : tasks) {
			remainingCapacity -= aw.getWeight();
			if (remainingCapacity < 0) {
				throw new NegativeCapacityException();
			}
		}
		return remainingCapacity;
	}

	public int getTime(ActionWrapper aw) {
		return time.get(aw).intValue();
	}
	
	public Map<ActionWrapper, Integer> getTime() {
		return time;
	}

	public void setTime(Map<ActionWrapper, Integer> time) {
		this.time = time;
	}
	
	public Vehicle getVehicle(ActionWrapper aw) {
		return vehicle.get(aw);
	}

	public Map<ActionWrapper, Vehicle> getVehicle() {
		return vehicle;
	}

	public void setVehicle(Map<ActionWrapper, Vehicle> vehicle) {
		this.vehicle = vehicle;
	}
}
