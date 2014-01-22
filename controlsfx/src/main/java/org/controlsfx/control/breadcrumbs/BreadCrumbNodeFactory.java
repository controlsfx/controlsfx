package org.controlsfx.control.breadcrumbs;

/**
 * Represents a crumb factory to create (custom) {@link BreadCrumbButton} instances
 *
 * @param <T>
 */
@FunctionalInterface
public interface BreadCrumbNodeFactory<T> { 
    /**
     * Create a crumb button with the given crumb model
     * @param crumb The crumb model
     * @param index The index of the bread crumb (0 = first)
     * @return
     */
    BreadCrumbButton createBreadCrumbButton(T crumb, int index); 
}