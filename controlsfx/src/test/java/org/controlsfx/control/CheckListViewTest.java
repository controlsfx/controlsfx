package org.controlsfx.control;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class CheckListViewTest {
    
    private ObservableList<String> items;
    
    private CheckListView<String> nonEmptyCheckListView;
    private CheckListView<String> emptyCheckListView;
    
    @Before public void setup() {
        emptyCheckListView = new CheckListView<>();
        
        items = FXCollections.observableArrayList("string1", "string2", "string3");
        nonEmptyCheckListView = new CheckListView<>(items);
    }

    @Test public void emptyCtor_checkModelIsNotNull() {
        assertNotNull(emptyCheckListView.getCheckModel());
    }
    
    @Test public void itemsCtor_checkModelIsNotNull() {
        assertNotNull(nonEmptyCheckListView.getCheckModel());
    }
}
