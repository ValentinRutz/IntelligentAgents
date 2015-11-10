package model;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import logist.simulation.Vehicle;
import logist.topology.Topology.City;

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
		setSolution(new HashMap<Integer, List<ActionWrapper>>());
		for (Map.Entry<Integer, List<ActionWrapper>> entry : otherSolution.entrySet()) {
			List<ActionWrapper> tmpL = new LinkedList<ActionWrapper>();
			Map<Integer, ActionWrapper> added = new HashMap<Integer, ActionWrapper>();

			for (ActionWrapper aw : entry.getValue()) {
				aw.copy(tmpL, added);
			}
			solution.put(entry.getKey(), tmpL);
		}

		setTime(new HashMap<ActionWrapper, Integer>());
		for (Map.Entry<ActionWrapper, Integer> entry : otherSolution.time.entrySet()) {
			time.put(entry.getKey(), entry.getValue().intValue());
		}

		setVehicle(new HashMap<ActionWrapper, Vehicle>());
		vehicle.putAll(otherSolution.vehicle);
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

	public int remainingCapacity(Vehicle v, List<ActionWrapper> tasks) {

		int remainingCapacity = v.capacity();
		for (ActionWrapper aw : tasks) {
			remainingCapacity -= aw.getWeight();
			if (remainingCapacity < 0) {
				break;
			} else if (remainingCapacity > v.capacity()) {
				return -1;
			}
		}
		return remainingCapacity;
	}

	public int getTime(ActionWrapper aw) {
		return time.get(aw).intValue();
	}

	public void putTime(ActionWrapper aw, int newTime) {
		time.put(aw, newTime);
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

	public void putVehicle(ActionWrapper aw, Vehicle v) {
		vehicle.put(aw, v);
	}

	public Map<ActionWrapper, Vehicle> getVehicle() {
		return vehicle;
	}

	public void setVehicle(Map<ActionWrapper, Vehicle> vehicle) {
		this.vehicle = vehicle;
	}

	public boolean changeTasksOrder(int vehicleID, int firstTaskInd, int secondTaskInd) {
		List<ActionWrapper> oldList = solution.get(vehicleID);

		List<ActionWrapper> newList = new LinkedList<ActionWrapper>();

		ActionWrapper t1 = oldList.get(firstTaskInd);
		ActionWrapper t2 = oldList.get(secondTaskInd);

		int i = 0;
		for (Iterator<ActionWrapper> iterator = oldList.iterator(); iterator.hasNext();) {
			ActionWrapper actionWrapper = (ActionWrapper) iterator.next();
			if (i == firstTaskInd) {
				newList.add(t2);
			} else if (i == secondTaskInd) {
				newList.add(t1);
			} else {
				newList.add(actionWrapper);
			}

			i++;
		}

		time.put(t1, secondTaskInd + 1);
		time.put(t2, firstTaskInd + 1);
		solution.put(vehicleID, newList);

		if (remainingCapacity(vehicle.get(t1), newList) < 0
				|| !Constraints.testPickupBeforeDelivery(t1, this)
				|| !Constraints.testPickupBeforeDelivery(t2, this)) {
			time.put(t1, firstTaskInd + 1);
			time.put(t2, secondTaskInd + 1);
			solution.put(vehicleID, oldList);
			return false;
		}

		return true;
	}

	public double cost() {

		double cost = 0;
		for (List<ActionWrapper> entry : solution.values()) {
			if (!entry.isEmpty()) {
				ActionWrapper firstTask = entry.get(0);
				double costPerKm = vehicle.get(firstTask).costPerKm();
				City current = vehicle.get(firstTask).getCurrentCity();
				City next = firstTask.getCity();
				double kms = current.distanceTo(next);

				for (ActionWrapper actionWrapper : entry) {
					current = next;
					next = actionWrapper.getCity();
					kms += current.distanceTo(next);
				}

				cost += kms * costPerKm;
			}
		}

		return cost;
	}
}
