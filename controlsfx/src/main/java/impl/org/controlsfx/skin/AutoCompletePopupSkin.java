package impl.org.controlsfx.skin;

import javafx.scene.Node;
import javafx.scene.control.ListView;
import javafx.scene.control.Skin;

import org.controlsfx.control.autocompletion.AutoCompletePopup;


public class AutoCompletePopupSkin<T> implements Skin<AutoCompletePopup<T>> {

    private final AutoCompletePopup<T> control;
    private final ListView<T> suggestionList;

    public AutoCompletePopupSkin(AutoCompletePopup<T> control){
        this.control = control;
        suggestionList = new ListView<>(control.getSuggestions());
    }


    @Override
    public Node getNode() {
        return suggestionList;
    }

    @Override
    public AutoCompletePopup<T> getSkinnable() {
        return control;
    }

    @Override
    public void dispose() {
    }
}
