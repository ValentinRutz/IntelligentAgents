package template;

import static template.CentralizedTemplate.firstActions;

import java.util.ArrayList;
import java.util.List;

import logist.plan.Plan;
import logist.simulation.Vehicle;
import logist.task.Task;
import logist.task.TaskSet;
import model.ActionWrapper;
import model.Constraints;
import model.PickupWrapper;

public class SLS {
	
    static List<Plan> sls(List<Vehicle> vehicles, TaskSet tasks) {
    	List<Plan> A = new ArrayList<Plan>(), Aold;
    	Vehicle biggestCapacity = null;
    	for (Vehicle v : vehicles) {
    		A.add(v.id(), Plan.EMPTY);
			if(biggestCapacity == null || biggestCapacity.capacity() < v.capacity()) {
				biggestCapacity = v;
			}
		}
    	
    	if(Constraints.testCapacitySolution(biggestCapacity, tasks)) {
        	// Select initial solution
    		Integer id = new Integer(biggestCapacity.id());
			ActionWrapper a = firstActions.get(id), b = null;
    		for (Task task : tasks) {
    			if(a == null) {
    				a = new PickupWrapper(task);
    				firstActions.put(id, a);
    				a.setNextAction(a.getCounterpart());
    				a = a.getCounterpart();
    			} else {
    				b = new PickupWrapper(task);
    				a.setNextAction(b);
    				b.setNextAction(b.getCounterpart());
    				a = b.getCounterpart();
    			}
			}
    		
    		while(!Constraints.terminationCondition()) {
    			// Aold = firstActions.copy
    			
    		}
    	}
    	
    	return A;
    }
    
    private ??? chooseNeighbors() {
    	// TODO
    }
    
    private ??? localChoice() {
    	// TODO
    }

}
