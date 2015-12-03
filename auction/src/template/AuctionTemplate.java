package template;

//the list of imports
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import logist.Measures;
import logist.behavior.AuctionBehavior;
import logist.agent.Agent;
import logist.simulation.Vehicle;
import logist.plan.Plan;
import logist.task.Task;
import logist.task.TaskDistribution;
import logist.task.TaskSet;
import logist.topology.Topology;
import logist.topology.Topology.City;
import model.ActionWrapper;
import model.Solution;
import template.SLS;

/**
 * A very simple auction agent that assigns all tasks to its first vehicle and
 * handles them sequentially.
 * 
 */
@SuppressWarnings("unused")
public class AuctionTemplate implements AuctionBehavior {

	private Topology topology;
	private TaskDistribution distribution;
	private Agent agent;
	private Random random;
	// private Vehicle vehicle;
	// private City currentCity;
	private ArrayList<Task> wonSoFar;
	private ArrayList<Task> universe;

	private double costSoFar;
	private double tmpCost;
	private double moneySoFar;
	private Solution currentSolution;
	private Solution tmpSolution;
	private List<Plan> planSoFar;
	private double otherBids;
	private double sumMarginalCost;

	@Override
	public void setup(Topology topology, TaskDistribution distribution,
			Agent agent) {

		this.topology = topology;
		this.distribution = distribution;
		this.agent = agent;

		moneySoFar = 0;
		costSoFar = 0;
		wonSoFar = new ArrayList<>();
		universe = new ArrayList<>();
		otherBids = 0;
		sumMarginalCost = 0;
	}

	@Override
	public void auctionResult(Task previous, int winner, Long[] bids) {
		if (winner == agent.id()) {
			wonSoFar.add(previous);
			moneySoFar += bids[agent.id()];
			costSoFar = tmpCost;

			planSoFar = SLS.solutionToPlans(tmpSolution);

		} else {
			otherBids += bids[winner];
		}

		System.out.print((moneySoFar - costSoFar) + "\t" );
	}

	@Override
	public Long askPrice(Task task) {
		System.out.println();

		boolean cantakeit = false;
		for (Vehicle v : agent.vehicles()) {
			if (v.capacity() >= task.weight)
				cantakeit = true;

		}
		if (!cantakeit)
			return null;

		universe.add(task);
		Task[] arr = new Task[universe.size()];
		universe.toArray(arr);
		TaskSet tasks = TaskSet.create(arr);
		TaskSet empty = TaskSet.noneOf(tasks);

		for (Task task2 : wonSoFar) {

			empty.add(arr[task2.id]);
		}

		empty.add(task);

		tmpSolution = SLS.sls(agent.vehicles(), empty, 0.5);
		tmpCost = tmpSolution.cost();

		// System.out.println("Tmpcost " + tmpCost);
		// System.out.println("Curr cost "+ currentCost);
		// System.out.println("Won so far " + wonSoFar.size());

		double marginalCost = tmpCost - costSoFar;
		sumMarginalCost += marginalCost;

		if (marginalCost < 0)
			marginalCost = 0;

		double futureProb = 0.0;
		double futureFactor = 0.0;

		// futureProb = distribution.probability(currentCity, task.pickupCity);
		if (wonSoFar.size() > 0) {
			ActionWrapper a = tmpSolution.previousDelivery(task);
			if (a != null) {
				futureProb = Math.max(
						distribution.probability(a.getCity(), task.pickupCity),
						futureProb);

			}

			a = tmpSolution.nextPickUp(task);
			if (a != null) {
				futureProb = Math.max(distribution.probability(
						task.deliveryCity, a.getCity()), futureProb);
			}

			double f = 0.2;

			double profit = moneySoFar - costSoFar;
			if (profit < 0) {
				futureFactor = (f - futureProb) * (task.id / 10.0) * (-1)
						* profit;
			} else
				futureFactor = (f - futureProb) * (task.id / 10.0)
						* sumMarginalCost / universe.size();

		} else if (otherBids > 0) {
			marginalCost = otherBids / (universe.size() - 1 - wonSoFar.size());

		} else
			marginalCost *= 0.66;

		long bid = (long) (marginalCost + futureFactor);
		// System.out.println("Future prob " + futureProb);
		// System.out.println(bid + "\t" + marginalCost+ "\t" + (moneySoFar -
		// costSoFar));

		return bid;
	}

	@Override
	public List<Plan> plan(List<Vehicle> vehicles, TaskSet tasks) {

		// System.out.println("Agent " + agent.id() + " has tasks " + tasks);

		System.out.println("Profit " + (moneySoFar - costSoFar));
		Solution s = SLS.sls(vehicles, tasks, 0.5);
		List<Plan> plans = SLS.solutionToPlans(s);
		// List<Plan> plans = SLS.solutionToPlans(currentSolution);
		// System.out.println(s.cost());
		System.out.println("won so far " + wonSoFar.size());
		System.out.println(costSoFar);
		return plans;
	}

}
