package org.controlsfx.control.breadcrumbs;

import impl.org.controlsfx.skin.BreadCrumbBarSkin;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;

/**
 * Represents a bread crumb bar
 * 
 *
 */
public class BreadCrumbBar<T extends IBreadCrumbModel> extends Control {

	private final BreadCrumbNodeFactory<T> defaultCrumbNodeFactory = new BreadCrumbNodeFactory<T>(){
		@Override
		public BreadCrumbButton createBreadCrumbButton(T crumb, int index) {
			return new BreadCrumbButton(crumb.getName(), index == 0);
		}
	};
	
	public BreadCrumbBar(){
		 this(FXCollections.<T> observableArrayList());
	}
	
    public BreadCrumbBar(ObservableList<T> items) {
        getStyleClass().add(DEFAULT_STYLE_CLASS);
        setCrumbs(items);
        setCrumbFactory(defaultCrumbNodeFactory);
    }
    
    
    
    /**
     * {@inheritDoc}
     */
    @Override protected Skin<?> createDefaultSkin() {
        return new BreadCrumbBarSkin<>(this);
    }

    /**
     * {@inheritDoc}
     
    @Override protected String getUserAgentStylesheet() {
        return BreadCrumbBar.class.getResource("breadcrumbbar.css").toExternalForm();
    }*/
	
	
	
	
	private final ObjectProperty<ObservableList<T>> crumbs = new SimpleObjectProperty<ObservableList<T>>(this, "crumbs");

	
	public final ObjectProperty<ObservableList<T>> crumbsProperty() {
		return crumbs;
	}
	
	/**
	 * Sets a new {@link ObservableList} as the items list underlying GridView.
	 * The old items list will be discarded.
	 */
	public final void setCrumbs(ObservableList<T> value) {
		crumbsProperty().set(value);
	}

	/**
	 * Returns the currently-in-use items list that is being used by the
	 * BreadCrumbBar.
	 */
	public final ObservableList<T> getCrumbs() {
		return crumbs == null ? null : crumbs.get();
	}
	
	
	// --- crumb factory
	private final ObjectProperty<BreadCrumbNodeFactory<T>> crumbFactory = new SimpleObjectProperty<BreadCrumbNodeFactory<T>>(this, "crumbFactory");
    public final ObjectProperty<BreadCrumbNodeFactory<T>> crumbFactoryProperty() {
        return crumbFactory;
    }
    

    /**
     * Sets the cell factory to use to create {@link GridCell} instances to 
     * show in the GridView.
     */
    public final void setCrumbFactory(BreadCrumbNodeFactory<T> value) {
    	if(value == null){
    		value = defaultCrumbNodeFactory;
    	}
    	crumbFactoryProperty().set(value);
    }

    /**
     * Returns the cell factory that will be used to create {@link GridCell} 
     * instances to show in the GridView.
     */
    public final BreadCrumbNodeFactory<T> getCrumbFactory() {
        return crumbFactory.get();
    }
    
    // Style sheet handling
 
    private static final String DEFAULT_STYLE_CLASS = "bread-crumb-bar";
}
