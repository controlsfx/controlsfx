/**
 * Copyright (c) 2014, 2018 ControlsFX
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *     * Neither the name of ControlsFX, any associated website, nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL CONTROLSFX BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.controlsfx.control;

import impl.org.controlsfx.skin.PlusMinusSliderSkin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.collections.MapChangeListener;
import javafx.css.CssMetaData;
import javafx.css.PseudoClass;
import javafx.css.Styleable;
import javafx.css.StyleableObjectProperty;
import javafx.css.StyleableProperty;
import javafx.css.converter.EnumConverter;
import javafx.event.EventHandler;
import javafx.event.EventTarget;
import javafx.event.EventType;
import javafx.geometry.Orientation;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.input.InputEvent;

/**
 * A plus minus slider allows the user to continously fire an event carrying a
 * value between -1 and +1 by moving a thumb from its center position to the
 * left or right (or top and bottom) edge of the control. The thumb will
 * automatically center itself again on the zero position when the user lets go
 * of the mouse button. Scrolling through a large list of items at different
 * speeds is one possible use case for a control like this.
 * 
 * <center> <img src="plus-minus-slider.png" alt="Screenshot of PlusMinusSlider"> </center>
 */
public class PlusMinusSlider extends ControlsFXControl {

    private static final String DEFAULT_STYLE_CLASS = "plus-minus-slider"; //$NON-NLS-1$

    private static final PseudoClass VERTICAL_PSEUDOCLASS_STATE = PseudoClass.getPseudoClass("vertical"); //$NON-NLS-1$
    private static final PseudoClass HORIZONTAL_PSEUDOCLASS_STATE = PseudoClass.getPseudoClass("horizontal"); //$NON-NLS-1$

    /**
     * Constructs a new adjuster control with a default horizontal orientation.
     */
    public PlusMinusSlider() {
        getStyleClass().add(DEFAULT_STYLE_CLASS);

        setOrientation(Orientation.HORIZONTAL);

        /*
         * We are "abusing" the properties map to pass the new value of the
         * slider from the skin to the control. It has to be done this way
         * because the value property of this control is "read-only".
         */
        getProperties().addListener(new MapChangeListener<Object, Object>() {
            @Override
            public void onChanged(MapChangeListener.Change<? extends Object, ? extends Object> change) {
                if (change.getKey().equals("plusminusslidervalue")) { //$NON-NLS-1$
                    if (change.getValueAdded() != null) {
                        Double valueAdded = (Double) change.getValueAdded();
                        value.set(valueAdded);
                        change.getMap().remove("plusminusslidervalue"); //$NON-NLS-1$
                    }
                }
            };
        });
    }

    /** {@inheritDoc} */
    @Override public String getUserAgentStylesheet() {
        return getUserAgentStylesheet(PlusMinusSlider.class, "plusminusslider.css");
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new PlusMinusSliderSkin(this);
    }

    private ReadOnlyDoubleWrapper value = new ReadOnlyDoubleWrapper(this,
            "value", 0); //$NON-NLS-1$

    /**
     * Returns the value property of the adjuster. The value is always between
     * -1 and +1.
     * 
     * @return the value of the adjuster
     */
    public final ReadOnlyDoubleProperty valueProperty() {
        return value.getReadOnlyProperty();
    }

    /**
     * Returns the value of the value property.
     * 
     * @return the value of the adjuster [-1, +1]
     * 
     * @see #valueProperty()
     */
    public final double getValue() {
        return valueProperty().get();
    }

    // orientation

    private ObjectProperty<Orientation> orientation;

    /**
     * Sets the value of the orientation property.
     * 
     * @param value
     *            the new orientation (horizontal, vertical).
     * @see #orientationProperty()
     */
    public final void setOrientation(Orientation value) {
        orientationProperty().set(value);
    }

    /**
     * Returns the value of the orientation property.
     * 
     * @return the current orientation of the control
     * @see #orientationProperty()
     */
    public final Orientation getOrientation() {
        return orientation == null ? Orientation.HORIZONTAL : orientation.get();
    }

