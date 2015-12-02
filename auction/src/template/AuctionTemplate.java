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
public class AuctionTemplate implements AuctionBehavior {

	private Topology topology;
	private TaskDistribution distribution;
	private Agent agent;
	private Random random;
	//private Vehicle vehicle;
	//private City currentCity;
	private ArrayList<Task> wonSoFar;
	private ArrayList<Task> universe;

	private double currentCost;
	private double tmpCost;
	private boolean firstTime;

	@Override
	public void setup(Topology topology, TaskDistribution distribution,
			Agent agent) {

		this.topology = topology;
		this.distribution = distribution;
		this.agent = agent;
	
	
		firstTime = true;
		currentCost = 0;
		wonSoFar = new ArrayList<>();
		universe = new ArrayList<>();
	}

	@Override
	public void auctionResult(Task previous, int winner, Long[] bids) {
		if (winner == agent.id()) {
			wonSoFar.add(previous);
			
			currentCost = tmpCost;

		}
	}

	@Override
	public Long askPrice(Task task) {

		boolean cantakeit = false;
		for (Vehicle v : agent.vehicles()) {
			if (v.capacity() >= task.weight)
				cantakeit=true;

		}
		if(!cantakeit) return null;
		
		System.out.println(task.id);

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
		System.out.println("Won so far " + wonSoFar.size());
		
		double marginalCost = tmpCost - currentCost;
		
		double futureProb = 0.0;
		
	//	futureProb = distribution.probability(currentCity, task.pickupCity);
		for (Vehicle v : agent.vehicles()) {
			double p = distribution.probability(v.getCurrentCity(), task.pickupCity);
			futureProb = Math.max(futureProb,p);
		}
		
		for (Task t : wonSoFar) {
			double p1 = distribution.probability(t.deliveryCity, task.pickupCity);
			double p2 = distribution.probability(task.deliveryCity,t.pickupCity);
			
			futureProb = Math.max(futureProb,p1);
			futureProb = Math.max(futureProb,p2);
						
		}
		
		
		
		double futureFactor = (1.0/5.0 - futureProb)*0.5*marginalCost;
		System.out.println("Future prob " + futureProb);
		
		long bid = (long) (marginalCost + futureFactor);
		
		System.out.println("Future factor "+ futureFactor);
		return bid;
	}

	@Override
	public List<Plan> plan(List<Vehicle> vehicles, TaskSet tasks) {

		// System.out.println("Agent " + agent.id() + " has tasks " + tasks);

		List<Plan> plans = SLS.solutionToPlans(SLS.sls(vehicles, tasks, 0.5));

		
		return plans;
	}

	
}
