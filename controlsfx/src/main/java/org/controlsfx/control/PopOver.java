/**
 * Copyright (c) 2013, ControlsFX
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

import static javafx.stage.PopupWindow.AnchorLocation.CONTENT_TOP_LEFT;
import impl.org.controlsfx.skin.PopOverSkin;
import javafx.animation.FadeTransition;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.WeakInvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WeakChangeListener;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.Skin;
import javafx.stage.Popup;
import javafx.stage.Window;
import javafx.util.Duration;

/**
 * The PopOver control provides detailed information about an owning node in a
 * popup window. The popup window has a very lightweight appearance (no default
 * window decorations) and an arrow pointing at the owner. Due to the nature of
 * popup windows the PopOver will move around with the parent window when the
 * user drags it. <br>
 * <center> <img src="popover.png"/> </center> <br>
 * The PopOver can be detached from the owning node by dragging it away from the
 * owner. It stops displaying an arrow and starts displaying a title and a close
 * icon. <br>
 * <br>
 * <center> <img src="popover-detached.png"/> </center> <br>
 * The following image shows a popover with an accordion content node. PopOver
 * controls are automatically resizing themselves when the content node changes
 * its size.<br>
 * <br>
 * <center> <img src="popover-accordion.png"/> </center> <br>
 */
public class PopOver extends Control {

    private static final String DEFAULT_STYLE_CLASS = "popover";

    private static final Duration DEFAULT_FADE_IN_DURATION = Duration
            .seconds(.2);

    /**
     * Creates a pop over with a label as the content node.
     */
    public PopOver() {
        super();

        getStyleClass().add(DEFAULT_STYLE_CLASS);

        /*
         * Create some initial content.
         */
        Label label = new Label("<No Content>");
        label.setPadding(new Insets(4));
        setContent(label);

        /*
         * The min width and height equal 2 * corner radius + 2 * arrow indent +
         * 2 * arrow size.
         */
        minWidthProperty().bind(
                Bindings.add(Bindings.multiply(2, arrowSizeProperty()),
                        Bindings.add(
                                Bindings.multiply(2, cornerRadiusProperty()),
                                Bindings.multiply(2, arrowIndentProperty()))));

        minHeightProperty().bind(minWidthProperty());

        ChangeListener<Object> repositionListener = new ChangeListener<Object>() {
            @Override
            public void changed(ObservableValue<? extends Object> value,
                    Object oldObject, Object newObject) {
                if (isShowing() && !isDetached()) {
                    show(getOwner(), targetX, targetY, Duration.ONE);
                }
            }
        };

        arrowSize.addListener(repositionListener);
        cornerRadius.addListener(repositionListener);
        arrowLocation.addListener(repositionListener);
        arrowIndent.addListener(repositionListener);
    }

    /**
     * Creates a pop over with the given node as the content node.
     * 
     * @param the
     *            content shown by the pop over
     */
    public PopOver(Node content) {
        setContent(content);
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new PopOverSkin(this);
    }

    // Content support.

    private final ObjectProperty<Node> content = new SimpleObjectProperty<>(
            this, "content");

    /**
     * Returns the content shown by the pop over.
     * 
     * @return the content node property
     */
    public final ObjectProperty<Node> contentProperty() {
        return content;
    }

    /**
     * Returns the value of the content property
     * 
     * @return the content node
     * 
     * @see #contentProperty()
     */
    public final Node getContent() {
        return contentProperty().get();
    }

    /**
     * Sets the value of the content property.
     * 
     * @param content
     *            the new content node value
     * 
     * @see #contentProperty()
     */
    public final void setContent(Node content) {
        contentProperty().set(content);
    }

    // Owner support.

    private final ObjectProperty<Node> owner = new SimpleObjectProperty<>(this,
            "owner");

    /**
     * Points to the node that "owns" the pop over. If the owner changes its
     * location the pop over will automatically disappear.
     * 
     * @return the owner property
     */
    public final ObjectProperty<Node> ownerProperty() {
        return owner;
    }

    /**
     * Returns the owner property.
     * 
     * @return the owner property
     * 
     * @see #ownerProperty()
     */
    public final Node getOwner() {
        return ownerProperty().get();
    }

    /**
     * Sets the value of the owner property.
     * 
     * @param owner
     * 
     * @see #ownerProperty()
     */
    public final void setOwner(Node owner) {
        ownerProperty().set(owner);
    }

    // The popup that displays the pop over when the pop over is "attached"
    private Popup popup;

    private InvalidationListener hideListener = new InvalidationListener() {
        @Override
        public void invalidated(Observable observable) {
            if (!isDetached()) {
                hide();
            }
        }
    };

