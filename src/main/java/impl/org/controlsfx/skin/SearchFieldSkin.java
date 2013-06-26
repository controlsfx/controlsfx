package impl.org.controlsfx.skin;

import javafx.animation.FadeTransition;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.event.EventHandler;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import org.controlsfx.control.SearchField;

import com.sun.javafx.scene.control.behavior.BehaviorBase;
import com.sun.javafx.scene.control.skin.BehaviorSkinBase;

public class SearchFieldSkin extends BehaviorSkinBase<SearchField, BehaviorBase<SearchField>>{
    
    private static final Duration FADE_DURATION = Duration.millis(350);
    private final FadeTransition fader;
    
    private final TextField textField;
    
    private boolean clearButtonShowing = false;
    private final StackPane clearButtonPane;
    private final Region clearButton;
    
    public SearchFieldSkin(SearchField control) {
        super(control, new BehaviorBase<>(control));
        
        textField = control.getTextField();
        
        clearButton = new Region();
        clearButton.getStyleClass().addAll("graphic");
        clearButtonPane = new StackPane(clearButton);
        clearButtonPane.getStyleClass().addAll("clear-button");
        clearButtonPane.setOpacity(0.0);
        Tooltip.install(clearButtonPane, new Tooltip("Clear the search text"));
        
        clearButtonPane.setOnMouseReleased(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent e) {
                textField.clear();
            }
        });
        
        fader = new FadeTransition(FADE_DURATION, clearButtonPane);
        fader.setCycleCount(1);
        
        control.textProperty().addListener(new InvalidationListener() {
            @Override public void invalidated(Observable arg0) {
                updateClearButton();
            }
        });
        
        getChildren().setAll(textField, clearButtonPane);
    }
    
    private void updateClearButton() {
        String text = getSkinnable().getText();
        
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
        textField.resizeRelocate(x, y, w, h);
        
        double btnWidth = clearButton.prefWidth(h);
        double btnHeight = clearButton.prefHeight(w);
        clearButtonPane.resizeRelocate(w - snappedRightInset() - btnWidth - 8, h / 2.0 - btnHeight / 2.0, btnWidth, btnHeight);
    }
    
    @Override
    protected double computePrefWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
        return textField.prefWidth(height);
    }
}
