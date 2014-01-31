package impl.org.controlsfx.skin;

import javafx.beans.binding.Bindings;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.ListView;
import javafx.scene.control.Skin;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

import org.controlsfx.control.PopOver;
import org.controlsfx.control.autocompletion.AutoCompletePopup;

import com.sun.org.apache.xml.internal.security.utils.HelperNodeList;


public class AutoCompletePopupSkin<T> implements Skin<AutoCompletePopup<T>> {

    private final AutoCompletePopup<T> control;
    private final ListView<T> suggestionList;
    final int LIST_CELL_HEIGHT = 24;

    public AutoCompletePopupSkin(AutoCompletePopup<T> control){
        this.control = control;
        suggestionList = new ListView<>(control.getSuggestions());

        suggestionList.getStyleClass().add(AutoCompletePopup.DEFAULT_STYLE_CLASS);

        suggestionList.getStylesheets().add(AutoCompletePopup.class.getResource("autocompletion.css").toExternalForm());
        suggestionList.prefHeightProperty().bind(
                Bindings.size(suggestionList.getItems()).multiply(LIST_CELL_HEIGHT)
                .add(5) // HACK: avoid that the vertical scrollbar is shown
                );
        suggestionList.maxHeightProperty().bind(control.maxHeightProperty());
        registerEventListener();
    }

    private void registerEventListener(){
        suggestionList.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent me) {
                if(me.getButton().equals(MouseButton.PRIMARY)){
                    if(me.getClickCount() == 2){
                        onSuggestionChoosen(suggestionList.getSelectionModel().getSelectedItem());
                    }
                }
            }
        });


        suggestionList.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent ke) {
                switch (ke.getCode()) {
                case ENTER:
                    onSuggestionChoosen(suggestionList.getSelectionModel().getSelectedItem());
                    break;

                default:
                    break;
                }
            }
        });
    }

    private void onSuggestionChoosen(T suggestion){
        if(suggestion != null)
            Event.fireEvent(control, new AutoCompletePopup.SuggestionChoosenEvent<T>(suggestion));
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
