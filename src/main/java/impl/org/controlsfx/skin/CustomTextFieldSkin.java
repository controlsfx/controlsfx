package impl.org.controlsfx.skin;

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
                control.pseudoClassStateChanged(HAS_FOCUS, textField.isFocused());
            }
        });
        
        updateChildren();
        
        registerChangeListener(control.leftProperty(), "LEFT_NODE");
        registerChangeListener(control.rightProperty(), "RIGHT_NODE");
    }
    
    @Override protected void handleControlPropertyChanged(String p) {
        super.handleControlPropertyChanged(p);
        
        if (p == "LEFT_NODE" || p == "RIGHT_NODE") {
            updateChildren();
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
