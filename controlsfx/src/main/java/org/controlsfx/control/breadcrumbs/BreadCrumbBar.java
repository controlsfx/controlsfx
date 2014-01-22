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
public class BreadCrumbBar<T> extends Control {

    private final ObservableList<T> crumbs = FXCollections.<T> observableArrayList();
    private final ObjectProperty<BreadCrumbNodeFactory<T>> crumbFactory = new SimpleObjectProperty<BreadCrumbNodeFactory<T>>(this, "crumbFactory");



    @SuppressWarnings("serial")
    public static class BreadCrumbActionEvent<T> extends Event {
        @SuppressWarnings("rawtypes")
        public static final EventType<BreadCrumbActionEvent> CRUMB_ACTION = new EventType<BreadCrumbActionEvent>("CRUMB_ACTION");

        private final T crumbModel;

        public BreadCrumbActionEvent(T crumbModel) {
            super(CRUMB_ACTION);
            this.crumbModel = crumbModel;
        }

        public T getCrumbModel() {
            return crumbModel;
        }
    }


    private final BreadCrumbNodeFactory<T> defaultCrumbNodeFactory = new BreadCrumbNodeFactory<T>(){
        @Override
        public BreadCrumbButton createBreadCrumbButton(T crumb, int index) {
            return new BreadCrumbButton(crumb.toString(), index == 0);
        }
    };

    public BreadCrumbBar(){
        this(FXCollections.<T> observableArrayList());
    }

    public BreadCrumbBar(ObservableList<T> items) {
        getStyleClass().add(DEFAULT_STYLE_CLASS);
        getCrumbs().addAll(items);
        setCrumbFactory(defaultCrumbNodeFactory);
    }

    /**
     * Register an action event handler which is invoked when a bread crumb is activated
     * @param handler
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void setOnBreadCrumbAction(EventHandler<BreadCrumbActionEvent<T>> handler){
        addEventHandler(BreadCrumbActionEvent.CRUMB_ACTION, (EventHandler<BreadCrumbActionEvent>)(Object)handler);
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

    /**
     * Returns the currently-in-use items list that is being used by the
     * BreadCrumbBar.
     */
    public final ObservableList<T> getCrumbs() {
        return crumbs;
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
