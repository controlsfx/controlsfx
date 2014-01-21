package org.controlsfx.control.breadcrumbs;


/**
 * Model of a single BredCrumb
 *
 */
public interface IBreadCrumbModel {

	/**
	 * Get the display name of the button
	 * @return
	 */
	public String getName();

	/**
	 * Occurs when this bread crumb is activated
	 */
	public void activated();
}