    private WeakInvalidationListener weakHideListener = new WeakInvalidationListener(
            hideListener);

    private ChangeListener<Number> xListener = new ChangeListener<Number>() {
        @Override
        public void changed(ObservableValue<? extends Number> value,
                Number oldX, Number newX) {
            popup.setX(popup.getX() + (newX.doubleValue() - oldX.doubleValue()));
        }
    };

    private WeakChangeListener<Number> weakXListener = new WeakChangeListener<>(
            xListener);

    private ChangeListener<Number> yListener = new ChangeListener<Number>() {
        @Override
        public void changed(ObservableValue<? extends Number> value,
                Number oldY, Number newY) {
            popup.setY(popup.getY() + (newY.doubleValue() - oldY.doubleValue()));
        }
    };

    private WeakChangeListener<Number> weakYListener = new WeakChangeListener<>(
            yListener);

    private Window ownerWindow;

    /**
     * Makes the pop over visible at the give location and associates it with
     * the given owner node. The x and y coordinate will be the target location
     * of the arrow of the pop over and not the location of the window.
     * 
     * @param owner
     *            the owning node
     * @param x
     *            the x coordinate for the pop over arrow tip
     * @param y
     *            the y coordinate for the pop over arrow tip
     */
    public final void show(Node owner, double x, double y) {
        show(owner, x, y, DEFAULT_FADE_IN_DURATION);
    }

    /**
     * Makes the pop over visible at the give location and associates it with
     * the given owner node. The x and y coordinate will be the target location
     * of the arrow of the pop over and not the location of the window.
     * 
     * @param owner
     *            the owning node
     * @param x
     *            the x coordinate for the pop over arrow tip
     * @param y
     *            the y coordinate for the pop over arrow tip
     * @param fadeInDuration
     *            the time it takes for the pop over to be fully visible
     */
    public final void show(Node owner, double x, double y,
            Duration fadeInDuration) {

        targetX = x;
        targetY = y;

        setDetached(false);

        if (owner == null) {
            throw new IllegalArgumentException("owner can not be null");
        }

        if (fadeInDuration == null) {
            fadeInDuration = DEFAULT_FADE_IN_DURATION;
        }

        if (!owner.equals(getOwner())) {
            if (getOwner() != null) {
                getOwner().boundsInLocalProperty().removeListener(
                        weakHideListener);
                getOwner().boundsInParentProperty().removeListener(
                        weakHideListener);
            }
            owner.boundsInLocalProperty().addListener(weakHideListener);
            owner.boundsInParentProperty().addListener(weakHideListener);

            setOwner(owner);
        }

        if (popup == null) {
            popup = new Popup();
            popup.setAnchorLocation(CONTENT_TOP_LEFT);
            popup.autoFixProperty().bind(autoFixProperty());
            popup.getContent().add(this);
        }

        /*
         * This is all needed because children windows do not get their x and y
         * coordinate updated when the owning window gets moved by the user.
         */
        if (ownerWindow != null) {
            ownerWindow.xProperty().removeListener(weakXListener);
            ownerWindow.yProperty().removeListener(weakYListener);
            ownerWindow.widthProperty().removeListener(weakHideListener);
            ownerWindow.heightProperty().removeListener(weakHideListener);
        }

        ownerWindow = owner.getScene().getWindow();
        ownerWindow.xProperty().addListener(weakXListener);
        ownerWindow.yProperty().addListener(weakYListener);
        ownerWindow.widthProperty().addListener(weakHideListener);
        ownerWindow.heightProperty().addListener(weakHideListener);

        double xLocation = computePopOverXLocation(x);
        double yLocation = computePopOverYLocation(y);

        setOpacity(0);

        popup.show(owner, xLocation, yLocation);

        FadeTransition fadeIn = new FadeTransition(fadeInDuration, this);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();
    }

    private double targetX;

    private double targetY;

    private double computePopOverXLocation(double targetX) {
        switch (getArrowLocation()) {
        case LEFT_TOP:
        case LEFT_BOTTOM:
        case LEFT_CENTER:
            return targetX + getArrowSize();
        case RIGHT_TOP:
        case RIGHT_CENTER:
        case RIGHT_BOTTOM:
            return targetX - prefWidth(-1) - getArrowSize();
        case TOP_LEFT:
        case BOTTOM_LEFT:
            return targetX - getCornerRadius() - getArrowIndent()
                    - getArrowSize();
        case TOP_CENTER:
        case BOTTOM_CENTER:
            return targetX - prefWidth(-1) / 2;
        case TOP_RIGHT:
        case BOTTOM_RIGHT:
            return targetX - prefWidth(-1) + getArrowIndent()
                    + getCornerRadius() + getArrowSize();
        default:
            return targetX;
        }
    }

