package impl.org.controlsfx.skin;

import javafx.beans.binding.Bindings;
import javafx.event.Event;
import javafx.scene.Node;
import javafx.scene.control.ListView;
import javafx.scene.control.Skin;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.input.MouseButton;
import org.controlsfx.control.textfield.AutoCompletionBinding;


public class AutoCompletePopupSkin<T> implements Skin<AutoCompletePopup<T>> {

    private final AutoCompletePopup<T> control;
    private final ListView<T> suggestionList;
    final int LIST_CELL_HEIGHT = 24;

    public AutoCompletePopupSkin(AutoCompletePopup<T> control){
        this.control = control;
        suggestionList = new ListView<>(control.getSuggestions());

        suggestionList.getStyleClass().add(AutoCompletePopup.DEFAULT_STYLE_CLASS);

        suggestionList.getStylesheets().add(AutoCompletionBinding.class
        		.getResource("autocompletion.css").toExternalForm()); //$NON-NLS-1$
        suggestionList.prefHeightProperty().bind(
                Bindings.size(suggestionList.getItems()).multiply(LIST_CELL_HEIGHT)
                .add(15));
        suggestionList.maxHeightProperty().bind(control.maxHeightProperty());
        suggestionList.setCellFactory(TextFieldListCell.forListView(control.getConverter()));               
        registerEventListener();
    }

    private void registerEventListener(){
        suggestionList.setOnMouseClicked(me -> {
            if (me.getButton() == MouseButton.PRIMARY){
                onSuggestionChoosen(suggestionList.getSelectionModel().getSelectedItem());
            }
        });


        suggestionList.setOnKeyPressed(ke -> {
            switch (ke.getCode()) {
            case ENTER:
                onSuggestionChoosen(suggestionList.getSelectionModel().getSelectedItem());
                break;

            default:
                break;
            }
        });
    }

    private void onSuggestionChoosen(T suggestion){
        if(suggestion != null) {
            Event.fireEvent(control, new AutoCompletePopup.SuggestionEvent<>(suggestion));
        }
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
