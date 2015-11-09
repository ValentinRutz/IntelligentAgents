package model;

import java.util.Map;

import logist.simulation.Vehicle;
import logist.task.Task;
import logist.task.TaskSet;

public class Constraints {
	
	public static boolean testCapacitySolution(int biggestCapacity, TaskSet tasks) {
		for (Task task : tasks) {
			if(task.weight > biggestCapacity)
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
		Map<ActionWrapper, Integer> time = s.getTime();
		Map<ActionWrapper, Vehicle> vehicle = s.getVehicle();
		return time.get(pw) < time.get(dw) && vehicle.get(pw).id() == vehicle.get(dw).id();
	}
	
	public static boolean testFirstTaskHasTimeOne(Solution s) {
		return false;
	}

	public static boolean terminationCondition() {
		// TODO Auto-generated method stub
		return false;
	}
}
