package org.controlsfx.control.textfield;

import javafx.event.EventType;
import javafx.scene.input.InputEvent;

/**
 * An event which indicates that a clear icon has been pressed in a {@link TextFields#createClearableTextField()} or {@link TextFields#createClearablePasswordField()}.
 *
 * This event is generated after cleaning a text for the TextField
 */
public class ClearEvent extends InputEvent {

    public static final EventType<ClearEvent> CLEAR_PRESSED = new EventType<>(InputEvent.ANY, "CLEAR_PRESSED");

    public ClearEvent(EventType<? extends InputEvent> eventType) {
        super(eventType);
    }

}
