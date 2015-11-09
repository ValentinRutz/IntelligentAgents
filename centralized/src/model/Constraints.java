package model;

import logist.simulation.Vehicle;
import logist.task.Task;
import logist.task.TaskSet;

public class Constraints {
	
	public static boolean testCapacitySolution(Vehicle biggestCapacity, TaskSet tasks) {
		for (Task task : tasks) {
			if(task.weight > biggestCapacity.capacity())
				return false;
		}
		
		return true;
	}

	public static boolean testCapacity(Vehicle v, Task t) {
		return v.capacity() /* Some expression to get the sum of the weights of the current tasks */ - t.weight >= 0;
	}
	
	public static boolean testInitialAction(Vehicle v, Task t) {
		return false;
	}

	public static boolean terminationCondition() {
		// TODO Auto-generated method stub
		return false;
	}
}
