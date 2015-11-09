package model;

import java.util.List;

import logist.simulation.Vehicle;
import logist.task.Task;
import logist.task.TaskSet;

public class Constraints {

	public static boolean testCapacitySolution(int biggestCapacity, TaskSet tasks) {
		for (Task task : tasks) {
			if (task.weight > biggestCapacity)
				return false;
		}
		return true;
	}

	public static boolean testCapacity(Vehicle v, Task t, Solution s) {
		try {
			return v.capacity() - s.remainingCapacity(v) - t.weight >= 0;
		} catch (NegativeCapacityException e) {
			return false;
		}
	}

	public static boolean testPickupBeforeDelivery(ActionWrapper pw, Solution s) {
		ActionWrapper dw = pw.getCounterpart();
		return s.getTime(pw) < s.getTime(dw) && s.getVehicle(pw).id() == s.getVehicle(dw).id();
	}

	public static boolean testFirstTaskHasTimeOneAndVehicleIsConsistent(int vehicleID, Solution s) {
		ActionWrapper a = s.get(vehicleID).get(0);
		return s.getTime(a) == 1 && s.getVehicle(a).id() == vehicleID;
	}

	public static boolean testNextActionHasConsitentTimeAndVehicle(ActionWrapper aw, Solution s) {
		int vID = s.getVehicle(aw).id();
		List<ActionWrapper> l = s.get(vID);
		int idx = l.indexOf(aw);
		
		if (idx != -1) {
			ActionWrapper next = l.get(idx + 1);
			return s.getTime(aw) + 1 == s.getTime(next);
		}
		return false;
	}

	public static boolean terminationCondition() {
		return false;
	}
}
