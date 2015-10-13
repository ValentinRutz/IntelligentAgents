package template;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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

	// ADDED CODE - this variable counts how many actions have passed so far
	int counterSteps = 0;
	// ADDED CODE - this variable keeps reference of the Agent object
	Agent agent;
	
	private double pricePerKm;
	private Map<RKey, Double> R;
	private Map<TKey, Double> T;
	private Map<State, VValue> V;
	private Set<Actions> A;

	public void setup(Topology topology, TaskDistribution td, Agent agent) {

		// Reads the discount factor from the agents.xml file.
		// If the property is not present it defaults to 0.95
		Double discount = agent.readProperty("discount-factor", Double.class, 0.95);
		
		// ADDED CODE
		this.agent = agent;

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
			S.add(new State(start, null));
		}
		// Initialize A
		for (City city : topology.cities()) {
			A.add(new Actions(city));
		}
		//cout<<""
		// Initialize R(a, s)
		for (Actions a: A) {
			for (State s: S) {
				if (s.currLocation.hasNeighbor(a.city) && !a.city.equals(s.genPackage)) {
					R.put(new RKey(s, a), - s.currLocation.distanceTo(a.city) * pricePerKm);
				} else if (s.genPackage != null && a.city.equals(s.genPackage)) {
					R.put(new RKey(s, a), td.reward(s.currLocation, a.city) - s.currLocation.distanceTo(a.city) * pricePerKm);
				} else {
					R.put(new RKey(s, a), new Double(Integer.MIN_VALUE));
				}
				for (State to : S) {
					if(s.genPackage != null && a.city.equals(s.genPackage) && s.genPackage.equals(to.currLocation)) {
						T.put(new TKey(s, a, to), 1.d * td.probability(s.currLocation, s.genPackage) * td.probability(to.currLocation, to.genPackage));
					} else if(!a.city.equals(s.genPackage) && s.currLocation.hasNeighbor(to.currLocation) && a.city.equals(to.currLocation)) {
						T.put(new TKey(s, a, to), 1.d * td.probability(s.currLocation, s.genPackage) * td.probability(to.currLocation, to.genPackage));
					} else {
						T.put(new TKey(s, a, to), 0.d);
					}
				}
			}
		}
		
