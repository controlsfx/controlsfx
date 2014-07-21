package org.controlsfx.control;

import javafx.collections.ObservableList;

public interface CheckModel<T> {

    /**
     * Returns the item in the given index in the control.
     */
    public T getItem(int index);

    /**
     * Returns the count of items in the control.
     */
    public int getItemCount();

    /**
     * Returns the index of the given item.
     */
    public int getItemIndex(T item);

    /**
     * Returns a read-only list of the currently checked indices in the control.
     */
    public ObservableList<Integer> getCheckedIndices();

    /**
     * Returns a read-only list of the currently checked items in the control.
     */
    public ObservableList<T> getCheckedItems();

    /**
     * Checks all items in the control
     */
    public void checkAll();

    /**
     * Checks the given indices in the control
     */
    public void checkIndices(int... indices);

    /**
     * Unchecks all items in the control
     */
    public void clearChecks();

    /**
     * Unchecks the given index in the control
     */
    public void clearCheck(int index);

    /**
     * Returns true if there are no checked items in the control.
     */
    public boolean isEmpty();

    /**
     * Returns true if the given index represents an item that is checked in the control.
     */
    public boolean isChecked(int index);

    /**
     * Checks the item in the given index in the control.
     */
    public void check(int index);

    /**
     * Checks the given item in the control.
     */
    public void check(T item);

}