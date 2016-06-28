package org.controlsfx.control.action;

import javafx.scene.control.Button;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ToggleButton;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks the {@link Action} or a method annotated with {@link ActionProxy} to let action engine know
 * that {@link ToggleButton} or {@link CheckMenuItem} has to be bound to the action
 * instead of standard {@link Button} and {@link MenuItem}
 */
@Target( { ElementType.TYPE, ElementType.METHOD } )
@Retention(RetentionPolicy.RUNTIME)
public @interface ActionCheck {
}
