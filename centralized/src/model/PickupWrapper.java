package model;

import java.util.List;
import java.util.Map;

import logist.plan.Action.Pickup;
import logist.task.Task;

public class PickupWrapper extends ActionWrapper {

	public PickupWrapper(Task t) {
		super(t);
		setWeight(t.weight);
		setCity(t.pickupCity);
		setAction(new Pickup(t));
		setCounterpart(new DeliveryWrapper(t, this));
	}
	
	public PickupWrapper(ActionWrapper aw) {
		super(aw);
		setWeight(aw.getWeight());
		setCity(aw.getCity());
		setAction(aw.getAction());
		setCounterpart(new DeliveryWrapper(aw.getCounterpart(), this));
	}

	@Override
	public void copy(List<ActionWrapper> l, Map<Integer, ActionWrapper> added) {
		PickupWrapper pw = new PickupWrapper(this);
		l.add(pw);
		added.put(pw.getID(), pw.getCounterpart());
	}
}
