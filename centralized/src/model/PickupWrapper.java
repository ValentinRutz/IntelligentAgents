package model;

import logist.plan.Action.Pickup;
import logist.task.Task;

public class PickupWrapper extends ActionWrapper {

	public PickupWrapper(Task t) {
		super(t);
		setCity(t.pickupCity);
		setAction(new Pickup(t));
		setCounterpart(new DeliveryWrapper(t, this));
	}
}
