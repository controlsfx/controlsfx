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
package impl.org.controlsfx.skin;

import java.util.Collections;

import javafx.collections.ObservableList;
import javafx.scene.Node;

import org.controlsfx.control.NotificationPane;
import org.controlsfx.control.action.Action;

import com.sun.javafx.scene.control.behavior.BehaviorBase;
import com.sun.javafx.scene.control.behavior.KeyBinding;
import com.sun.javafx.scene.control.skin.BehaviorSkinBase;

public class NotificationPaneSkin extends BehaviorSkinBase<NotificationPane, BehaviorBase<NotificationPane>> {
    
    private NotificationBar notificationBar;
    private Node content;
    
    public NotificationPaneSkin(final NotificationPane control) {
        super(control, new BehaviorBase<>(control, Collections.<KeyBinding> emptyList()));
        
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
            
            @Override public double getContainerHeight() {
                return control.getHeight();
            }
            
            @Override public void relocateInParent(double x, double y) {
                notificationBar.relocate(x, y);
            }
        };
        
        updateContent();
        
        registerChangeListener(control.contentProperty(), "CONTENT");
        registerChangeListener(control.textProperty(), "TEXT");
        registerChangeListener(control.graphicProperty(), "GRAPHIC");
        registerChangeListener(control.showingProperty(), "SHOWING");
        registerChangeListener(control.showFromTopProperty(), "SHOW_FROM_TOP");
    }
    
    @Override protected void handleControlPropertyChanged(String p) {
        super.handleControlPropertyChanged(p);
        
        if ("CONTENT".equals(p)) {
            updateContent();
        } else if ("TEXT".equals(p)) {
            notificationBar.label.setText(getSkinnable().getText());
        } else if ("GRAPHIC".equals(p)) {
            notificationBar.label.setGraphic(getSkinnable().getGraphic());
        } else if ("SHOWING".equals(p)) {
            if (getSkinnable().isShowing()) {
                notificationBar.doShow();
            } else {
                notificationBar.doHide();
            }
        } else if ("SHOW_FROM_TOP".equals(p)) {
            if (getSkinnable().isShowing()) {
                getSkinnable().requestLayout();
            }
        }
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
    }
    
    protected double computeMinWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
        return content == null ? 0 : content.minWidth(height);
    };
    
    protected double computeMinHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        return content == null ? 0 : content.minHeight(width);
    };
    
    protected double computePrefWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
        return content == null ? 0 : content.prefWidth(height);
    };
    
    protected double computePrefHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        return content == null ? 0 : content.prefHeight(width);
    };
    
    protected double computeMaxWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
        return content == null ? 0 : content.maxWidth(height);
    };
    
    protected double computeMaxHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        return content == null ? 0 : content.maxHeight(width);
    };
}
