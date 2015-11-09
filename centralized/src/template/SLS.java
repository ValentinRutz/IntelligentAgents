package template;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import logist.plan.Plan;
import logist.simulation.Vehicle;
import logist.task.Task;
import logist.task.TaskSet;
import model.ActionWrapper;
import model.Constraints;
import model.DeliveryWrapper;
import model.PickupWrapper;

public class SLS {

	static List<Plan> sls(List<Vehicle> vehicles, TaskSet tasks) {
		Map<Integer, List<ActionWrapper>> A = new HashMap<Integer, List<ActionWrapper>>();
		Map<Integer, List<ActionWrapper>> Aold = new HashMap<Integer, List<ActionWrapper>>();
		
		Vehicle biggestCapacity = null;
		for (Vehicle v : vehicles) {
			A.put(v.id(), new LinkedList<ActionWrapper>());
			if (biggestCapacity == null || biggestCapacity.capacity() < v.capacity()) {
				biggestCapacity = v;
			}
		}

		if (Constraints.testCapacitySolution(biggestCapacity, tasks)) {
			// Select initial solution
			Integer id = new Integer(biggestCapacity.id());
			ActionWrapper a = null;
			List<ActionWrapper> l;
			for (Task task : tasks) {
				a = new PickupWrapper(task);
				l = A.get(id);
				l.add(a);
				l.add(a.getCounterpart());
			}
			
			List<Map<Integer, List<ActionWrapper>>> N;

			while (!Constraints.terminationCondition()) {
				// Aold ← A
				for (Map.Entry<Integer, List<ActionWrapper>> entry : A.entrySet()) {
					List<ActionWrapper> tmpL = new LinkedList<ActionWrapper>();
					Map<Integer, ActionWrapper> added = new HashMap<Integer, ActionWrapper>();
					for (ActionWrapper aw : entry.getValue()) {
						aw.copy(tmpL, added);
					}
					Aold.put(entry.getKey(), tmpL);
				}
				
				// N ← ChooseNeighbours(Aold, X, D, C, f)
				N = chooseNeighbors();
				
				// A ← LocalChoice(N, f)
				A = localChoice(N);
			}
		}

		return solutionToPlans(A);
	}

	private static List<Map<Integer, List<ActionWrapper>>> chooseNeighbors() {
    	// TODO
		return null;
    }

	private static Map<Integer, List<ActionWrapper>> localChoice(List<Map<Integer, List<ActionWrapper>>> N) {
    	// TODO
		return null;
    }
	
	private static List<Plan> solutionToPlans(Map<Integer, List<ActionWrapper>> solution) {
    	// TODO
		return null;
	}
}