    private double computePopOverYLocation(double targetY) {
        switch (getArrowLocation()) {
        case LEFT_TOP:
        case RIGHT_TOP:
            return targetY - getCornerRadius() - getArrowIndent()
                    - getArrowSize();
        case LEFT_BOTTOM:
        case RIGHT_BOTTOM:
            return targetY - prefHeight(-1) + getCornerRadius()
                    + getArrowIndent() + getArrowSize();
        case LEFT_CENTER:
        case RIGHT_CENTER:
            return targetY - prefHeight(-1) / 2;
        case TOP_LEFT:
        case TOP_CENTER:
        case TOP_RIGHT:
            return targetY + getArrowSize();
        case BOTTOM_LEFT:
        case BOTTOM_CENTER:
        case BOTTOM_RIGHT:
            return targetY - prefHeight(-1) - getArrowSize();
        default:
            return targetY;
        }

    }

    private final BooleanProperty autoFix = new SimpleBooleanProperty(this,
            "autoFix", true);

    /**
     * Determines if the popup window used by the PopOver will automatically fix
     * itself if the requested bounds would make it end up outside the screen
     * bounds.
     * 
     * @return the autofix property
     */
    public final BooleanProperty autoFixProperty() {
        return autoFix;
    }

    /**
     * Sets the value of the auto fix property.
     * 
     * @param fix
     *            if true the popup window used by the PopOver will fix its
     *            bounds to always be completely visible on screen
     * 
     * @see #autoFixProperty()
     */
    public final void setAutoFix(boolean fix) {
        autoFixProperty().set(fix);
    }

    /**
     * Returns the value of the autofix property.
     * 
     * @return true if the popup window will fix its bounds automatically
     * 
     * @see #autoFixProperty()
     */
    public final boolean isAutoFix() {
        return autoFixProperty().get();
    }

    /**
     * Determines if the pop over is currently showing on the screen, indpendent
     * of whether the pop over is detached or not.
     * 
     * @return true if the pop over is currently showing on the screen
     */
    public final boolean isShowing() {
        return popup != null && popup.isShowing();
    }

    /**
     * Hides the window that currently displays the pop over. This can either be
     * the popup window (in attached mode) or a stage window (in detached mode).
     * 
     * @see Window#hide()
     */
    public final void hide() {
        if (isShowing()) {
            popup.hide();
            setDetached(false);
        }
    }

    /**
     * Detaches the pop over from the owning node. The pop over will no longer
     * display an arrow pointing at the owner node.
     */
    public final void detach() {
        if (isDetachable()) {
            setDetached(true);
        }
    }

    @Override
    protected String getUserAgentStylesheet() {
        return PopOver.class.getResource("popover.css").toExternalForm();
    }

    // detach support

    private final BooleanProperty detachable = new SimpleBooleanProperty(this,
            "detachable", true);

    /**
     * Determines if the pop over is detachable at all.
     */
    public final BooleanProperty detachableProperty() {
        return detachable;
    }

    /**
     * Sets the value of the detachable property.
     * 
     * @param detachable
     *            if true then the user can detach / tear off the pop over
     * 
     * @see #detachableProperty()
     */
    public final void setDetachable(boolean detachable) {
        detachableProperty().set(detachable);
    }

    /**
     * Returns the value of the detachable property.
     * 
     * @return true if the user is allowed to detach / tear off the pop over
     * 
     * @see #detachableProperty()
     */
    public final boolean isDetachable() {
        return detachableProperty().get();
    }

    private final BooleanProperty detached = new SimpleBooleanProperty(this,
            "detached", false);

    /**
     * Determines whether the pop over is detached from the owning node or not.
     * A detached pop over no longer shows an arrow pointing at the owner and
     * features its own title bar.
     * 
     * @return the detached property
     */
    public final BooleanProperty detachedProperty() {
        return detached;
    }

    /**
     * Sets the value of the detached property.
     * 
     * @param detached
     *            if true the pop over will change its apperance to "detached"
     *            mode
     * 
     * @see #detachedProperty();
     */
    public final void setDetached(boolean detached) {
        detachedProperty().set(detached);
    }

    /**
     * Returns the value of the detached property.
     * 
     * @return true if the pop over is currently detached.
     * 
     * @see #detachedProperty();
     */
    public final boolean isDetached() {
        return detachedProperty().get();
    }

    // arrow size support

    // TODO: make styleable

    private final DoubleProperty arrowSize = new SimpleDoubleProperty(this,
            "arrowSize", 12);

