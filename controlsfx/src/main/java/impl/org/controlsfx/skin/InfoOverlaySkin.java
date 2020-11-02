/**
 * Copyright (c) 2014, 2016 ControlsFX
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

package impl.org.controlsfx.skin;

import javafx.animation.Animation.Status;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.SkinBase;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.util.Duration;

import org.controlsfx.control.InfoOverlay;

public class InfoOverlaySkin extends SkinBase<InfoOverlay> {
    
    private final ImageView EXPAND_IMAGE = new ImageView(new Image(InfoOverlay.class.getResource("expand.png").toExternalForm())); //$NON-NLS-1$
    private final ImageView COLLAPSE_IMAGE = new ImageView(new Image(InfoOverlay.class.getResource("collapse.png").toExternalForm())); //$NON-NLS-1$
    
    private static final Duration TRANSITION_DURATION = new Duration(350.0);

    private Node content;
    private Label infoLabel;
    private HBox infoPanel;
    private ToggleButton expandCollapseButton;

    // animation support
    private Timeline timeline;
    private DoubleProperty transition = new SimpleDoubleProperty(this, "transition", 0.0) { //$NON-NLS-1$
        @Override protected void invalidated() {
            getSkinnable().requestLayout();
        }
    };

    public InfoOverlaySkin(final InfoOverlay control) {
        super(control);

        // content
        content = control.getContent();
        control.hoverProperty().addListener(new ChangeListener<Boolean>() {
            @Override public void changed(ObservableValue<? extends Boolean> o, Boolean wasHover, Boolean isHover) {
                if (control.isShowOnHover()) {
                    if ((isHover && ! isExpanded()) || (!isHover && isExpanded())) {
                        doToggle();
                    }
                }
            }
        });

        // text
        infoLabel = new Label();
        infoLabel.setWrapText(true);
        infoLabel.setAlignment(Pos.TOP_LEFT);
        infoLabel.getStyleClass().add("info"); //$NON-NLS-1$
        infoLabel.textProperty().bind(control.textProperty());

        // button to expand / collapse the info overlay
        expandCollapseButton = new ToggleButton();
        expandCollapseButton.setMouseTransparent(true);
        expandCollapseButton.visibleProperty().bind(Bindings.not(control.showOnHoverProperty()));
        expandCollapseButton.managedProperty().bind(Bindings.not(control.showOnHoverProperty()));
        updateToggleButton();

        // container for the info overlay and the button
        infoPanel = new HBox(infoLabel, expandCollapseButton);
        infoPanel.setAlignment(Pos.TOP_LEFT);
        infoPanel.setFillHeight(true);
        infoPanel.getStyleClass().add("info-panel"); //$NON-NLS-1$
        infoPanel.setCursor(Cursor.HAND);
        infoPanel.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override public void handle(MouseEvent e) {
                if (! control.isShowOnHover()) {
                    doToggle();
                }
            }
        });

        // adding everything to the scenegraph
        getChildren().addAll(content, infoPanel);
        
        registerChangeListener(control.contentProperty(), e -> {
            getChildren().remove(0);
            getChildren().add(0, getSkinnable().getContent());
            getSkinnable().requestLayout();
        });
    }
    
    private void doToggle() {
        // do animation to show / hide the info panel
        expandCollapseButton.setSelected(!expandCollapseButton.isSelected());
        toggleInfoPanel();
        updateToggleButton();
    }
    
    private boolean isExpanded() {
        return expandCollapseButton.isSelected();
    }

    @Override
    protected void layoutChildren(double contentX, double contentY, double contentWidth, double contentHeight) {
        final double contentPrefHeight = content.prefHeight(contentWidth);
        
        // we calculate the pref width of the expand/collapse button. We will
        // ensure that the button does not get smaller than this.
        final double toggleButtonPrefWidth = expandCollapseButton.prefWidth(-1);
        expandCollapseButton.setMinWidth(toggleButtonPrefWidth);

        // All remaining width goes to the info label
        final Insets infoPanelPadding = infoPanel.getPadding();
        final double infoLabelWidth = snapSize(contentWidth - toggleButtonPrefWidth - 
                infoPanelPadding.getLeft() - infoPanelPadding.getRight());

        // we then can work out the necessary height for the info panel, based on
        // whether it is expanded or not, and given the current state of the animation.
        final double prefInfoPanelHeight = (snapSize(infoLabel.prefHeight(infoLabelWidth)) +
                snapSpace(infoPanel.getPadding().getTop()) +
                snapSpace(infoPanel.getPadding().getBottom())) *
                transition.get();

        infoLabel.setMaxWidth(infoLabelWidth);
        infoLabel.setMaxHeight(prefInfoPanelHeight);

        // position the imageView
        layoutInArea(content, contentX, contentY,
                                contentWidth, contentHeight, -1, HPos.CENTER, VPos.TOP);
        
        // position the infoPanel (the HBox consisting of the Label and ToggleButton)
        layoutInArea(infoPanel, contentX, snapPosition(contentPrefHeight - prefInfoPanelHeight),
                                contentWidth, prefInfoPanelHeight, 0, HPos.CENTER, VPos.BOTTOM);
    }

    private void updateToggleButton() {
        if (expandCollapseButton.isSelected()) {
            expandCollapseButton.getStyleClass().setAll("collapse-button"); //$NON-NLS-1$
            expandCollapseButton.setGraphic(COLLAPSE_IMAGE);
        } else {
            expandCollapseButton.getStyleClass().setAll("expand-button"); //$NON-NLS-1$
            expandCollapseButton.setGraphic(EXPAND_IMAGE);
        }
    }
    
    @Override protected double computePrefHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        double insets = topInset + bottomInset;
        return insets + (content == null ? 0 : content.prefHeight(width));
    }
    
    @Override protected double computePrefWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
        double insets = leftInset + rightInset;
        return insets + (content == null ? 0 : content.prefWidth(height));
    }
    
    @Override protected double computeMaxHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        return computePrefHeight(width, topInset, rightInset, bottomInset, leftInset);
    }
    
    @Override protected double computeMaxWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
        return computePrefWidth(height, topInset, rightInset, bottomInset, leftInset);
    }
    
    private void toggleInfoPanel() {
        // animate!
        // The best way I know how is to transition a number between 0.0 and 1.0
        // over a set duration, and have this request layout as it goes. Then,
        // use this value and multiply it against the actualInfoPanelHeight
        // variable in layoutChildren - this will give a nice smooth animation.
        if (content == null) {
            return;
        }

        Duration duration;
        if (timeline != null && (timeline.getStatus() != Status.STOPPED)) {
            duration = timeline.getCurrentTime();
            timeline.stop();
        } else {
            duration = TRANSITION_DURATION;
        }

        timeline = new Timeline();
        timeline.setCycleCount(1);

        KeyFrame k1, k2;

        if (isExpanded()) {
            k1 = new KeyFrame(Duration.ZERO, new KeyValue(transition, 0));
            k2 = new KeyFrame(duration,new KeyValue(transition, 1, Interpolator.LINEAR));
        } else {
            k1 = new KeyFrame(Duration.ZERO, new KeyValue(transition, 1));
            k2 = new KeyFrame(duration, new KeyValue(transition, 0, Interpolator.LINEAR));
        }

        timeline.getKeyFrames().setAll(k1, k2);
        timeline.play();
    }
}
