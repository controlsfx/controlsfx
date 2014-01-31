package impl.org.controlsfx.skin;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.ListView;
import javafx.scene.control.Skin;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

import org.controlsfx.control.autocompletion.AutoCompletePopup;

import com.sun.org.apache.xml.internal.security.utils.HelperNodeList;


public class AutoCompletePopupSkin<T> implements Skin<AutoCompletePopup<T>> {

    private final AutoCompletePopup<T> control;
    private final ListView<T> suggestionList;

    public AutoCompletePopupSkin(AutoCompletePopup<T> control){
        this.control = control;
        suggestionList = new ListView<>(control.getSuggestions());

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
