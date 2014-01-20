package org.controlsfx.control.breadcrumbs;

public interface BreadCrumbNodeFactory<T> { 
	BreadCrumbButton createBreadCrumbButton(T crumb, int index); 
}