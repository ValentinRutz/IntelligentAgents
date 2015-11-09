package model;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Solution {
	private Map<Integer, List<ActionWrapper>> solution;
	
	public Solution() {
		setSolution(new HashMap<Integer, List<ActionWrapper>>());
	}
	
	public Solution(Solution otherSolution) {
		for (Map.Entry<Integer, List<ActionWrapper>> entry : otherSolution.entrySet()) {
			List<ActionWrapper> tmpL = new LinkedList<ActionWrapper>();
			Map<Integer, ActionWrapper> added = new HashMap<Integer, ActionWrapper>();
			for (ActionWrapper aw : entry.getValue()) {
				aw.copy(tmpL, added);
			}
			solution.put(entry.getKey(), tmpL);
		}
	}

	public Map<Integer, List<ActionWrapper>> getSolution() {
		return solution;
	}

	public void setSolution(Map<Integer, List<ActionWrapper>> solution) {
		this.solution = solution;
	}
	
	private Set<Map.Entry<Integer, List<ActionWrapper>>> entrySet() {
		return solution.entrySet();
	}

	public void put(int key, List<ActionWrapper> value) {
		solution.put(key, value);
	}

	public List<ActionWrapper> get(Integer id) {
		return solution.get(id);
	}
}
