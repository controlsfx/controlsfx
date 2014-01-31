package org.controlsfx.control.autocompletion;

import impl.org.controlsfx.skin.AutoCompletePopupSkin;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Scene;
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

    private final ObservableList<T> suggestions = FXCollections.observableArrayList();


    public AutoCompletePopup(){

    }

    /**
     * Get the suggestions presented by this AutoCompletePopup
     * @return
     */
    public ObservableList<T> getSuggestions() {
        return suggestions;
    }
    private final static int TITLE_HEIGHT = 28;

    public void showBelowNode(Node node){

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


    //
    // Style sheet handling
    //

    private static final String DEFAULT_STYLE_CLASS = "auto-complete-popup";

    @Override
    protected Skin<?> createDefaultSkin() {
        return new AutoCompletePopupSkin<T>(this);
    }

}
