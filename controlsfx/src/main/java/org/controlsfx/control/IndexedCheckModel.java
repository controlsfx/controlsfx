package org.controlsfx.control;

import javafx.collections.ObservableList;

public interface IndexedCheckModel<T> extends CheckModel<T> {

    /**
     * Returns the item in the given index in the control.
     */
    public T getItem(int index);

    /**
     * Returns the index of the given item.
     */
    public int getItemIndex(T item);

    /**
     * Returns a read-only list of the currently checked indices in the control.
     */
    public ObservableList<Integer> getCheckedIndices();

    /**
     * Checks the given indices in the control
     */
    public void checkIndices(int... indices);

    /**
     * Unchecks the given index in the control
     */
    public void clearCheck(int index);

    /**
     * Returns true if the given index represents an item that is checked in the control.
     */
    public boolean isChecked(int index);

    /**
     * Checks the item in the given index in the control.
     */
    public void check(int index);

}