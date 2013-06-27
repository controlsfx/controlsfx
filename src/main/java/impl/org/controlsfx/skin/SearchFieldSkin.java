package impl.org.controlsfx.skin;

import javafx.animation.FadeTransition;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.css.PseudoClass;
import javafx.event.EventHandler;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import org.controlsfx.control.CustomTextField;
import org.controlsfx.control.SearchField;

import com.sun.javafx.scene.control.behavior.BehaviorBase;
import com.sun.javafx.scene.control.skin.BehaviorSkinBase;

public class SearchFieldSkin extends BehaviorSkinBase<SearchField, BehaviorBase<SearchField>>{
    
    private static final Duration FADE_DURATION = Duration.millis(350);
    private final FadeTransition fader;
    
    private static final PseudoClass HAS_FOCUS = PseudoClass.getPseudoClass("text-field-has-focus");
    
    private final CustomTextField customTextField;
    private final TextField textField;
    
    private boolean clearButtonShowing = false;
    private final StackPane clearButtonPane;
    private final Region clearButton;
    
    public SearchFieldSkin(final SearchField control) {
        super(control, new BehaviorBase<>(control));
        
        customTextField = new CustomTextField();
        textField = customTextField.getTextField();
        customTextField.focusedProperty().addListener(new InvalidationListener() {
            @Override public void invalidated(Observable arg0) {
                control.pseudoClassStateChanged(HAS_FOCUS, customTextField.isFocused());
            }
        });
        
        clearButton = new Region();
        clearButton.getStyleClass().addAll("graphic");
        clearButtonPane = new StackPane(clearButton);
        clearButtonPane.getStyleClass().addAll("clear-button");
        clearButtonPane.setOpacity(0.0);
//        Tooltip.install(clearButtonPane, new Tooltip("Clear the search text"));
        
        clearButtonPane.setOnMouseReleased(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent e) {
                textField.clear();
            }
        });
        
        customTextField.setRight(clearButtonPane);
        
        fader = new FadeTransition(FADE_DURATION, clearButtonPane);
        fader.setCycleCount(1);
        
        textField.textProperty().addListener(new InvalidationListener() {
            @Override public void invalidated(Observable arg0) {
                updateClearButton();
            }
        });
        
        getChildren().setAll(customTextField);
    }
    
    private void updateClearButton() {
        String text = textField.getText();
        
        if ((text == null || text.isEmpty()) && clearButtonShowing) {
            // hide clear button
            fader.setFromValue(1.0);
            fader.setToValue(0.0);
            fader.play();
            clearButtonShowing = false;
        } else if ((text != null && ! text.isEmpty()) && ! clearButtonShowing) {
            // show clear button
            fader.setFromValue(0.0);
            fader.setToValue(1.0);
            fader.play();
            clearButtonShowing = true;
        }
    }
    
    @Override protected void layoutChildren(double x, double y, double w, double h) {
        customTextField.resizeRelocate(x, y, w, h);
    }
    
    @Override
    protected double computePrefWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
        return customTextField.prefWidth(height);
    }
}
