/**
 * Copyright (c) 2014, 2016, 2020 ControlsFX
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *     * Neither the name of ControlsFX, any associated website, nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL CONTROLSFX BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package impl.org.controlsfx.skin;

import javafx.beans.binding.Bindings;
import javafx.event.Event;
import javafx.scene.Node;
import javafx.scene.control.ListView;
import javafx.scene.control.ListCell;
import javafx.scene.control.Skin;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.input.MouseButton;
import javafx.util.StringConverter;
import javafx.util.Callback;
import org.controlsfx.control.textfield.AutoCompletionBinding;


public class AutoCompletePopupSkin<T> implements Skin<AutoCompletePopup<T>> {

    private final AutoCompletePopup<T> control;
    private final ListView<T> suggestionList;
    final int LIST_CELL_HEIGHT = 24;

    public AutoCompletePopupSkin(AutoCompletePopup<T> control) {
        this(control, control.getConverter());
    }

    /**
	 * @param control
	 *            The popup to be skinned
	 * @param displayConverter
	 *            An alternate {@link StringConverter} to use. This way, you can show autocomplete suggestions
	 *            that when applied will fill in a different text than displayed. For example, you may preview
	 *            {@code Files.newBufferedReader(Path: path) - Bufferedreader} but only fill in
	 *            {@code Files.newBufferedReader(}
	 */
    public AutoCompletePopupSkin(AutoCompletePopup<T> control, StringConverter<T> displayConverter) {
        this(control, TextFieldListCell.forListView(displayConverter));
    }

    /**
	 * @param control
	 *            The popup to be skinned
	 * @param cellFactory
	 *            Set a custom cell factory for the suggestions.
	 */
    public AutoCompletePopupSkin(AutoCompletePopup<T> control, Callback<ListView<T>, ListCell<T>> cellFactory) {
        this.control = control;
        suggestionList = new ListView<>(control.getSuggestions());

        suggestionList.getStyleClass().add(AutoCompletePopup.DEFAULT_STYLE_CLASS);

        suggestionList.getStylesheets().add(AutoCompletionBinding.class
        		.getResource("autocompletion.css").toExternalForm()); //$NON-NLS-1$
        /**
         * Here we bind the prefHeightProperty to the minimum height between the
         * max visible rows and the current items list. We also add an arbitrary
         * 5 number because when we have only one item we have the vertical
         * scrollBar showing for no reason.
         */
        suggestionList.prefHeightProperty().bind(
                Bindings.min(control.visibleRowCountProperty(), Bindings.size(suggestionList.getItems()))
                .multiply(LIST_CELL_HEIGHT).add(18));
        suggestionList.setCellFactory(cellFactory);
        
        //Allowing the user to control ListView width.
        suggestionList.prefWidthProperty().bind(control.prefWidthProperty());
        suggestionList.maxWidthProperty().bind(control.maxWidthProperty());
        suggestionList.minWidthProperty().bind(control.minWidthProperty());
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
                case TAB:
                case ENTER:
                    onSuggestionChoosen(suggestionList.getSelectionModel().getSelectedItem());
                    break;
                case ESCAPE:
                    if (control.isHideOnEscape()) {
                        control.hide();
                    }
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
