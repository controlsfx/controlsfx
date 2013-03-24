package org.controlsfx.control.cell;

import org.controlsfx.control.GridCell;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

public class DefaultGridCell<T> extends GridCell<T> {

	public DefaultGridCell() {
	    // no-op
	}
	
	@Override protected void updateItem(T item, boolean empty) {
		super.updateItem(item, empty);
		if(empty) {
			setText("");
		} else {
			setText(item.toString());
		}
	}
}
