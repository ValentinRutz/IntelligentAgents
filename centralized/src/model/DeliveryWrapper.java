package model;

import logist.plan.Action.Delivery;
import logist.task.Task;

public class DeliveryWrapper extends ActionWrapper {

	public DeliveryWrapper(Task t, PickupWrapper pickupWrapper) {
		super(t);
		setCity(t.pickupCity);
		setAction(new Delivery(t));
		setCounterpart(pickupWrapper);
	}
}
