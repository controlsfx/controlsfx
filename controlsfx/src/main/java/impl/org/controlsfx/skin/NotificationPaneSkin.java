/**
 * Copyright (c) 2013, 2016 ControlsFX
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

import com.sun.javafx.scene.traversal.ParentTraversalEngine;
import impl.org.controlsfx.ReflectionUtils;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.SkinBase;
import javafx.scene.shape.Rectangle;

import org.controlsfx.control.NotificationPane;
import org.controlsfx.control.action.Action;

public class NotificationPaneSkin extends SkinBase<NotificationPane> {

    private NotificationBar notificationBar;
    private Node content;
    private Rectangle clip = new Rectangle();

    public NotificationPaneSkin(final NotificationPane control) {
        super(control);

        notificationBar = new NotificationBar() {
            @Override public void requestContainerLayout() {
                control.requestLayout();
            }

            @Override public String getText() {
                return control.getText();
            }

            @Override public Node getGraphic() {
                return control.getGraphic();
            }

            @Override public ObservableList<Action> getActions() {
                return control.getActions();
            }

            @Override public boolean isShowing() {
                return control.isShowing();
            }

            @Override public boolean isShowFromTop() {
                return control.isShowFromTop();
            }

            @Override public void hide() {
                control.hide();
            }

            @Override public boolean isCloseButtonVisible() {
                    return control.isCloseButtonVisible();
            }

            @Override public double getContainerHeight() {
                return control.getHeight();
            }

            @Override public void relocateInParent(double x, double y) {
                notificationBar.relocate(x, y);
            }
        };

        control.setClip(clip);
        updateContent();

        registerChangeListener(control.heightProperty(), e -> {
            // For resolving https://bitbucket.org/controlsfx/controlsfx/issue/409
            if (getSkinnable().isShowing() && !getSkinnable().isShowFromTop()) {
                notificationBar.requestLayout();
            }
        });
        registerChangeListener(control.contentProperty(), e -> updateContent());
        registerChangeListener(control.textProperty(), e -> notificationBar.label.setText(getSkinnable().getText()));
        registerChangeListener(control.graphicProperty(), e -> notificationBar.label.setGraphic(getSkinnable().getGraphic()));
        registerChangeListener(control.showingProperty(), e -> {
            if (getSkinnable().isShowing()) {
                notificationBar.doShow();
            } else {
                notificationBar.doHide();
            }
        });
        registerChangeListener(control.showFromTopProperty(), e -> {
            if (getSkinnable().isShowing()) {
                getSkinnable().requestLayout();
            }
        });
        registerChangeListener(control.closeButtonVisibleProperty(), e -> notificationBar.updatePane());

        // Fix for Issue #522: Prevent NotificationPane from receiving focus
        ParentTraversalEngine engine = new ParentTraversalEngine(getSkinnable());
        ReflectionUtils.setTraversalEngine(control, engine);
        engine.setOverriddenFocusTraversability(false);
    }
    
    private void updateContent() {
        if (content != null) {
            getChildren().remove(content);
        }
        
        content = getSkinnable().getContent();
        
        if (content == null) {
            getChildren().setAll(notificationBar);
        } else {
            getChildren().setAll(content, notificationBar);
        }
    }
    
    @Override protected void layoutChildren(double x, double y, double w, double h) {
        final double notificationBarHeight = notificationBar.prefHeight(w);
        
        notificationBar.resize(w, notificationBarHeight);
        
        // layout the content
        if (content != null) {
            content.resizeRelocate(x, y, w, h);
        }
        
        // and update the clip so that the notification bar does not draw outside
        // the bounds of the notification pane
        clip.setX(x);
        clip.setY(y);
        clip.setWidth(w);
        clip.setHeight(h);
    }
    
    @Override
    protected double computeMinWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
        return content == null ? 0 : content.minWidth(height);
    };
    
    @Override
    protected double computeMinHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        return content == null ? 0 : content.minHeight(width);
    };
    
    @Override
    protected double computePrefWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
        return content == null ? 0 : content.prefWidth(height);
    };
    
    @Override
    protected double computePrefHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        return content == null ? 0 : content.prefHeight(width);
    };
    
    @Override
    protected double computeMaxWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
        return content == null ? 0 : content.maxWidth(height);
    };
    
    @Override
    protected double computeMaxHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        return content == null ? 0 : content.maxHeight(width);
    };
}
