package org.controlsfx.control.breadcrumbs;


/**
 * Simple implementation of {@link IBreadCrumbModel}
 * @author IsNull
 *
 */
public class SimpleBreadCrumbModel implements IBreadCrumbModel {

	private final String name;

	public SimpleBreadCrumbModel(String name){
		this.name = name;
	}

	@Override
	public final String getName() {
		return name;
	}

	@Override
	public void activated() {
		System.out.println("SimpleBreadCrumbModel: Crumb " + getName() + " activated!");
	}
}
