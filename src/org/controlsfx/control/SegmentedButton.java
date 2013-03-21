package org.controlsfx.control;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;

public class SegmentedButton extends HBox {
    
    private ObjectProperty<ObservableList<ToggleButton>> buttons = new ObjectPropertyBase<ObservableList<ToggleButton>>() {
        @Override public Object getBean() {
            return SegmentedButton.this;
        }
        @Override public String getName() {
            return "buttons";
        }
    };
    
    public final void setButtons(ObservableList<ToggleButton> value) { buttons.set(value); }
    public final ObjectProperty<ObservableList<ToggleButton>> buttonsProperty() { return buttons; }
        
    public final ObservableList<ToggleButton> getButtons() {
        return buttons.get();
    }

    final ToggleGroup group = new ToggleGroup();

    public SegmentedButton() {
        this(FXCollections.<ToggleButton>observableArrayList());
    }
    public SegmentedButton(ObservableList<ToggleButton> buttons) {
        getStyleClass().add("segmented-button");
        setButtons(buttons);
        setFocusTraversable(true);
        updateButtons();
        buttons.addListener(new InvalidationListener() {
            @Override public void invalidated(Observable o) {
                updateButtons();
            }
        });
    }

    private void updateButtons() {
        for (int i = 0; i < getButtons().size(); i++) {
            ToggleButton t = getButtons().get(i);
            t.setToggleGroup(group);
            getChildren().add(t);

            if (i == buttons.get().size() - 1) {
                if(i == 0) {
                    t.getStyleClass().add("only-button");
                } else {
                    t.getStyleClass().add("last-button");
                }
            } else if (i == 0) {
                t.getStyleClass().add("first-button");
            } else {
                t.getStyleClass().add("middle-button");
            }
        }
    }
}