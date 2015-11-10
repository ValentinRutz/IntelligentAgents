package template;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import logist.plan.Action;
import logist.plan.Action.Move;
import logist.plan.Plan;
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
		Solution Aold;
		List<Integer> ids = new LinkedList<Integer>();

		Vehicle biggestCapacity = null;
		for (Vehicle v : vehicles) {
			A.put(v.id(), new LinkedList<ActionWrapper>());
			if (biggestCapacity == null || biggestCapacity.capacity() < v.capacity()) {
				biggestCapacity = v;
			}
			ids.add(v.id());
		}

		if (Constraints.testCapacitySolution(biggestCapacity.capacity(), tasks)) {
			// Select initial solution
			Integer id = new Integer(biggestCapacity.id());
			ActionWrapper a = null;
			List<ActionWrapper> l;
			int time = 1;
			for (Task task : tasks) {
				a = new PickupWrapper(task);
				l = A.get(id);
				if (Constraints.testCapacity(biggestCapacity, a, A)) {
					l.add(a);
					A.putVehicle(a, biggestCapacity);
					A.putTime(a, time++);
					if (Constraints.testCapacity(biggestCapacity, a.getCounterpart(), A)) {
						l.add(a.getCounterpart());
						A.putVehicle(a.getCounterpart(), biggestCapacity);
						A.putTime(a.getCounterpart(), time++);
					}
				}
			}

			List<Solution> N;
			List<Vehicle> vehiclesCopy = new ArrayList<Vehicle>();
			vehiclesCopy.addAll(vehicles);
			for (int i = 0; !Constraints.terminationCondition(i); ++i) {
				// Aold ← A
				Aold = new Solution(A);

				// N ← ChooseNeighbours(Aold, X, D, C, f)
				N = chooseNeighbors(Aold, vehiclesCopy);

				// A ← LocalChoice(N, f)
				A = localChoice(N, Aold);
			}
		}

		return solutionToPlans(A);
	}

	private static List<Solution> chooseNeighbors(Solution sol, List<Vehicle> vehicles) {
		Collections.shuffle(vehicles);
		int vehicleID = vehicles.get(0).id();

		List<Solution> neighbors = new ArrayList<Solution>();
		exchangeTask(vehicles, sol, neighbors);
		swapTasks(vehicleID, sol, neighbors);

		return neighbors;
	}

	private static void exchangeTask(List<Vehicle> vehicles, Solution sol, List<Solution> neighbors) {
		Vehicle chosen = vehicles.get(0);
		Solution neighbor = null;
		int size = vehicles.size();
		if (size >= 2) {
			for (int i = 1; i < size; i++) {
				for (int j = 0; j < sol.get(chosen.id()).size(); j++) {
					neighbor = new Solution(sol);
					if (neighbor.changeTaskVehicle(chosen, vehicles.get(i), j)) {
						neighbors.add(neighbor);
					}
				}
			}
		}
	}

	private static void swapTasks(int vehicleID, Solution sol, List<Solution> neighbors) {
		Solution neighbor = null;
		int size = sol.get(vehicleID).size();
		for (int i = 0; i < size; i++) {
			for (int j = i + 1; j < size; j++) {
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

		if (p < 0.4 && bestSolution != null)
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

				City next = null;

				for (ActionWrapper actionWrapper : entry) {
					next = actionWrapper.getCity();

					for (City city : current.pathTo(next)) {
						acts.add(new Move(city));
					}
					acts.add(actionWrapper.getAction());
					current = next;
				}

				p = new Plan(v.getCurrentCity(), acts);

			} else {
				p = Plan.EMPTY;
			}

			result.add(p);

		}
		return result;
	}
}
