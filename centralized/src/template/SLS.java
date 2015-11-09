package template;

import java.util.LinkedList;
import java.util.List;

import logist.plan.Plan;
import logist.simulation.Vehicle;
import logist.task.Task;
import logist.task.TaskSet;
import model.ActionWrapper;
import model.Constraints;
import model.PickupWrapper;
import model.Solution;

public class SLS {

	static List<Plan> sls(List<Vehicle> vehicles, TaskSet tasks) {
		Solution A = new Solution();
		Solution Aold = new Solution();

		Vehicle biggestCapacity = null;
		for (Vehicle v : vehicles) {
			A.put(v.id(), new LinkedList<ActionWrapper>());
			if (biggestCapacity == null || biggestCapacity.capacity() < v.capacity()) {
				biggestCapacity = v;
			}
		}

		if (Constraints.testCapacitySolution(biggestCapacity.capacity(), tasks)) {
			// Select initial solution
			Integer id = new Integer(biggestCapacity.id());
			ActionWrapper a = null;
			List<ActionWrapper> l;
			for (Task task : tasks) {
				a = new PickupWrapper(task);
				l = A.get(id);
				if (Constraints.testCapacity(biggestCapacity, task, A)) {
					l.add(a);
					if (Constraints.testCapacity(biggestCapacity, task, A)) {
						l.add(a.getCounterpart());
					}
				}
			}

			List<Solution> N;

			while (!Constraints.terminationCondition()) {
				// Aold ← A
				Aold = new Solution(A);

				// N ← ChooseNeighbours(Aold, X, D, C, f)
				N = chooseNeighbors(Aold);

				// A ← LocalChoice(N, f)
				A = localChoice(N);
			}
		}

		return solutionToPlans(A);
	}

	private static List<Solution> chooseNeighbors(Solution sol) {
		// TODO
		return null;
	}

	private static Solution localChoice(List<Solution> N) {
		// TODO
		return null;
	}

	private static List<Plan> solutionToPlans(Solution solution) {
		// TODO
		return null;
	}
}
