package template;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import logist.plan.Action.Move;
import logist.plan.Plan;
import logist.plan.Action;
import logist.simulation.Vehicle;
import logist.task.Task;
import logist.task.TaskSet;
import logist.topology.Topology.City;
import model.ActionWrapper;
import model.Constraints;
import model.PickupWrapper;
import model.Solution;

public class SLS {

	static List<Plan> sls(List<Vehicle> vehicles, TaskSet tasks) {
		Solution A = new Solution();
		Solution Aold = new Solution();
		List<Integer> ids = new LinkedList<Integer>();

		Vehicle biggestCapacity = null;
		for (Vehicle v : vehicles) {
			A.put(v.id(), new LinkedList<ActionWrapper>());
			if (biggestCapacity == null
					|| biggestCapacity.capacity() < v.capacity()) {
				biggestCapacity = v;
			}
			ids.add(v.id());
		}

		if (Constraints.testCapacitySolution(biggestCapacity.capacity(), tasks)) {
			// Select initial solution
			Integer id = new Integer(biggestCapacity.id());
			ActionWrapper a = null;
			List<ActionWrapper> l;
			for (Task task : tasks) {
				a = new PickupWrapper(task);
				l = A.get(id);
				if (Constraints.testCapacity(biggestCapacity, a, A)) {
					l.add(a);
					if (Constraints.testCapacity(biggestCapacity,
							a.getCounterpart(), A)) {
						l.add(a.getCounterpart());
					}
				}
			}

			List<Solution> N;

			while (!Constraints.terminationCondition()) {
				// Aold ← A
				Aold = new Solution(A);

				// N ← ChooseNeighbours(Aold, X, D, C, f)
				N = chooseNeighbors(Aold, ids);

				// A ← LocalChoice(N, f)
				A = localChoice(N, A);
			}
		}

		return solutionToPlans(A);
	}

	private static List<Solution> chooseNeighbors(Solution sol,
			List<Integer> ids) {
		Collections.shuffle(ids);
		int vehicleID = ids.get(0);

		List<Solution> neighbors = new ArrayList<Solution>();
		swapTasks(vehicleID, sol, neighbors);
		exchangeTask(vehicleID, sol, neighbors);

		return neighbors;
	}

	private static void exchangeTask(int vehicleID, Solution sol,
			List<Solution> neighbors) {
		// TODO
	}

	private static void swapTasks(int vehicleID, Solution sol,
			List<Solution> neighbors) {

		Solution neighbor = null;
		for (int i = 0; i < sol.get(vehicleID).size(); i++) {
			for (int j = i + 1; j < sol.get(vehicleID).size(); j++) {
				neighbor = new Solution(sol);
				if (neighbor.changeTasksOrder(vehicleID, i, j)) {
					neighbors.add(neighbor);
				}

			}
		}

	}

	private static Solution localChoice(List<Solution> N, Solution A) {
		double bestCost = 0;
		boolean firstTime = true;
		Solution bestSolution = null;

		for (Solution solution : N) {
			if (firstTime) {
				bestCost = solution.cost();
				bestSolution = solution;
				firstTime = false;
			} else if (bestCost > solution.cost()) {
				bestCost = solution.cost();
				bestSolution = solution;
			}

		}

		Random r = new Random();
		double p = r.nextDouble();

		if (p < 0.3 && bestSolution!=null)
			return bestSolution;
		else
			return A;
	}

	
	private static List<Plan> solutionToPlans(Solution solution) {
		List<Plan> result = new ArrayList<Plan>();
		for (List<ActionWrapper> entry : solution.getSolution().values()) {
			Plan p = null;
			if (!entry.isEmpty()) {

				ActionWrapper firstTask = entry.get(0);
				Vehicle v = solution.getVehicle(firstTask);
				City current = v.getCurrentCity();
				
				List<Action> acts = new ArrayList<Action>();
				
				City next = firstTask.getCity();

				for (City city : current.pathTo(next)) {
					acts.add(new Move(city));
				}
				
				acts.add(firstTask.getAction());
				
				for (ActionWrapper actionWrapper : entry) {
					current = next;
					next = actionWrapper.getCity();
					
					for (City city : current.pathTo(next)) {
						acts.add(new Move(city));
					}
					acts.add(actionWrapper.getAction());					
				}
				
				
				p = new Plan(v.getCurrentCity(),acts);
				
			}else {
				p = Plan.EMPTY;
			}
			
			result.add(p);

		}
		return result;
	}
}
