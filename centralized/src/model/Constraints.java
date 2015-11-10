package model;

import java.util.List;

import logist.simulation.Vehicle;
import logist.task.Task;
import logist.task.TaskSet;

public class Constraints {

	public static boolean testCapacitySolution(int biggestCapacity,
			TaskSet tasks) {
		for (Task task : tasks) {
			if (task.weight > biggestCapacity)
				return false;
		}
		return true;
	}

	public static boolean testCapacity(Vehicle v, ActionWrapper t, Solution s) {
		int r = s.remainingCapacity(v, s.get(v.id()));

		if (r < 0)
			return false;

		return r - t.getWeight() >= 0;

	}

	public static boolean testPickupBeforeDelivery(ActionWrapper aw, Solution s) {
		return aw.checkTime(s) && s.getVehicle(aw).id() == s.getVehicle(aw.getCounterpart()).id();
	}

	public static boolean testFirstTaskHasTimeOneAndVehicleIsConsistent(int vehicleID, Solution s) {
		if(s.get(vehicleID).size() < 1)
			return true;
		ActionWrapper a = s.get(vehicleID).get(0);
		return s.getTime(a) == 1 && s.getVehicle(a).id() == vehicleID;
	}

	public static boolean testNextActionHasConsitentTimeAndVehicle(
			ActionWrapper aw, Solution s) {
		int vID = s.getVehicle(aw).id();
		List<ActionWrapper> l = s.get(vID);
		int idx = l.indexOf(aw);

		if (idx != -1) {
			ActionWrapper next = l.get(idx + 1);
			return s.getTime(aw) + 1 == s.getTime(next);
		}
		return false;
	}

	public static boolean terminationCondition(int i) {
		return i >= 10000;
	}
}
