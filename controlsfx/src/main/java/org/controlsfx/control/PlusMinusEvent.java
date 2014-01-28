package org.controlsfx.control;

import javafx.event.EventTarget;
import javafx.event.EventType;
import javafx.scene.input.InputEvent;

/**
 * An event class used by the {@link PlusMinusAdjuster} to inform event handlers
 * about changes.
 */
public final class PlusMinusEvent extends InputEvent {

	private static final long serialVersionUID = 2881004583512990781L;

	public static final EventType<PlusMinusEvent> ANY = new EventType<>(
			InputEvent.ANY, "ANY");

	/**
	 * An event type used when the value property (
	 * {@link PlusMinusAdjuster#valueProperty()}) changes.
	 */
	public static final EventType<PlusMinusEvent> VALUE_CHANGED = new EventType<>(
			PlusMinusEvent.ANY, "VALUE_CHANGED");

	private double value;

	/**
	 * Constructs a new event object.
	 * 
	 * @param source
	 *            the source of the event (always the {@link PlusMinusAdjuster})
	 * @param target
	 *            the target of the event (always the {@link PlusMinusAdjuster})
	 * @param eventType
	 *            the type of the event (e.g. {@link #VALUE_CHANGED})
	 * @param value
	 *            the actual current value of the adjuster
	 */
	public PlusMinusEvent(Object source, EventTarget target,
			EventType<? extends InputEvent> eventType, double value) {
		super(source, target, eventType);

		this.value = value;
	}

	/**
	 * The value of the {@link PlusMinusAdjuster}.
	 * 
	 * @return the value
	 */
	public double getValue() {
		return value;
	}
}