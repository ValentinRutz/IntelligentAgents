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
	private double cost = -1;

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
			ActionWrapper actionWrapper = iterator.next();
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
			// Necessary to revert changes??
			time.put(t1, firstTaskInd + 1);
			time.put(t2, secondTaskInd + 1);
			solution.put(vehicleID, oldList);
			return false;
		}

		return true;
	}

	public boolean changeTaskVehicle(Vehicle vehicle0, Vehicle vehicle1, int taskInd) {
		int vehicleID0 = vehicle0.id(), vehicleID1 = vehicle1.id();
		List<ActionWrapper> vehicle0Solution = solution.get(vehicleID0);
		if (vehicle0Solution.size() < 2) {
//			System.out.println("Not enough tasks to transfer");
			return false;
		}
		
		ActionWrapper pw = vehicle0Solution.remove(taskInd).getPickup();
		ActionWrapper dw = pw.getCounterpart();
		if (!vehicle0Solution.remove(dw)) {
			return false;
		}
		
		// Update time in vehicle0Solution
		int i = 1;
		for (ActionWrapper aw : vehicle0Solution) {
			time.put(aw, i++);
		}

		List<ActionWrapper> vehicle1Solution = solution.get(vehicleID1);

		if (!Constraints.testCapacity(vehicle1, pw, this) || !vehicle1Solution.add(pw)) {
//			System.out.println("vehicle "+ vehicleID1 +" cannot add pickup");
			return false;
		}

		if (!Constraints.testCapacity(vehicle1, dw, this) || !vehicle1Solution.add(dw)) {
//			System.out.println("vehicle "+ vehicleID1 +" cannot add delivery");
			return false;
		}

		time.put(pw, vehicle1Solution.size() - 1);
		time.put(dw, vehicle1Solution.size());
		vehicle.put(pw, vehicle1);
		vehicle.put(dw, vehicle1);

		if (!(Constraints.testFirstTaskHasTimeOneAndVehicleIsConsistent(vehicleID1, this)
				&& Constraints.testFirstTaskHasTimeOneAndVehicleIsConsistent(vehicleID0, this)
				&& Constraints.testPickupBeforeDelivery(pw, this))) {
//			System.out.println("Pickup before delivery or time not correctly updated");
//			for (ActionWrapper aw : vehicle1Solution) {
//				System.out.println("Time is " + time.get(aw));
//			}
			return false;
		}

		return true;
	}

	public double cost() {
		if (cost == -1) {
			double cost = 0;
			for (List<ActionWrapper> entry : solution.values()) {
				if (!entry.isEmpty()) {
					ActionWrapper firstTask = entry.get(0);
					double costPerKm = vehicle.get(firstTask).costPerKm();
					City current = vehicle.get(firstTask).getCurrentCity();
					City next = null;
					double kms = 0;
					for (ActionWrapper actionWrapper : entry) {
						next = actionWrapper.getCity();
						kms += current.distanceTo(next);
						current = next;
	
					}
	
					cost += kms * costPerKm;
				}
			}
			this.cost = cost;
		}

		return this.cost;
	}
}