//		// Initialize T(s, a, s')
//		for (Actions a : A) {
//			for (State from : S) {
//				for (State to : S) {
//					if(from.genPackage != null && a.city.equals(from.genPackage) && from.genPackage.equals(to.from)) {
//						T.put(new TKey(from, a, to), td.probability(from.from, from.genPackage));
//					} else if(!a.city.equals(from.genPackage) && from.from.hasNeighbor(to.from) && a.city.equals(to.from)) {
//						T.put(new TKey(from, a, to), 1.d/from.from.neighbors().size());
//					} else {
//						T.put(new TKey(from, a, to), 0.d);
//					}
//				}
//			}
//		}
		
		// Initialize V(s) with  stupid values
		for (Actions a : A) {
			for (State s : S) {
				V.put(s, new VValue(1.d, a));
			}
		}
		
		Map<State, Boolean> change = new HashMap<State, Boolean>(V.size());
		for (State state : S) {
			change.put(state, true);
		}
		// Until "good enough" learn V(s)
		int i = 0;
		do {
			for (State state : S) {
				Actions bestAction = null;
				Double bestCurrentValue = 0.0;
				int k=0;
				for (Actions action : A) {
					RKey key = new RKey(state, action);
					double sum=0;
					for (State destination : S) {
					//	System.out.println("V " + V.get(destination).getReward() + " T " + T.get(new TKey(state, action, destination)));
						
						
						sum += T.get(new TKey(state, action, destination)) * V.get(destination).getReward();
					}
		//			System.out.println("Sum " + sum);
		//			System.out.println("R " + R.get(key));
					
					
					double value = discount*sum + R.get(key);
					
					if(k==0){
						bestCurrentValue=value;
						bestAction = action;
						k=1;
					}else if(value >= bestCurrentValue){
						bestCurrentValue=value;
						bestAction = action;
					}
				}
				/*
				System.out.println("Value "+bestCurrentValue);
				System.out.println(bestAction.city.toString());
				System.out.println(state.from.toString());
				System.out.println(state.genPackage.toString());
				System.out.println(td.reward(state.from, state.genPackage));
				*/

				//break;
			/*	if(Math.abs(V.get(state).reward - bestCurrentValue) < 0.00001) {
					change.put(state, false);
				} else V.put(state, new VValue(bestCurrentValue, bestAction));
			*/
				V.put(state, new VValue(bestCurrentValue, bestAction));
			
			}
			if(i%10 == 0) {
				System.out.println(i);
				for (State state : S) {
					System.out.print(V.get(state).getReward()+ " "); 
					Actions a = V.get(state).a;
					System.out.print(td.reward(state.currLocation, a.city) + " - " + state.currLocation.distanceTo(a.city) * pricePerKm);
					System.out.println();
					
				}
				System.out.println();
			}
			i++;
		} while((nb(change.values()) != V.size()) && i< 1);

		for (State state : S) {
			System.out.print(V.get(state).getReward()+ " "); 
		}
		System.out.println(i);
		System.out.println(nb(change.values()));
		System.out.println(V.size());
	}

	private int nb(Collection<Boolean> values) {
		int sum = 0;
		for (Boolean bool : values) {
			if(!bool) {
				sum++;
			}
		}
		return sum;
	}

	public Action act(Vehicle vehicle, Task availableTask) {
		// ADDED CODE - this output gives information about the "goodness" of your agent (higher values are preferred)
		if ((counterSteps > 0) && (counterSteps % 100 == 0)) {
			System.out.println("The total profit after " + counterSteps + " steps is " + agent.getTotalProfit() + ".");
			System.out.println("The profit per action after " + counterSteps + " steps is "
					+ ((double) agent.getTotalProfit() / counterSteps) + ".");
		}
		counterSteps++;
		// END OF ADDED CODE
		
		Action action;
		State currentState;
		VValue value;
		Actions bestAction;
		
		if (availableTask == null) {
			currentState = new State(vehicle.getCurrentCity(), null);
			value = V.get(currentState);
			bestAction = value.a;
			action = new Move(bestAction.city);
		} else {
			currentState = new State(vehicle.getCurrentCity(), availableTask.deliveryCity);
			value = V.get(currentState);
			bestAction = value.a;
			if(bestAction.city.equals(availableTask.deliveryCity)) {
				action = new Pickup(availableTask);
			} else {
				action = new Move(bestAction.city);
			}
		}

		return action;
	}
	
	
	// Model
	private class Actions {
		private City city;
		
		public Actions(City city) {
			this.city = city;
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + city.hashCode();
			return result;
		}

		public boolean equals(Object other) {
			if(!(other instanceof Actions)) {
				return false;
			}
			return ((Actions) other).city.equals(this.city);
		}

		private ReactiveTemplate getOuterType() {
			return ReactiveTemplate.this;
		}
	}
	
	private class State {
		private City currLocation, genPackage;
		
		public State(City from, City genPackage) {
			assert(from != null);
			this.currLocation = from;
			this.genPackage = genPackage;
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + currLocation.hashCode();
			result = prime * result + ((genPackage == null) ? 0 : genPackage.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object other) {
			if(!(other instanceof State)) {
				return false;
			}
			State otherState = (State) other;
			return otherState.currLocation.equals(this.currLocation) && (this.genPackage == null && otherState.genPackage == null) ||
					(this.genPackage != null && otherState.genPackage != null && this.genPackage.equals(otherState.genPackage));
		}

		private ReactiveTemplate getOuterType() {
			return ReactiveTemplate.this;
		}
	}

	private class RKey {
		private State s;
		private Actions a;
		
		public RKey(State s, Actions a) {
			assert(s != null && a != null);
			this.s = s;
			this.a = a;
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + a.hashCode();
			result = prime * result + s.hashCode();
			return result;
		}

		@Override
		public boolean equals(Object other) {
			if(!(other instanceof RKey)) {
				return false;
			}
			RKey otherKey = (RKey) other;
			return otherKey.a.equals(this.a) && otherKey.s.equals(this.s);
		}

		private ReactiveTemplate getOuterType() {
			return ReactiveTemplate.this;
		}
	}

	private class TKey {
		private State start, end;
		private Actions a;
		
		public TKey(State start, Actions a, State end) {
			assert(start != null && a != null && end != null);
			this.start = start;
			this.a = a;
			this.end = end;
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + a.hashCode();
			result = prime * result + end.hashCode();
			result = prime * result + start.hashCode();
			return result;
		}

		@Override
		public boolean equals(Object other) {
			if(!(other instanceof TKey)) {
				return false;
			}
			TKey otherKey = (TKey) other;
			return otherKey.a.equals(this.a) && otherKey.start.equals(this.start) && otherKey.end.equals(this.end);
		}

		private ReactiveTemplate getOuterType() {
			return ReactiveTemplate.this;
		}
	}
	
	private class VValue {
		private double value;
		private Actions a;
		
		public VValue(double reward, Actions a) {
			assert(a != null);
			this.value = reward;
			this.a = a;
		}

		public Double getReward() {
			return value;
		}
		
		@Override
		public boolean equals(Object other) {
			if(!(other instanceof VValue)) {
				return false;
			}
			
			VValue otherVValue = (VValue) other;
			return otherVValue.a == this.a && otherVValue.value == this.value;
		}
	}
}
