package org.controlsfx.control.autocompletion;


import com.sun.javafx.event.EventHandlerManager;

import impl.org.controlsfx.skin.AutoCompletePopupSkin;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventDispatchChain;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.Node;
import javafx.scene.control.PopupControl;
import javafx.scene.control.Skin;
import javafx.stage.Window;

/**
 * The auto-complete-popup provides an list of available suggestions in order
 * to complete current user input.
 * 
 *
 */
public class AutoCompletePopup<T> extends PopupControl{

    /***************************************************************************
     *                                                                         *
     * Private fields                                                          *
     *                                                                         *
     **************************************************************************/

    private final static int TITLE_HEIGHT = 28; // HACK: Hard-coded title-bar height
    private final ObservableList<T> suggestions = FXCollections.observableArrayList();


    /***************************************************************************
     *                                                                         *
     * Inner classes                                                           *
     *                                                                         *
     **************************************************************************/

    /**
     * Represents an Event which is fired when the user has choosen a suggestion
     * for auto-complete
     *
     * @param <TE>
     */
    @SuppressWarnings("serial")
    public static class SuggestionChoosenEvent<TE> extends Event {
        @SuppressWarnings("rawtypes")
        public static final EventType<SuggestionChoosenEvent> SUGGESTION_CHOOSEN = new EventType<SuggestionChoosenEvent>("SUGGESTION_CHOOSEN");

        private final TE selectedSuggestion;

        public SuggestionChoosenEvent(TE selectedSuggestion) {
            super(SUGGESTION_CHOOSEN);
            this.selectedSuggestion = selectedSuggestion;
        }

        /**
         * Returns the suggestion which was chosen by the user
         * @return
         */
        public TE getSelectedSuggestion() {
            return selectedSuggestion;
        }
    }


    /***************************************************************************
     *                                                                         *
     * Constructors                                                            *
     *                                                                         *
     **************************************************************************/

    /**
     * Creates a new AutoCompletePopup
     */
    public AutoCompletePopup(){
        this.setAutoFix(true);
        this.setAutoHide(true);
        this.setHideOnEscape(true);

        getStyleClass().add(DEFAULT_STYLE_CLASS);
    }


    /***************************************************************************
     *                                                                         *
     * Public API                                                              *
     *                                                                         *
     **************************************************************************/


    /**
     * Get the suggestions presented by this AutoCompletePopup
     * @return
     */
    public ObservableList<T> getSuggestions() {
        return suggestions;
    }

    /**
     * Show this popup right below the given Node
     * @param node
     */
    public void show(Node node){

        if(node.getScene() == null || node.getScene().getWindow() == null)
            throw new IllegalStateException("Can not show popup. The node must be attached to a scene/window.");

        Window parent = node.getScene().getWindow();
        this.show(
                parent,
                parent.getX() + node.localToScene(0, 0).getX() +
                node.getScene().getX(),
                parent.getY() + node.localToScene(0, 0).getY() +
                node.getScene().getY() + TITLE_HEIGHT);

    }


    /***************************************************************************
     *                                                                         *
     * Properties                                                              *
     *                                                                         *
     **************************************************************************/


    private final EventHandlerManager eventHandlerManager = new EventHandlerManager(this);

    public final ObjectProperty<EventHandler<SuggestionChoosenEvent<T>>> onSuggestionChoosenProperty() { return onSuggestionChoosen; }
    public final void setOnSuggestionChoosen(EventHandler<SuggestionChoosenEvent<T>> value) { onSuggestionChoosenProperty().set(value); }
    public final EventHandler<SuggestionChoosenEvent<T>> getOnCrumbAction() { return onSuggestionChoosenProperty().get(); }
    private ObjectProperty<EventHandler<SuggestionChoosenEvent<T>>> onSuggestionChoosen = new ObjectPropertyBase<EventHandler<SuggestionChoosenEvent<T>>>() {
        @SuppressWarnings("rawtypes")
        @Override protected void invalidated() {
            eventHandlerManager.setEventHandler(SuggestionChoosenEvent.SUGGESTION_CHOOSEN, (EventHandler<SuggestionChoosenEvent>)(Object)get());
        }

        @Override
        public Object getBean() {
            return AutoCompletePopup.this;
        }

        @Override
        public String getName() {
            return "onSuggestionChoosen";
        }
    };

    /**{@inheritDoc}*/
    @Override public EventDispatchChain buildEventDispatchChain(EventDispatchChain tail) {
        return super.buildEventDispatchChain(tail).append(eventHandlerManager);
    } 


    /***************************************************************************
     *                                                                         *
     * Stylesheet Handling                                                     *
     *                                                                         *
     **************************************************************************/

    public static final String DEFAULT_STYLE_CLASS = "auto-complete-popup";

    @Override
    protected Skin<?> createDefaultSkin() {
        return new AutoCompletePopupSkin<T>(this);
    }

}
