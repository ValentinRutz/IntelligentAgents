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
import template.SLS;

/**
 * A very simple auction agent that assigns all tasks to its first vehicle and
 * handles them sequentially.
 * 
 */
@SuppressWarnings("unused")
public class MarginalCost implements AuctionBehavior {

	private Topology topology;
	private TaskDistribution distribution;
	private Agent agent;
	private Random random;
	private Vehicle vehicle;
	private City currentCity;
	private ArrayList<Task> wonSoFar;
	private ArrayList<Task> universe;

	private double currentCost;
	private double moneySoFar;
	private double tmpCost;
	private boolean firstTime;

	@Override
	public void setup(Topology topology, TaskDistribution distribution,
			Agent agent) {

		this.topology = topology;
		this.distribution = distribution;
		this.agent = agent;
		this.vehicle = agent.vehicles().get(0);
		this.currentCity = vehicle.homeCity();

		long seed = -9019554669489983951L * currentCity.hashCode() * agent.id();
		this.random = new Random(seed);

		firstTime = true;
		currentCost = 0;
		moneySoFar = 0;
		wonSoFar = new ArrayList<>();
		universe = new ArrayList<>();
	}

	@Override
	public void auctionResult(Task previous, int winner, Long[] bids) {
		if (winner == agent.id()) {
			currentCity = previous.deliveryCity;
			wonSoFar.add(previous);
			moneySoFar += bids[agent.id()];
			currentCost = tmpCost;

		}
		
		System.out.print((moneySoFar - currentCost) + "\t" );

	}

	@Override
	public Long askPrice(Task task) {

		if (vehicle.capacity() < task.weight)
			return null;

	//	System.out.println(task.id);

		universe.add(task);
		Task [] arr = new Task[universe.size()];
		universe.toArray(arr);
		TaskSet tasks = TaskSet.create(arr);
		TaskSet empty = TaskSet.noneOf(tasks);
		
		for (Task task2 : wonSoFar) {
			
			empty.add(arr[task2.id]);
		}
		
		empty.add(task);
			
		tmpCost = SLS.sls(agent.vehicles(), empty, 0.5).cost();
				
	//	System.out.println("Tmpcost " + tmpCost);
	//	System.out.println("Curr cost "+ currentCost);
	//	System.out.println("Won so far " + wonSoFar.size());
		
		double marginalCost = tmpCost - currentCost;
		
				
		long bid = (long) (marginalCost);
		
		
		
		return bid;
	}

	@Override
	public List<Plan> plan(List<Vehicle> vehicles, TaskSet tasks) {

		// System.out.println("Agent " + agent.id() + " has tasks " + tasks);

		List<Plan> plans = SLS.solutionToPlans(SLS.sls(vehicles, tasks, 0.5));

		
		return plans;
	}

	
}
