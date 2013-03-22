package org.controlsfx.control.cell;

import org.controlsfx.control.GridCell;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

public class DefaultGridCell<T> extends GridCell<T> {

	public DefaultGridCell() {
		itemProperty().addListener(new ChangeListener<T>() {

			@Override
			public void changed(ObservableValue<? extends T> arg0, T arg1,
					T arg2) {
				if(arg2 == null) {
					setText("");
				} else {
					setText(arg2.toString());
				}
			}
		});
	}
	
	@Override
	protected void updateItem(T item, boolean empty) {
		super.updateItem(item, empty);
		if(empty) {
			setText("");
		} else {
			setText(item.toString());
		}
	}
}