    /**
     * Returns the stylable object property used for storing the orientation of
     * the adjuster control. The CSS property "-fx-orientation" can be used to
     * initialize this value.
     * 
     * @return the orientation property
     */
    public final ObjectProperty<Orientation> orientationProperty() {
        if (orientation == null) {
            orientation = new StyleableObjectProperty<Orientation>(null) {
                @Override
                protected void invalidated() {
                    final boolean vertical = (get() == Orientation.VERTICAL);
                    pseudoClassStateChanged(VERTICAL_PSEUDOCLASS_STATE,
                            vertical);
                    pseudoClassStateChanged(HORIZONTAL_PSEUDOCLASS_STATE,
                            !vertical);
                }

                @Override
                public CssMetaData<PlusMinusSlider, Orientation> getCssMetaData() {
                    return StyleableProperties.ORIENTATION;
                }

                @Override
                public Object getBean() {
                    return PlusMinusSlider.this;
                }

                @Override
                public String getName() {
                    return "orientation"; //$NON-NLS-1$
                }
            };
        }
        return orientation;
    }

    // event support

    /**
     * Stores the event handler that will be informed when the adjuster's value
     * changes.
     * 
     * @return the value change event handler property
     */
    public final ObjectProperty<EventHandler<PlusMinusEvent>> onValueChangedProperty() {
        return onValueChanged;
    }

    /**
     * Sets an event handler that will receive plus minus events when the user
     * moves the adjuster's thumb.
     * 
     * @param value
     *            the event handler
     * 
     * @see #onValueChangedProperty()
     */
    public final void setOnValueChanged(EventHandler<PlusMinusEvent> value) {
        onValueChangedProperty().set(value);
    }

    /**
     * Returns the event handler that will be notified when the adjuster's value
     * changes.
     * 
     * @return An EventHandler.
     */
    public final EventHandler<PlusMinusEvent> getOnValueChanged() {
        return onValueChangedProperty().get();
    }

    private ObjectProperty<EventHandler<PlusMinusEvent>> onValueChanged = new ObjectPropertyBase<EventHandler<PlusMinusEvent>>() {

        @Override
        protected void invalidated() {
            setEventHandler(PlusMinusEvent.VALUE_CHANGED, get());
        }

        @Override
        public Object getBean() {
            return PlusMinusSlider.this;
        }

        @Override
        public String getName() {
            return "onValueChanged"; //$NON-NLS-1$
        }
    };

    private static class StyleableProperties {

        private static final CssMetaData<PlusMinusSlider, Orientation> ORIENTATION = new CssMetaData<PlusMinusSlider, Orientation>(
                "-fx-orientation", new EnumConverter<>( //$NON-NLS-1$
                        Orientation.class), Orientation.VERTICAL) {

            @Override
            public Orientation getInitialValue(PlusMinusSlider node) {
                // A vertical Slider should remain vertical
                return node.getOrientation();
            }

            @Override
            public boolean isSettable(PlusMinusSlider n) {
                return n.orientation == null || !n.orientation.isBound();
            }

            @SuppressWarnings("unchecked")
            @Override
            public StyleableProperty<Orientation> getStyleableProperty(
                    PlusMinusSlider n) {
                return (StyleableProperty<Orientation>) n.orientationProperty();
            }
        };

        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;
        static {
            final List<CssMetaData<? extends Styleable, ?>> styleables = new ArrayList<>(Control.getClassCssMetaData());
            styleables.add(ORIENTATION);

            STYLEABLES = Collections.unmodifiableList(styleables);
        }
    }

    /**
     * @return The CssMetaData associated with this class, which may include the
     *         CssMetaData of its super classes.
     */
    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return StyleableProperties.STYLEABLES;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CssMetaData<? extends Styleable, ?>> getControlCssMetaData() {
        return getClassCssMetaData();
    }

    /**
     * An event class used by the {@link PlusMinusSlider} to inform event
     * handlers about changes.
     */
    public static class PlusMinusEvent extends InputEvent {

        private static final long serialVersionUID = 2881004583512990781L;

        public static final EventType<PlusMinusEvent> ANY = new EventType<>(
                InputEvent.ANY, "ANY" + UUID.randomUUID().toString()); //$NON-NLS-1$

        /**
         * An event type used when the value property (
         * {@link PlusMinusSlider#valueProperty()}) changes.
         */
        public static final EventType<PlusMinusEvent> VALUE_CHANGED = new EventType<>(
                PlusMinusEvent.ANY, "VALUE_CHANGED" + UUID.randomUUID().toString()); //$NON-NLS-1$

        private double value;

        /**
         * Constructs a new event object.
         * 
         * @param source
         *            the source of the event (always the
         *            {@link PlusMinusSlider})
         * @param target
         *            the target of the event (always the
         *            {@link PlusMinusSlider})
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
         * The value of the {@link PlusMinusSlider}.
         * 
         * @return the value
         */
        public double getValue() {
            return value;
        }
    }
}
