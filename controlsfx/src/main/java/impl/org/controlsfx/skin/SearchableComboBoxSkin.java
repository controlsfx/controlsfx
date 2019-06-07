/**
 * Copyright (c) 2019, ControlsFX
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
import javafx.collections.transformation.FilteredList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.Skin;
import javafx.scene.control.SkinBase;
import javafx.scene.control.skin.ComboBoxListViewSkin;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.util.StringConverter;
import org.controlsfx.control.SearchableComboBox;
import org.controlsfx.control.textfield.CustomTextField;
import org.controlsfx.control.textfield.TextFields;

import java.util.Arrays;
import java.util.function.Predicate;

import static impl.org.controlsfx.i18n.Localization.getString;

/**
 * A simple skin for a ComboBox, which shows a search field while the
 * popup is showing. The user can type any text into this search field to filter the
 * popup list.
 * <p>
 * The user can type multiple words. The popup list is filtered checking if the string
 * representation of an item contains all filter words (ignoring case).
 * <p>
 * After filtering the list, the user can select an entry by
 * <ul>
 *     <li>
 *         Pressing ENTER: the selected item is applied, the popup closes. If no item
 *         is selected, the first item is applied. To select another item the cursor
 *         keys can be used before pressing ENTER.
 *     </li>
 *     <li>
 *         Pressing TAB: Same as ENTER, but in addition the focus is transferred to the
 *         next control.
 *     </li>
 *     <li>
 *         Selecting an item using the mouse closes the popup.
 *     </li>
 * </ul>
 * <p>
 * When pressing ESCAPE while the popup is showing, the item that was selected when the
 * popup opened will be re-selected (even if the user did select another item using the
 * cursor keys.
 * <p>
 * Other than the {@link ComboBox}, the SearchableComboBox does open the Popup when using
 * the cursor keys (the {@link ComboBox} does only change the selected item without
 * opening the popup). This combined with the behavior of the ESCAPE key does allow to
 * go through the list of items with the cursor keys and than press ESCAPE to revert
 * the changes.
 *
 * <h3>Screenshot</h3>
 * To better describe what a SearchableComboBox is, please refer to the pictures below:
 * <center>
 *     <img src="searchable-combo-box-1.png" alt="Screenshot of SearchableComboBox"/>
 *     <img src="searchable-combo-box-2.png" alt="Screenshot of SearchableComboBox"/>
 *     <img src="searchable-combo-box-3.png" alt="Screenshot of SearchableComboBox"/>
 * </center>
 *
 * <h3>Example</h3>
 *
 * Let's look at an example to clarify this. The combo box offers the items
 * ["Berlin", "Bern", "Munich", "Paris", "New York", "Alberta"]. The user now types "ber" into
 * the search field. The combo box popup will only show ["Berlin", "Bern", "Alberta"].
 * <p>
 * To select the first item ("Berlin"), the user can now either just press ENTER or TAB,
 * or first select this item using the cursor DOWN key and press ENTER or TAB afterwards,
 * or select this item using the mouse.
 * <p>
 * To select the second or third item, the user either
 * must use the cursor keys first, use the mouse, or type more text until the searched item
 * is the first (or only) item in the list.
 * <p>
 * If you want to modify an existing {@link ComboBox} you can set the skin to
 * {@link SearchableComboBoxSkin} (e.g. using {@link ComboBox#setSkin(Skin)} or in CSS.
 *
 * @see SearchableComboBox
 */
public class SearchableComboBoxSkin<T> extends SkinBase<ComboBox<T>> {

    private static final Image filterIcon = new Image(SearchableComboBoxSkin.class.getResource("/impl/org/controlsfx/table/filter.png").toExternalForm());

    /**
     * A "normal" combobox used internally as a delegate to get the default combo box behavior.
     * This combo box contains the filtered items and handles the popup.
     */
    private final ComboBox<T> filteredComboBox;

    /**
     * The search field shown when the popup is shown.
     */
    private final CustomTextField searchField;

    /**
     * Used when pressing ESC
     */
    private T previousValue;

    public SearchableComboBoxSkin(ComboBox<T> comboBox) {
        super(comboBox);

        // first create the filtered combo box
        filteredComboBox = createFilteredComboBox();
        getChildren().add(filteredComboBox);

        // and the search field
        searchField = createSearchField();
        getChildren().add(searchField);

        bindSearchFieldAndFilteredComboBox();
        preventDefaultComboBoxKeyListener();

        // open the popup on Cursor Down and up
        comboBox.addEventHandler(KeyEvent.KEY_PRESSED, this::checkOpenPopup);
    }

    @Override
    protected void layoutChildren(double x, double y, double w, double h) {
        super.layoutChildren(x, y, w, h);
        // ensure filteredComboBox and searchField have the same size as the field
        filteredComboBox.resizeRelocate(x, y, w, h);
        searchField.resizeRelocate(x, y, w, h);
    }

    private CustomTextField createSearchField() {
        CustomTextField field = (CustomTextField) TextFields.createClearableTextField();
        field.setPromptText(getString("filterpanel.search.field"));
        field.setId("search");
        field.getStyleClass().add("combo-box-search");
        ImageView imageView = new ImageView(filterIcon);
        imageView.setFitHeight(15);
        imageView.setPreserveRatio(true);
        field.setLeft(imageView);
        return field;
    }

