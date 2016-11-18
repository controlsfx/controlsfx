/*
 * Copyright 2016-2016 Michael Hoffer <info@michaelhoffer.de>. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 *
 *    1. Redistributions of source code must retain the above copyright notice, this list of
 *       conditions and the following disclaimer.
 *
 *    2. Redistributions in binary form must reproduce the above copyright notice, this list
 *       of conditions and the following disclaimer in the documentation and/or other materials
 *       provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY Michael Hoffer <info@michaelhoffer.de> "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL Michael Hoffer <info@michaelhoffer.de> OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are those of the
 * authors and should not be interpreted as representing official policies, either expressed
 * or implied, of Michael Hoffer <info@michaelhoffer.de>.
 */
package org.controlsfx.control;

import javafx.beans.DefaultProperty;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.Pane;

import javafx.scene.layout.Region;

import javafx.scene.transform.Scale;

/**
 * Scales content to always fit in the bounds of this pane. Useful for workflows
 * with lots of windows.
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
@DefaultProperty("content")
public class ScalableContentPane extends Region {

    public enum ScaleBehavior {
        /**
         * Always scale the content.
         */
        ALWAYS,
        /**
         * Rescale content only if requested by the layout properties of the content
         * node.
         */
        IF_NECESSARY
    }

    private Scale contentScaleTransform;
    private final Property<Node> contentPaneProperty
            = new SimpleObjectProperty<>();
    private double contentScaleWidth = 1.0;
    private double contentScaleHeight = 1.0;
    private boolean aspectScale = true;
    private boolean autoRescale = true;
    private final DoubleProperty minScaleXProperty
            = new SimpleDoubleProperty(Double.MIN_VALUE);
    private final DoubleProperty maxScaleXProperty
            = new SimpleDoubleProperty(Double.MAX_VALUE);
    private final DoubleProperty minScaleYProperty
            = new SimpleDoubleProperty(Double.MIN_VALUE);
    private final DoubleProperty maxScaleYProperty
            = new SimpleDoubleProperty(Double.MAX_VALUE);

    private final BooleanProperty fitToWidthProperty
            = new SimpleBooleanProperty(true);
    private final BooleanProperty fitToHeightProperty
            = new SimpleBooleanProperty(true);

    private final ObjectProperty<ScaleBehavior> scaleBehavior
            = new SimpleObjectProperty<>(ScaleBehavior.ALWAYS);

    private boolean manualReset;

    /**
     * Constructor.
     */
    public ScalableContentPane() {
        setContent(new Pane());

        needsLayoutProperty().addListener((ov, oldV, newV) -> {
            if (newV
                    && (getWidth() <= getPrefWidth()
                    || getHeight() <= getPrefHeight())
                    || getPrefWidth() == USE_COMPUTED_SIZE
                    || getPrefHeight() == USE_COMPUTED_SIZE) {
                computeScale();
            }
        });

        fitToWidthProperty().addListener((ov, oldValue, newValue) -> {
            requestLayout();
        });

        fitToHeightProperty().addListener((ov, oldValue, newValue) -> {
            requestLayout();
        });

        scaleBehaviorProperty().addListener((ov, oldV, newV) -> {
            requestLayout();
        });
    }

    /**
     * Creates a new instance with the specified content node.
     * @param content content node to scale
     */
    public ScalableContentPane(Node content) {
        this();
        setContent(content);
    }
    
    

    /**
     * @return the content pane
     */
    public Node getContent() {
        return contentPaneProperty.getValue();
    }

    /**
     * Defines the content pane of this scalable pane.
     *
     * @param content content node to scale
     */
    public final void setContent(Node content) {
        contentPaneProperty.setValue(content);
        content.setManaged(false);
        initContentPaneListener();

        contentScaleTransform = new Scale(1, 1);
        getContentScaleTransform().setPivotX(0);
        getContentScaleTransform().setPivotY(0);
        getContentScaleTransform().setPivotZ(0);
        getContent().getTransforms().add(getContentScaleTransform());

        getChildren().add(content);

        ChangeListener<Number> changeListener = (ov, oldValue, newValue) -> {
            requestScale();
        };

        getContentScaleTransform().setOnTransformChanged((event) -> {
            requestLayout();
        });

        minScaleXProperty().addListener(changeListener);
        minScaleYProperty().addListener(changeListener);
        maxScaleXProperty().addListener(changeListener);
        maxScaleYProperty().addListener(changeListener);

    }

    /**
     * Returns the content pane property.
     *
     * @return the content pane property
     */
    public Property<Node> contentProperty() {
        return contentPaneProperty;
    }

    /**
     * Returns the content scale transform.
     *
     * @return the content scale transform
     */
    public final Scale getContentScaleTransform() {
        return contentScaleTransform;
    }

    @Override
    protected void layoutChildren() {
        super.layoutChildren();
    }

    private void computeScale() {
        double realWidth
                = getContent().prefWidth(getLayoutBounds().getHeight());

        double realHeight
                = getContent().prefHeight(getLayoutBounds().getWidth());

        double leftAndRight = getInsets().getLeft() + getInsets().getRight();
        double topAndBottom = getInsets().getTop() + getInsets().getBottom();

        double contentWidth
                = getLayoutBounds().getWidth() - leftAndRight;
        double contentHeight
                = getLayoutBounds().getHeight() - topAndBottom;

        contentScaleWidth = contentWidth / realWidth;
        contentScaleHeight = contentHeight / realHeight;

        contentScaleWidth = Math.max(contentScaleWidth, getMinScaleX());
        contentScaleWidth = Math.min(contentScaleWidth, getMaxScaleX());

        contentScaleHeight = Math.max(contentScaleHeight, getMinScaleY());
        contentScaleHeight = Math.min(contentScaleHeight, getMaxScaleY());

        double resizeScaleW;
        double resizeScaleH;

        boolean partOfSceneGraph = true;

        if (isAspectScale()) {
            double scale = Math.min(contentScaleWidth, contentScaleHeight);

            if (getScaleBehavior() == ScaleBehavior.ALWAYS || manualReset) {

                getContentScaleTransform().setX(scale);
                getContentScaleTransform().setY(scale);

            } else if (getScaleBehavior() == ScaleBehavior.IF_NECESSARY) {
                if (scale < getContentScaleTransform().getX()
                        && getLayoutBounds().getWidth() > 0 
                        && partOfSceneGraph) {

                    getContentScaleTransform().setX(scale);
                    getContentScaleTransform().setY(scale);
                }
            }

        } else if (getScaleBehavior() == ScaleBehavior.ALWAYS || manualReset) {

            getContentScaleTransform().setX(contentScaleWidth);
            getContentScaleTransform().setY(contentScaleHeight);

        } else if (getScaleBehavior() == ScaleBehavior.IF_NECESSARY) {
            if (contentScaleWidth < getContentScaleTransform().getX()
                    && getLayoutBounds().getWidth() > 0 && partOfSceneGraph) {
                getContentScaleTransform().setX(contentScaleWidth);

            }
            if (contentScaleHeight < getContentScaleTransform().getY()
                    && getLayoutBounds().getHeight() > 0 && partOfSceneGraph) {
                getContentScaleTransform().setY(contentScaleHeight);

            }
        }

        resizeScaleW = getContentScaleTransform().getX();
        resizeScaleH = getContentScaleTransform().getY();

        getContent().relocate(
                getInsets().getLeft(), getInsets().getTop());

        double realContentWidth;
        double realContentHeight;

        if (isFitToWidth()) {
            realContentWidth = contentWidth / resizeScaleW;
        } else {
            realContentWidth = contentWidth / contentScaleWidth;
        }

        if (isFitToHeight()) {
            realContentHeight = contentHeight / resizeScaleH;
        } else {
            realContentHeight = contentHeight / contentScaleHeight;
        }

        getContent().resize(realContentWidth, realContentHeight);

    }

    /**
     * Requests scale computation. <b>Note:</b> Usually, this will be performed
     * automatically. If calling this method is necessary, it is likely that you
     * found a bug that should probably be reported.
     */
    public void requestScale() {
        computeScale();
    }

    /**
     * Resets the scale that is applied to the content node.
     */
    public void resetScale() {

        if (manualReset) {
            return;
        }

        manualReset = true;

        try {
            computeScale();
        } finally {
            manualReset = false;
        }
    }

    @Override
    protected double computeMinWidth(double d) {

        double result = getInsets().getLeft() + getInsets().getRight();

        // apply content width (including scale)
        result += getContent().prefWidth(d) * getMinScaleX();

        return result;
    }

    @Override
    protected double computeMinHeight(double d) {

        double result = getInsets().getTop() + getInsets().getBottom();

        // apply content width (including scale)
        result += getContent().prefHeight(d) * getMinScaleY();

        return result;
    }

    @Override
    protected double computePrefWidth(double d) {

        double result = getInsets().getLeft() + getInsets().getRight();

        // apply content width (including scale)
        result += getContent().prefWidth(d) * contentScaleWidth;

        return result;
    }

    @Override
    protected double computePrefHeight(double d) {

        double result = getInsets().getTop() + getInsets().getBottom();

        // apply content width (including scale)
        result += getContent().prefHeight(d) * contentScaleHeight;

        return result;
    }

    private void initContentPaneListener() {

        final ChangeListener<Bounds> boundsListener =
                (ov, oldValue, newValue) -> {
            if (isAutoRescale()) {
                setNeedsLayout(false);
                if (getContent() instanceof Region) {
                    ((Region) getContent()).requestLayout();
                }

                requestLayout();
            }
        };

        final ChangeListener<Number> numberListener =
                (ov, oldValue, newValue) -> {
            if (isAutoRescale()) {
                setNeedsLayout(false);
                if (getContent() instanceof Parent) {
                    ((Parent) getContent()).requestLayout();
                }
                requestLayout();
            }
        };

        if (getContent() instanceof Pane) {
            ((Pane) getContent()).getChildren().addListener(
                    (ListChangeListener.Change<? extends Node> c) -> {
                        while (c.next()) {
                            // TODO handle permutation
//                if (c.wasPermutated()) {
//                    for (int i = c.getFrom(); i < c.getTo(); ++i) {
//                        //permutate
//                    }
//                } else if (c.wasUpdated()) {
//                    //update item
//                } else 

                            if (c.wasRemoved()) {
                                for (Node n : c.getRemoved()) {
                                    n.boundsInLocalProperty().
                                            removeListener(boundsListener);
                                    n.layoutXProperty().
                                            removeListener(numberListener);
                                    n.layoutYProperty().
                                            removeListener(numberListener);
                                }
                            } else if (c.wasAdded()) {
                                for (Node n : c.getAddedSubList()) {
                                    n.boundsInLocalProperty().
                                            addListener(boundsListener);
                                    n.layoutXProperty().
                                            addListener(numberListener);
                                    n.layoutYProperty().
                                            addListener(numberListener);
                                }
                            }

                        }
                    });
        }
    }

    /**
     * Defines whether to keep aspect ration when scaling content.
     *
     * @return <code>true</code> if keeping aspect ratio of the content;
     * <code>false</code> otherwise
     */
    public boolean isAspectScale() {
        return aspectScale;
    }

    /**
     * Defines whether to keep aspect ration of the content.
     *
     * @param aspectScale the state to set
     */
    public void setAspectScale(boolean aspectScale) {
        this.aspectScale = aspectScale;
    }

    /**
     * Indicates whether content is automatically scaled.
     *
     * @return <code>true</code> if content is automatically scaled;
     * <code>false</code> otherwise
     */
    public boolean isAutoRescale() {
        return autoRescale;
    }

    /**
     * Defines whether to automatically rescale content.
     *
     * @param autoRescale the state to set
     */
    public void setAutoRescale(boolean autoRescale) {
        this.autoRescale = autoRescale;
    }

    public DoubleProperty minScaleXProperty() {
        return minScaleXProperty;
    }

    public DoubleProperty minScaleYProperty() {
        return minScaleYProperty;
    }

    public DoubleProperty maxScaleXProperty() {
        return maxScaleXProperty;
    }

    public DoubleProperty maxScaleYProperty() {
        return maxScaleYProperty;
    }

    public double getMinScaleX() {
        return minScaleXProperty().get();
    }

    public double getMaxScaleX() {
        return maxScaleXProperty().get();
    }

    public double getMinScaleY() {
        return minScaleYProperty().get();
    }

    public double getMaxScaleY() {
        return maxScaleYProperty().get();
    }

    public void setMinScaleX(double s) {
        minScaleXProperty().set(s);
    }

    public void setMaxScaleX(double s) {
        maxScaleXProperty().set(s);
    }

    public void setMinScaleY(double s) {
        minScaleYProperty().set(s);
    }

    public void setMaxScaleY(double s) {
        maxScaleYProperty().set(s);
    }

    public void setFitToWidth(boolean value) {
        fitToWidthProperty().set(value);
    }

    public boolean isFitToWidth() {
        return fitToWidthProperty().get();
    }

    public final BooleanProperty fitToWidthProperty() {
        return fitToWidthProperty;
    }

    public void setFitToHeight(boolean value) {
        fitToHeightProperty().set(value);
    }

    public final BooleanProperty fitToHeightProperty() {
        return fitToHeightProperty;
    }

    public boolean isFitToHeight() {
        return fitToHeightProperty().get();
    }

    public final ObjectProperty<ScaleBehavior> scaleBehaviorProperty() {
        return scaleBehavior;
    }

    public void setScaleBehavior(ScaleBehavior behavior) {
        scaleBehaviorProperty().set(behavior);
    }

    public ScaleBehavior getScaleBehavior() {
        return scaleBehaviorProperty().get();
    }
}