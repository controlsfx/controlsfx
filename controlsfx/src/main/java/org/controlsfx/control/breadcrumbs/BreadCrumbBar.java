package org.controlsfx.control.breadcrumbs;

import impl.org.controlsfx.skin.BreadCrumbBarSkin;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;

/**
 * Represents a bread crumb bar
 * 
 */
public class BreadCrumbBar<T extends IBreadCrumbModel> extends Control {

	private final ObjectProperty<ObservableList<T>> crumbs = new SimpleObjectProperty<ObservableList<T>>(this, "crumbs");
	private final ObjectProperty<BreadCrumbNodeFactory<T>> crumbFactory = new SimpleObjectProperty<BreadCrumbNodeFactory<T>>(this, "crumbFactory");


	@SuppressWarnings("serial")
	public static class BreadCrumbActionEvent extends Event {
		public static final EventType<BreadCrumbActionEvent> CRUMB_ACTION = new EventType<BreadCrumbActionEvent>("CRUMB_ACTION");

		private final IBreadCrumbModel crumbModel;

		public BreadCrumbActionEvent(IBreadCrumbModel crumbModel) {
			super(CRUMB_ACTION);
			this.crumbModel = crumbModel;
		}

		public IBreadCrumbModel getCrumbModel() {
			return crumbModel;
		}
	}


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
	 * Fire the bread crumb action event
	 * @param crumb Crumb model which was activated
	 */
	public final void fireBreadCrumbAction(T crumb){
		fireEvent(new BreadCrumbActionEvent(crumb));
	}

	/**
	 * Register an action event handler which is invoked when a bread crumb is activated
	 * @param handler
	 */
	public void setOnBreadCrumbAction(EventHandler<BreadCrumbActionEvent> handler){
		addEventHandler(BreadCrumbActionEvent.CRUMB_ACTION, handler);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override protected Skin<?> createDefaultSkin() {
		return new BreadCrumbBarSkin<>(this);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override protected String getUserAgentStylesheet() {
		return BreadCrumbBar.class.getResource("breadcrumbbar.css").toExternalForm();
	}



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
	public final ObjectProperty<BreadCrumbNodeFactory<T>> crumbFactoryProperty() {
		return crumbFactory;
	}


	/**
	 * Sets the crumb factory to create (custom) {@link BreadCrumbButton} instances.
	 * <code>null</code> is not allowed and will result in a fall back to the default factory.
	 */
	public final void setCrumbFactory(BreadCrumbNodeFactory<T> value) {
		if(value == null){
			value = defaultCrumbNodeFactory;
		}
		crumbFactoryProperty().set(value);
	}

	/**
	 * Returns the cell factory that will be used to create {@link BreadCrumbButton} 
	 * instances
	 */
	public final BreadCrumbNodeFactory<T> getCrumbFactory() {
		return crumbFactory.get();
	}

	// Style sheet handling

	private static final String DEFAULT_STYLE_CLASS = "bread-crumb-bar";
}
