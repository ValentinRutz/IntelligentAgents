package template;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;

import logist.agent.Agent;
import logist.behavior.ReactiveBehavior;
import logist.plan.Action;
import logist.plan.Action.Move;
import logist.plan.Action.Pickup;
import logist.simulation.Vehicle;
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
	private Map<State, VValue> V;
	private Set<Actions> A;

	public void setup(Topology topology, TaskDistribution td, Agent agent) {

		// Reads the discount factor from the agents.xml file.
		// If the property is not present it defaults to 0.95
		Double discount = agent.readProperty("discount-factor", Double.class, 0.95);

		this.random = new Random();
		this.pPickup = discount;
		this.A = new HashSet<Actions>();
		
		this.R = new HashMap<RKey, Double>();
		this.T = new HashMap<TKey, Double>();
		this.V = new HashMap<State, VValue>();
		this.pricePerKm = agent.readProperty("price-per-km", Double.class, 1.d);
		Set<State> S = new HashSet<State>();
		
		// Initialize S
		for (City start : topology.cities()) {
			for (City genPackage : topology.cities()) {
				S.add(new State(start, genPackage));
			}
			S.add(new State(start,null));
		}
		// Initialize A
		for (City city : topology.cities()) {
			A.add(new Actions(city));
		}
		
		// Initialize R(a, s)
		for (Actions a: A) {
			for (State s: S) {
				if (s.from.hasNeighbor(a.city) && !a.city.equals(s.genPackage)) {
					R.put(new RKey(s, a), - s.from.distanceTo(a.city) * pricePerKm);
				} else if (s.genPackage != null && a.city.equals(s.genPackage)) {
					R.put(new RKey(s, a), td.reward(s.from, a.city) - s.from.distanceTo(a.city) * pricePerKm);
				} else {
					R.put(new RKey(s, a), Double.MIN_VALUE);
				}
			}
		}
		
		// Initialize T(s, a, s')
		for (Actions a : A) {
			for (State from : S) {
				for (State to : S) {
					if(from.genPackage != null && a.city.equals(from.genPackage) && from.genPackage.equals(to.from)) {
						T.put(new TKey(from, a, to), td.probability(from.from, from.genPackage));
					} else if(!a.city.equals(from.genPackage) && from.from.hasNeighbor(to.from) && a.city.equals(to.from)) {
						T.put(new TKey(from, a, to), 1.d/from.from.neighbors().size());
					} else {
						T.put(new TKey(from, a, to), 0.d);
					}
				}
			}
		}
		
		// Initialize V(s) with  stupid values
		for (Actions a : A) {
			for (State s : S) {
				V.put(s, new VValue(1.d, a));
			}
		}
		
		//TreeMap<RKey, Double> Q = new TreeMap<RKey, Double>();
		// Until "good enough" learn V(s)
		int i = 0;
		while(i < 100000) {
			for (State state : S) {
				Actions bestAction = null;
				Double bestCurrentValue = Double.MIN_VALUE;
				for (Actions action : A) {
					RKey key = new RKey(state, action);
					double sum=0;
					for (State destination : S) {
						sum+= T.get(new TKey(state, action, destination))*V.get(destination).getReward();
					}
					double value = discount*sum + R.get(key);
					if(value >= bestCurrentValue){
						bestCurrentValue=value;
						bestAction = action;
					}
					//Q.put(key,value);
				}
				
				V.put(state, new VValue(bestCurrentValue, bestAction));
			}
			
			i++;
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
	private class Actions {
		private City city;
		
		public Actions(City city) {
			this.city = city;
		}
		
		public boolean equals(Object other) {
			if(!(other instanceof Actions)) {
				return false;
			}
			return ((Actions) other).city.equals(this.city);
		}
	}
	
	private class State {
		private City from, genPackage;
		
		public State(City from, City genPackage) {
			this.from = from;
			this.genPackage = genPackage;
		}
		
		@Override
		public boolean equals(Object other) {
			if(!(other instanceof State)) {
				return false;
			}
			State otherState = (State) other;
			return otherState.from.equals(this.from) && (this.genPackage.equals(otherState.genPackage));
		}
	}

	private class RKey {
		private State s;
		private Actions a;
		
		public RKey(State s, Actions a) {
			this.s = s;
			this.a = a;
		}
		
		@Override
		public boolean equals(Object other) {
			if(!(other instanceof RKey)) {
				return false;
			}
			RKey otherKey = (RKey) other;
			return otherKey.a.equals(this.a) && otherKey.s.equals(this.s);
		}
	}

	private class TKey {
		private State start, end;
		private Actions a;
		
		public TKey(State start, Actions a, State end) {
			this.start = start;
			this.a = a;
			this.end = end;
		}
		
		@Override
		public boolean equals(Object other) {
			if(!(other instanceof TKey)) {
				return false;
			}
			TKey otherKey = (TKey) other;
			return otherKey.a.equals(this.a) && otherKey.start.equals(this.start) && otherKey.end.equals(this.end);
		}
	}
	
	private class VValue {
		private double reward;
		private Actions a;
		
		public VValue(double reward, Actions a) {
			this.reward = reward;
			this.a = a;
		}

		public Double getReward() {
			return reward;
		}
		
		@Override
		public boolean equals(Object other) {
			if(!(other instanceof VValue)) {
				return false;
			}
			
			VValue otherVValue = (VValue) other;
			return otherVValue.a == this.a && otherVValue.reward == this.reward;
		}
	}
}
