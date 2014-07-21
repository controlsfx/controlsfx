package org.controlsfx.control;

import javafx.collections.ObservableList;

public interface CheckModel<T> {
    
    /**
     * Returns the count of items in the control.
     */
    public int getItemCount();

    /**
     * Returns a read-only list of the currently checked items in the control.
     */
    public ObservableList<T> getCheckedItems();

    /**
     * Checks all items in the control
     */
    public void checkAll();
    
    public void clearCheck(T item);
    
    /**
     * Unchecks all items in the control
     */
    public void clearChecks();
    
    /**
     * Returns true if there are no checked items in the control.
     */
    public boolean isEmpty();
    
    public boolean isChecked(T item);
    
    /**
     * Checks the given item in the control.
     */
    public void check(T item);
}
