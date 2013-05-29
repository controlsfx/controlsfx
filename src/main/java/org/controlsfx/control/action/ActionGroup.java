package org.controlsfx.control.action;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;

public class ActionGroup extends AbstractAction {
    
    public ActionGroup(String text, Action... actions) {
        super(text);
        getActions().addAll(actions);
    }

    @Override 
    public final void execute(ActionEvent ae) {
    }

    // --- actions
    private final ObservableList<Action> actions = FXCollections.<Action> observableArrayList();

    public final ObservableList<Action> getActions() {
        return actions;
    }
    
}
