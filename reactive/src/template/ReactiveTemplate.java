package template;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import logist.simulation.Vehicle;
import logist.agent.Agent;
import logist.behavior.ReactiveBehavior;
import logist.plan.Action;
import logist.plan.Action.Move;
import logist.plan.Action.Pickup;
import logist.task.Task;
import logist.task.TaskDistribution;
import logist.topology.Topology;
import logist.topology.Topology.City;

public class ReactiveTemplate implements ReactiveBehavior {

	private Random random;
	private double pPickup;
	
	private double pricePerKm;
	private Map<RKey, Double> R;
	private Map<TKey, Double> T;

	@Override
	public void setup(Topology topology, TaskDistribution td, Agent agent) {

		// Reads the discount factor from the agents.xml file.
		// If the property is not present it defaults to 0.95
		Double discount = agent.readProperty("discount-factor", Double.class, 0.95);

		this.random = new Random();
		this.pPickup = discount;
		
		this.R = new HashMap<RKey, Double>();
		this.T = new HashMap<TKey, Double>();
		this.pricePerKm = agent.readProperty("price-per-km", Double.class, 1.d);
		// TODO: Initialize R(a, s) and T(s, a, s')
		for (Actions a : Actions.values()) {
			for (City from : topology.cities()) {
				for (City to : topology.cities()) {
					R.put(new RKey(new State(from, to), a), from.distanceTo(to) * pricePerKm);
				}
			}
		}
	}

	@Override
	public Action act(Vehicle vehicle, Task availableTask) {
		Action action;
		double distanceToDelivery = availableTask.pickupCity.distanceTo(availableTask.deliveryCity);
		double priceOfDelivery = distanceToDelivery * pricePerKm;

		if (availableTask == null || random.nextDouble() > pPickup) {
			City currentCity = vehicle.getCurrentCity();
			action = new Move(currentCity.randomNeighbor(random));
		} else {
			action = new Pickup(availableTask);
		}
		return action;
	}
	
	private enum Actions {
		PICKUP, MOVETO
	}
	
	private class State {
		City from, to;
		
		public State(City from, City to) {
			this.from = from;
			this.to = to;
		}
	}

	private class RKey {
		State s;
		Actions a;
		
		public RKey(State s, Actions a) {
			this.s = s;
			this.a = a;
		}
	}
	
	private class TKey {
		State start, end;
		Actions a;
		
		public TKey(State start, Actions a, State end) {
			this.start = start;
			this.a = a;
			this.end = end;
		}
	}
}
