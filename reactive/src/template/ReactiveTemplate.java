package template;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
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

	private Random random;
	private double pPickup;
	
	private double pricePerKm;
	private Map<RKey, Double> R;
	private Map<TKey, Double> T;
	private Map<State, VValue> V;

	public void setup(Topology topology, TaskDistribution td, Agent agent) {

		// Reads the discount factor from the agents.xml file.
		// If the property is not present it defaults to 0.95
		Double discount = agent.readProperty("discount-factor", Double.class, 0.95);

		this.random = new Random();
		this.pPickup = discount;
		Actions[] actions = Actions.values();
		
		this.R = new HashMap<RKey, Double>();
		this.T = new HashMap<TKey, Double>();
		this.V = new HashMap<State, VValue>();
		this.pricePerKm = agent.readProperty("price-per-km", Double.class, 1.d);
		Set<State> S = new HashSet<State>();
		
		// Initialize S
		for (City start : topology.cities()) {
			for (City end : topology.cities()) {
				for (City genPackage : topology.cities()) {
					S.add(new State(start, end, genPackage));
				}
				
			}
		}
		
		// Initialize R(a, s)
		for (Actions a : actions) {
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
		for (Actions a : actions) {
			for (State from : S) {
				for (State to : S) {
					if(a == Actions.PICKUP && from.to.id == to.from.id) {
						T.put(new TKey(from, a, to), td.probability(from.from, to.from));
					} else if(a == Actions.MOVETO && from.from.hasNeighbor(to.from) &&
							from.to.id == to.from.id && !from.from.neighbors().isEmpty() &&
							from.genPackage.id == to.from.id && from.genPackage.id == from.to.id) {
						T.put(new TKey(from, a, to), 1.d/from.from.neighbors().size());
					} else {
						T.put(new TKey(from, a, to), 0.d);
					}
				}
			}
		}
		
		// Initialize V(s) with  stupid values
		for (Actions a : actions) {
			for (State s : S) {
				V.put(s, new VValue(1.d, a));
			}
		}
		
		Map<RKey, Double> Q = new HashMap<RKey, Double>();
		// Until "good enough" learn V(s)
		int i = 0;
		while(i < 10000) {
			for (State s : S) {
				for (Actions a : actions) {
					RKey k = new RKey(s, a);
					double sum = 0;
					for (State otherS : S) {
						if(!T.containsKey(new TKey(s, a, otherS))) {
							System.out.println("T is not complete!!!");
							break;
						}
						sum += (T.get(new TKey(s, a, otherS))
								* V.get(otherS).getReward());
					}
					if(!R.containsKey(k)) {
						System.out.println("R is not complete!!!");
						break;
					}
					Q.put(k, R.get(k) + discount * sum);
				}
				double pickup = Q.get(new RKey(s, Actions.PICKUP));
				double moveto = Q.get(new RKey(s, Actions.MOVETO));
				
				V.put(s, (pickup >= moveto)? new VValue(pickup, Actions.PICKUP) : new VValue(moveto, Actions.MOVETO));
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
	private enum Actions { PICKUP, MOVETO; }
	
	private class State {
		private City from, to, genPackage;
		
		public State(City from, City to, City genPackage) {
			this.from = from;
			this.to = to;
			this.genPackage = genPackage;
		}
	}

	@SuppressWarnings("unused")
	private class RKey {
		private State s;
		private Actions a;
		
		public RKey(State s, Actions a) {
			this.s = s;
			this.a = a;
		}
	}

	@SuppressWarnings("unused")
	private class TKey {
		private State start, end;
		private Actions a;
		
		public TKey(State start, Actions a, State end) {
			this.start = start;
			this.a = a;
			this.end = end;
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
	}
}
