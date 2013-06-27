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

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.css.PseudoClass;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;

import org.controlsfx.control.CustomTextField;

import com.sun.javafx.scene.control.behavior.BehaviorBase;
import com.sun.javafx.scene.control.skin.BehaviorSkinBase;

public class CustomTextFieldSkin extends BehaviorSkinBase<CustomTextField, BehaviorBase<CustomTextField>> {
    
    private static final PseudoClass HAS_FOCUS = PseudoClass.getPseudoClass("text-field-has-focus");
    
    
    private static final PseudoClass HAS_NO_SIDE_NODE = PseudoClass.getPseudoClass("no-side-nodes");
    private static final PseudoClass HAS_LEFT_NODE = PseudoClass.getPseudoClass("left-node-visible");
    private static final PseudoClass HAS_RIGHT_NODE = PseudoClass.getPseudoClass("right-node-visible");
    
    private Node left;
    private StackPane leftPane;
    private Node right;
    private StackPane rightPane;
    private TextField textField;
    
    private final CustomTextField control;
    
    public CustomTextFieldSkin(final CustomTextField control) {
        super(control, new BehaviorBase<>(control));
        
        this.control = control;
        this.textField = control.getTextField();
        textField.focusedProperty().addListener(new InvalidationListener() {
            @Override public void invalidated(Observable arg0) {
                boolean hasFocus = getSkinnable().isFocused() || textField.isFocused();
                control.pseudoClassStateChanged(HAS_FOCUS, hasFocus);
            }
        });
        
        updateChildren();
        
        registerChangeListener(control.leftProperty(), "LEFT_NODE");
        registerChangeListener(control.rightProperty(), "RIGHT_NODE");
        registerChangeListener(control.focusedProperty(), "FOCUSED");
    }
    
    @Override protected void handleControlPropertyChanged(String p) {
        super.handleControlPropertyChanged(p);
        
        if (p == "LEFT_NODE" || p == "RIGHT_NODE") {
            updateChildren();
        } else if (p == "FOCUSED") {
            Platform.runLater(new Runnable() {
                @Override public void run() {
                    textField.requestFocus();
                }
            });
        }
    }
    
    private void updateChildren() {
        getChildren().setAll(textField);
        
        left = control.getLeft();
        if (left != null) {
            leftPane = new StackPane(left);
            leftPane.setAlignment(Pos.CENTER_LEFT);
            leftPane.getStyleClass().add("left-pane");
            getChildren().add(leftPane);
        }
        
        right = control.getRight();
        if (right != null) {
            rightPane = new StackPane(right);
            rightPane.setAlignment(Pos.CENTER_RIGHT);
            rightPane.getStyleClass().add("right-pane");
            getChildren().add(rightPane);
        }
        
        control.pseudoClassStateChanged(HAS_LEFT_NODE, left != null);
        control.pseudoClassStateChanged(HAS_RIGHT_NODE, right != null);
        control.pseudoClassStateChanged(HAS_NO_SIDE_NODE, left == null && right == null);
    }
    
    @Override protected void layoutChildren(double x, double y, double w, double h) {
        final double fullHeight = h + snappedTopInset() + snappedBottomInset();
        
        final double leftWidth = leftPane == null ? 0.0 : leftPane.prefWidth(fullHeight);
        final double rightWidth = rightPane == null ? 0.0 : rightPane.prefWidth(fullHeight);
        
        final double textFieldStartX = leftWidth;
        final double textFieldWidth = w - x - leftWidth - rightWidth;
        
        textField.resizeRelocate(textFieldStartX, 0, textFieldWidth, fullHeight);
        
        if (leftPane != null) {
            final double leftStartX = 0;
            leftPane.resizeRelocate(leftStartX, 0, leftWidth, fullHeight);
        }
        
        if (rightPane != null) {
            final double rightStartX = rightPane == null ? 0.0 : textFieldStartX + textFieldWidth;
            rightPane.resizeRelocate(rightStartX, 0, rightWidth, fullHeight);
        }
    }
    
    @Override protected double computePrefHeight(double w, double arg1, double arg2, double arg3, double arg4) {
        return textField.prefHeight(w) + snappedTopInset() + snappedBottomInset();
    }
}
