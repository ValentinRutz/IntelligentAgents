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
		// Initialize R(a, s)
		for (Actions a : Actions.values()) {
			for (City from : topology.cities()) {
				if(a == Actions.MOVETO) {
					for (City neighbor : from.neighbors()) {
						for (City genPackage: topology.cities()) {
							if(from.id != genPackage.id && genPackage.id != neighbor.id)
								R.put(new RKey(new State(from, neighbor, genPackage), a), - from.distanceTo(neighbor) * pricePerKm);
						}
						R.put(new RKey(new State(from, neighbor, null), a), - from.distanceTo(neighbor) * pricePerKm);
					}
				} else if(a == Actions.PICKUP) {
					for (City to : topology.cities()) {
						if(from.id != to.id)
							R.put(new RKey(new State(from,  to, to), a), td.reward(from, to) - from.distanceTo(to) * pricePerKm);
					}
				}
				
			}
		}
		
		// Initialize T(s, a, s')
		for (Actions a : Actions.values()) {
			for (City fromStart : topology.cities()) {
				for (City fromEnd : topology.cities()) {
					for (City toStart : topology.cities()) {
						for (City toEnd : topology.cities()) {
							
						}
					}
				}
			}
		}
	}

	public Action act(Vehicle vehicle, Task availableTask) {
		Action action;

		if (availableTask == null || random.nextDouble() > pPickup) {
			City currentCity = vehicle.getCurrentCity();
			action = new Move(currentCity.randomNeighbor(random));
		} else {
			action = new Pickup(availableTask);
		}
		return action;
	}
	
	
	// Model
	private enum Actions { PICKUP, MOVETO; }
	
	private class State {
		City from, to, genPackage;
		
		public State(City from, City to, City genPackage) {
			this.from = from;
			this.to = to;
			this.genPackage = genPackage;
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