    /**
     * Controls the size of the arrow. Default value is 12.
     * 
     * @return the arrow size property
     */
    public final DoubleProperty arrowSizeProperty() {
        return arrowSize;
    }

    /**
     * Returns the value of the arrow size property.
     * 
     * @return the arrow size property value
     * 
     * @see #arrowSizeProperty()
     */
    public final double getArrowSize() {
        return arrowSizeProperty().get();
    }

    /**
     * Sets the value of the arrow size property.
     * 
     * @param size
     *            the new value of the arrow size property
     * 
     * @see #arrowSizeProperty()
     */
    public final void setArrowSize(double size) {
        arrowSizeProperty().set(size);
    }

    // arrow indent support

    // TODO: make styleable

    private final DoubleProperty arrowIndent = new SimpleDoubleProperty(this,
            "arrowIndent", 12);

    /**
     * Controls the distance between the arrow and the corners of the pop over.
     * The default value is 12.
     * 
     * @return the arrow indent property
     */
    public final DoubleProperty arrowIndentProperty() {
        return arrowIndent;
    }

    /**
     * Returns the value of the arrow indent property.
     * 
     * @return the arrow indent value
     * 
     * @see #arrowIndentProperty()
     */
    public final double getArrowIndent() {
        return arrowIndentProperty().get();
    }

    /**
     * Sets the value of the arrow indent property.
     * 
     * @param size
     *            the arrow indent value
     * 
     * @see #arrowIndentProperty()
     */
    public final void setArrowIndent(double size) {
        arrowIndentProperty().set(size);
    }

    // radius support

    // TODO: make styleable

    private final DoubleProperty cornerRadius = new SimpleDoubleProperty(this,
            "cornerRadius", 6);

    /**
     * Returns the corner radius property for the pop over.
     * 
     * @return the corner radius property (default is 6)
     */
    public final DoubleProperty cornerRadiusProperty() {
        return cornerRadius;
    }

    /**
     * Returns the value of the corner radius property.
     * 
     * @return the corner radius
     * 
     * @see #cornerRadiusProperty()
     */
    public final double getCornerRadius() {
        return cornerRadiusProperty().get();
    }

    /**
     * Sets the value of the corner radius property.
     * 
     * @param radius
     *            the corner radius
     * 
     * @see #cornerRadiusProperty()
     */
    public final void setCornerRadius(double radius) {
        cornerRadiusProperty().set(radius);
    }

    // Detached stage title

    private final StringProperty detachedTitle = new SimpleStringProperty(this,
            "detachedTitle", "Info");

    /**
     * Stores the title to display when the pop over becomes detached.
     * 
     * @return the detached title property
     */
    public final StringProperty detachedTitleProperty() {
        return detachedTitle;
    }

    /**
     * Returns the value of the detached title property.
     * 
     * @return the detached title
     * 
     * @see #detachedTitleProperty()
     */
    public final String getDetachedTitle() {
        return detachedTitleProperty().get();
    }

    /**
     * Sets the value of the detached title property.
     * 
     * @param title
     *            the title to use when detached
     * 
     * @see #detachedTitleProperty()
     */
    public final void setDetachedTitle(String title) {
        if (title == null) {
            throw new IllegalArgumentException("title can not be null");
        }

        detachedTitleProperty().set(title);
    }

    private final ObjectProperty<ArrowLocation> arrowLocation = new SimpleObjectProperty<PopOver.ArrowLocation>(
            this, "arrowLocation", ArrowLocation.LEFT_TOP);

    /**
     * Stores the preferred arrow location. This might not be the actual
     * location of the arrow if auto fix is enabled.
     * 
     * @see #setAutoFix(boolean)
     * 
     * @return the arrow location property
     */
    public final ObjectProperty<ArrowLocation> arrowLocationProperty() {
        return arrowLocation;
    }

    /**
     * Sets the value of the arrow location property.
     * 
     * @see #arrowLocationProperty()
     * 
     * @param location
     *            the requested location
     */
    public final void setArrowLocation(ArrowLocation location) {
        arrowLocationProperty().set(location);
    }

    /**
     * Returns the value of the arrow location property.
     * 
     * @see #arrowLocationProperty()
     * 
     * @return the preferred arrow location
     */
    public final ArrowLocation getArrowLocation() {
        return arrowLocationProperty().get();
    }

    /**
     * All possible arrow locations.
     */
    public enum ArrowLocation {
        LEFT_TOP, LEFT_CENTER, LEFT_BOTTOM, RIGHT_TOP, RIGHT_CENTER, RIGHT_BOTTOM, TOP_LEFT, TOP_CENTER, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_CENTER, BOTTOM_RIGHT;
    }
}
