package model;

import java.util.List;
import java.util.Map;

import logist.plan.Action.Delivery;
import logist.task.Task;

public class DeliveryWrapper extends ActionWrapper {

	public DeliveryWrapper(Task t, PickupWrapper pickupWrapper) {
		super(t);
		setCity(t.pickupCity);
		setAction(new Delivery(t));
		setCounterpart(pickupWrapper);
	}

	public DeliveryWrapper(ActionWrapper counterpart, PickupWrapper pickupWrapper) {
		super(counterpart);
		setCity(counterpart.getCity());
		setAction(counterpart.getAction());
		setCounterpart(pickupWrapper);
	}

	@Override
	public void copy(List<ActionWrapper> l, Map<Integer, ActionWrapper> added) {
		l.add(added.get(this.getID()));
	}
}