    private ComboBox<T> createFilteredComboBox() {
        ComboBox<T> box = new ComboBox<>();
        box.setId("filtered");
        box.getStyleClass().add("combo-box-filtered");
        box.setFocusTraversable(false);

        // unidirectional bindings -- copy values from skinnable
        Bindings.bindContent(box.getStyleClass(), getSkinnable().getStyleClass());
        box.buttonCellProperty().bind(getSkinnable().buttonCellProperty());
        box.cellFactoryProperty().bind(getSkinnable().cellFactoryProperty());
        box.converterProperty().bind(getSkinnable().converterProperty());
        box.placeholderProperty().bind(getSkinnable().placeholderProperty());
        box.disableProperty().bind(getSkinnable().disableProperty());
        box.visibleRowCountProperty().bind(getSkinnable().visibleRowCountProperty());
        getSkinnable().showingProperty().addListener((obs, oldVal, newVal) ->
        {
            if (newVal)
                box.show();
            else
                box.hide();
        });

        // bidirectional bindings
        box.valueProperty().bindBidirectional(getSkinnable().valueProperty());

        return box;
    }

    private void bindSearchFieldAndFilteredComboBox() {
        // set the items of the filtered combo box
        filteredComboBox.setItems(createFilteredList());
        // and keep it up to date, even if the original list changes
        getSkinnable().itemsProperty()
                .addListener((obs, oldVal, newVal) -> filteredComboBox.setItems(createFilteredList()));
        // and update the filter, when the text in the search field changes
        searchField.textProperty().addListener(o -> updateFilter());

        // the search field must only be visible, when the popup is showing
        searchField.visibleProperty().bind(filteredComboBox.showingProperty());

        filteredComboBox.showingProperty().addListener((obs, oldVal, newVal) ->
        {
            if (newVal) {
                // When the filtered combo box popup is showing, we must also set the showing property
                // of the original combo box. And here we must remember the previous value for the
                // ESCAPE behavior. And we must transfer the focus to the search field, because
                // otherwise the search field would not allow typing in the search text.
                getSkinnable().show();
                previousValue = getSkinnable().getValue();
                searchField.requestFocus();
            } else {
                // When the filtered combo box popup is hidden, we must also set the showing property
                // of the original combo box to false, clear the search field.
                getSkinnable().hide();
                searchField.setText("");
            }
        });

        // but when the search field is focussed, the popup must still be shown
        searchField.focusedProperty().addListener((obs, oldVal, newVal) ->
        {
            if (newVal)
                filteredComboBox.show();
            else
                filteredComboBox.hide();
        });
    }

    private FilteredList<T> createFilteredList() {
        return new FilteredList<T>(getSkinnable().getItems(), predicate());
    }

    /**
     * Called every time the filter text changes.
     */
    private void updateFilter() {
        // does not work, because of Bug https://bugs.openjdk.java.net/browse/JDK-8174176
        // ((FilteredList<T>)filteredComboBox.getItems()).setPredicate(predicate());

        // therefore we need to do this
        filteredComboBox.setItems(createFilteredList());
    }

    /**
     * Return the Predicate to filter the popup items based on the search field.
     */
    private Predicate<T> predicate() {
        String searchText = searchField.getText().trim();
        if (searchText.isEmpty()) {
            // don't filter
            return null;
        }

        return predicate(searchText);
    }

    /**
     * Return the Predicate to filter the popup items based on the given search text.
     */
    private Predicate<T> predicate(String searchText) {
        // OK, if the display text contains all words, ignoring case
        String[] lowerCaseSearchWords = searchText.toLowerCase().split(" ");
        return value ->
        {
            String lowerCaseDisplayText = getDisplayText(value).toLowerCase();
            return Arrays.stream(lowerCaseSearchWords).allMatch(word -> lowerCaseDisplayText.contains(word));
        };
    }

    /**
     * Create a text for the given item, that can be used to compare with the filter text.
     */
    private String getDisplayText(T value) {
        StringConverter<T> converter = filteredComboBox.getConverter();
        return value == null ? "" : (converter != null ? converter.toString(value) : value.toString());
    }

    /**
     * The default behavior of the ComboBoxListViewSkin is to close the popup on
     * ENTER and SPACE, but we need to override this behavior.
     */
    private void preventDefaultComboBoxKeyListener() {
        filteredComboBox.skinProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal instanceof ComboBoxListViewSkin) {
                ComboBoxListViewSkin cblwSkin = (ComboBoxListViewSkin)newVal;
                if(cblwSkin.getPopupContent() instanceof ListView) {
                    final ListView<T> listView = (ListView<T>) cblwSkin.getPopupContent();
                    if (listView != null) {
                        listView.setOnKeyPressed(this::checkApplyAndCancel);
                    }
                }
            }
        });
    }

    /**
     * Used to alter the behaviour. React on Enter, Tab and ESC.
     */
    private void checkApplyAndCancel(KeyEvent e) {
        KeyCode code = e.getCode();
        if (code == KeyCode.ENTER || code == KeyCode.TAB) {
            // select the first item if no selection
            if (filteredComboBox.getSelectionModel().isEmpty())
                filteredComboBox.getSelectionModel().selectFirst();
            getSkinnable().hide();
            if (code == KeyCode.ENTER) {
                // otherwise the focus would be somewhere else
                getSkinnable().requestFocus();
            }
        } else if (code == KeyCode.ESCAPE) {
            getSkinnable().setValue(previousValue);
            getSkinnable().hide();
            // otherwise the focus would be somewhere else
            getSkinnable().requestFocus();
        }
    }

    /**
     * Show the popup on UP, DOWN, and on beginning typing a word.
     */
    private void checkOpenPopup(KeyEvent e) {
        KeyCode code = e.getCode();
        if (code == KeyCode.UP || code == KeyCode.DOWN) {
            filteredComboBox.show();
            // only open the box navigation
            e.consume();
        } else if (code.isLetterKey() || code.isDigitKey() || code == KeyCode.SPACE) {
            // show the box, let the box handle the KeyEvent
            filteredComboBox.show();
        }
    }

}