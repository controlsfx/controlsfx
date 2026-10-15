/**
 * Copyright (c) 2019, 2026, ControlsFX
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
package org.controlsfx.control;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Point2D;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.skin.ComboBoxListViewSkin;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.junit.*;
import org.testfx.api.FxRobot;
import org.testfx.api.FxToolkit;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.Arrays.asList;
import static javafx.scene.input.KeyCode.*;
import static org.junit.Assert.*;

public class SearchableComboBoxTest extends FxRobot{

    private static Application application;

    private static FxRobot robot = new FxRobot();

    private static ComboBox comboBox;
    private static TextField searchField;
    private static ComboBox filteredComboBox;

    private static TextField textField;

    @BeforeClass
    public static void setupSpec() throws TimeoutException {
        FxToolkit.registerPrimaryStage();
        application = FxToolkit.setupApplication(SearchableComboBoxMain.class);
        comboBox = robot.lookup("#comboBox").query();
        textField = robot.lookup("#textField").query();
    }

    @Before
    public void setup() throws InterruptedException {
        setValue(null);
        robot.clickOn(textField);
        if (searchField == null)
            searchField = robot.lookup("#comboBox #search").query();
        if (filteredComboBox == null)
            filteredComboBox = robot.lookup("#comboBox #filtered").query();
    }

    @AfterClass
    public static void cleanupSpec() throws TimeoutException {
        FxToolkit.cleanupApplication(application);
        application = null;
        robot = null;
        comboBox = null;
        textField = null;
    }

    @Test
    public void showsPopupOnClick() {
        // when:
        robot.clickOn(comboBox);

        // then:
        assertTrue(comboBox.isShowing());
    }

    @Test
    public void showsPopupWhenTypingText() {
        // given: "transfer focus using tab"
        robot.type(TAB);

        // when: "type in text"
        robot.type(DIGIT1, DIGIT0, DIGIT0);

        // then:
        assertTrue(comboBox.isShowing());
    }

    @Test
    public void showsPopupOnCursorUpButDoesNotChangeTheSelection() {
        showsPopupOnCursorMovementButDoesNotChangeTheSelection(UP);
    }

    @Test
    public void showsPopupOnCursorDownButDoesNotChangeTheSelection() {
        showsPopupOnCursorMovementButDoesNotChangeTheSelection(DOWN);
    }

    private void showsPopupOnCursorMovementButDoesNotChangeTheSelection(KeyCode direction) {
        // given: "transfer focus using tab"
        robot.type(TAB);

        Object oldValue = comboBox.getValue();

        // when: "use cursor key"
        robot.type(direction);

        // then:
        assertTrue(comboBox.isShowing());

        // and: "does not change the value (first key press only opens the popup)"
        assertEquals(oldValue, comboBox.getValue());

    }

    @Test
    public void doesNotShowPopupOnFocusGainedWithKeyboard() {
        // when: "transfer focus using tab"
        robot.type(TAB);

        // then:
        assertTrue(comboBox.isFocused());

        // and:
        assertFalse(comboBox.isShowing());
    }

    @Test
    public void hidesPopupOnTab() {
        hidesPopupOn(TAB, textField);
    }

    @Test
    public void hidesPopupOnEnter() {
        hidesPopupOn(ENTER, comboBox);
    }

    @Test
    public void hidesPopupOnEscape() {
        hidesPopupOn(ESCAPE, comboBox);
    }

    private void hidesPopupOn(KeyCode key, Node newFocusOwner) {
        // given: "open popup"
        robot.clickOn(comboBox);

        // when: "typing #key"
        robot.type(key);

        // then: "popup is not showing any more"
        assertFalse(comboBox.isShowing());

        // and: "#newFocusOwner has the focus"
        assertTrue(newFocusOwner.isFocused());
    }

    @Test
    public void hidePopupWhenClickingOnTheSearchField() {
        // given: "open popup"
        robot.clickOn(comboBox);

        // when: "clicking on the search field"
        robot.clickOn(searchField);

        // then: "popup is still showing"
        assertFalse(comboBox.isShowing());
    }

    @Test
    public void hidesPopupOnClickOutside() {
        // given: "open popup"
        robot.clickOn(comboBox);

        // when: "click somewhere outside"
        robot.clickOn(comboBox.getScene());

        // then: "popup is closed"
        assertFalse(comboBox.isFocused());
    }

    @Test
    public void hidesPopupOnFocusLost() {
        // given: "open popup"
        robot.clickOn(comboBox);

        // when:
        robot.type(TAB);

        // then:
        assertFalse(comboBox.isShowing());
    }

    @Test
    public void filtersTheListBasedOnTheInputText() {
        // given: "comboBox is focused"
        robot.type(TAB);

        // when: "input search text '100'"
        robot.type(NUMPAD1, DIGIT0, DIGIT0);

        // then: "list is filtered"
        assertEquals(asList("100"), filteredComboBox.getItems());
    }

    @Test
    public void filtersTheListBasedOnMultipleWords() {
        // given: "comboBox is focused"
        robot.type(TAB);

        // when: "input search text '1 00'"
        robot.type(DIGIT1, SPACE, DIGIT0, DIGIT0);

        // then: "list is filtered"
        assertEquals(asList("100"), filteredComboBox.getItems());
    }

    @Test
    public void filterIsCaseInsensitive() {
        // given: "comboBox is focused"
        robot.type(TAB);

        // when: "input search text 'a'"
        robot.type(A);

        // then: "Upper case entry is filtered"
        assertEquals(asList("AAA"), filteredComboBox.getItems());
    }

    @Test
    public void ifTheCurrentValueIsContainedInTheFilteredListItIsSelected() throws InterruptedException {
        // given: "comboBox is focused value '100' is set"
        setValue("100");
        robot.type(TAB);

        // when: "input search text '100'"
        robot.type(DIGIT1, DIGIT0, DIGIT0);

        // then: "value is still selected"
        assertEquals(0, filteredComboBox.getSelectionModel().getSelectedIndex());
    }

    @Test
    public void ifTheCurrentValueIsNotContainedInTheFilteredListNoEntryIsSelected() {
        // given: "comboBox is focused, no value is set"
        robot.type(TAB);

        // when: "input search text '100'"
        robot.type(DIGIT1, DIGIT0, DIGIT0);

        // then: "no value is selected"
        assertEquals(-1, filteredComboBox.getSelectionModel().getSelectedIndex());
    }

    // TODO: Find why is this test failing and remove @Ignore
    @Test @Ignore
    public void cursorDownSelectsFirstItemIfNoItemSelected() {
        // given: "open popup"
        robot.clickOn(comboBox);

        // when:
        robot.type(DOWN);

        // then:
        assertEquals("1", comboBox.getValue());
    }

    @Test
    public void selectingValueUsingTheMouseClosesThePopup() {
        // given: "open popup"
        robot.clickOn(comboBox);
        ListView listView = robot.lookup("#list-view").query();
        Point2D coordinate = listView.localToScreen(5, 5);

        // when: "clicking on the first entry"
        robot.clickOn(coordinate);

        // then: "the popup is closed"
        assertFalse(comboBox.isShowing());

        // and: "the value '1' is selected"
        assertEquals("1", comboBox.getValue());
    }

    // TODO: Find why is this test failing and remove @Ignore
    @Test @Ignore
    public void keepsValueOnTab() {
        keepsValueOn(TAB);
    }

    // TODO: Find why is this test failing and remove @Ignore
    @Test @Ignore
    public void keepsValueOnEnter() {
        keepsValueOn(ENTER);
    }

    private void keepsValueOn(KeyCode key) {
        // given: "open popup"
        robot.clickOn(comboBox);

        // when: "selecting the first entry"
        robot.type(DOWN);

        // and: "pressing the key"
        robot.type(key);

        // then: "the value is still selected"
        assertEquals("1", comboBox.getValue());
    }

    @Test
    public void selectsFirstItemIfNoSelectionOnTab() {
        selectsFirstItemIfNoSelectionOn(TAB);
    }

    @Test
    public void selectsFirstItemIfNoSelectionOnEnter() {
        selectsFirstItemIfNoSelectionOn(ENTER);
    }

    private void selectsFirstItemIfNoSelectionOn(KeyCode key) {
        // given: "open popup"
        robot.clickOn(comboBox);

        // when: "pressing the key"
        robot.type(key);

        // then: "the first value is selected"
        assertEquals("1", comboBox.getValue());
    }

    @Test
    public void escapeRestoresThePreviousValue() {
        // given: "open popup"
        robot.clickOn(comboBox);

        // when: "selecting the first entry"
        robot.type(DOWN);

        // and: "pressing ESC"
        robot.type(ESCAPE);

        // then: "the value is not selected any more"
        assertNull(comboBox.getValue());
    }

    @Test
    public void canProgrammaticallyShowAndHideThePopup() {
        // when: "programmatically open the popup"
        invokeAndWait(comboBox::show);

        // then:
        assertTrue(comboBox.isShowing());

        // when:
        invokeAndWait(comboBox::hide);

        // then:
        assertFalse(comboBox.isShowing());
    }

    @Test
    public void filterIsResetOnPopupClose() {
        // given: "comboBox is focused, no value is set"
        robot.type(TAB);

        //when: "input search text '100'"
        robot.type(DIGIT1, DIGIT0, DIGIT0);

        // and: "popup is closed again"
        robot.type(ESCAPE);

        // then: "filter is reset"
        assertEquals(filteredComboBox.getItems(), comboBox.getItems());
        assertEquals("", searchField.getText());
    }

    @Test
    public void spaceAllowsContinuingAMultiwordSearch() {
        withSteelItems(actions -> checkSpaceWhileSearching(null, actions));
    }

    @Test
    public void spaceDoesNotAcceptTheExistingSelection() {
        withSteelItems(actions -> checkSpaceWhileSearching("Steel Rod", actions));
    }

    private void checkSpaceWhileSearching(String initialValue, AtomicInteger actions) {
        invokeAndWait(() -> comboBox.setValue(initialValue));
        robot.clickOn(comboBox);
        robot.write("steel");
        int actionsBeforeSpace = actions.get();
        Object valueBeforeSpace = comboBox.getValue();

        robot.type(SPACE);
        invokeAndWait(() -> {
            assertEquals("steel ", searchField.getText());
            assertTrue(comboBox.isShowing());
            assertEquals(valueBeforeSpace, comboBox.getValue());
            assertEquals(actionsBeforeSpace, actions.get());
        });

        robot.write("rod");
        invokeAndWait(() -> {
            assertEquals(asList("Steel Rod"), filteredComboBox.getItems());
            assertEquals("steel rod", searchField.getText());
            assertTrue(comboBox.isShowing());
        });
    }

    @Test
    public void spaceOnPopupDoesNotSelectTheFocusedItem() {
        withSteelItems(actions -> {
            robot.clickOn(comboBox);
            invokeAndWait(() -> checkSpaceOnPopup(actions));
        });
    }

    @Test
    public void spaceStillWorksAfterReplacingTheDelegateSkin() {
        withSteelItems(actions -> {
            robot.clickOn(comboBox);
            invokeAndWait(() -> {
                filteredComboBox.hide();
                filteredComboBox.setSkin(new ComboBoxListViewSkin<>(filteredComboBox));
                comboBox.show();
                checkSpaceOnPopup(actions);
            });
        });
    }

    private void checkSpaceOnPopup(AtomicInteger actions) {
        Node popup = ((ComboBoxListViewSkin<?>) filteredComboBox.getSkin()).getPopupContent();
        ((ListView<?>) popup).getFocusModel().focus(0);
        int actionsBeforeSpace = actions.get();
        Object valueBeforeSpace = comboBox.getValue();
        popup.fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED,
                "", " ", SPACE, false, false, false, false));
        popup.fireEvent(new KeyEvent(KeyEvent.KEY_RELEASED,
                "", " ", SPACE, false, false, false, false));
        assertTrue(comboBox.isShowing());
        assertEquals(valueBeforeSpace, comboBox.getValue());
        assertEquals(actionsBeforeSpace, actions.get());
    }

    private void withSteelItems(Consumer<AtomicInteger> test) {
        ObservableList originalItems = comboBox.getItems();
        AtomicInteger actions = new AtomicInteger();
        EventHandler<ActionEvent> actionHandler = event -> actions.incrementAndGet();
        invokeAndWait(() -> {
            comboBox.setItems(FXCollections.observableArrayList("Steel Rod", "Steel Beam", "Iron Rod"));
            comboBox.addEventHandler(ActionEvent.ACTION, actionHandler);
        });
        try {
            test.accept(actions);
        } finally {
            invokeAndWait(() -> {
                comboBox.removeEventHandler(ActionEvent.ACTION, actionHandler);
                comboBox.hide();
                comboBox.setItems(originalItems);
                comboBox.setValue(null);
            });
        }
    }

    private void setValue(Object value) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            comboBox.setValue(value);
            latch.countDown();
        });
        latch.await();
    }

    private void invokeAndWait(Runnable runnable) {
        FutureTask task = new FutureTask<Void>(runnable, null);
        Platform.runLater(task);
        try {
            task.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public static class SearchableComboBoxMain extends Application {

        public static void main(String[] args) {
            Application.launch(SearchableComboBoxMain.class, args);
        }

        @Override
        public void start(Stage stage) throws Exception {
            TextField textField = new TextField();
            textField.setId("textField");

            ComboBox<String> comboBox = new SearchableComboBox<>();
            comboBox.setId("comboBox");
            comboBox.getItems().addAll(IntStream.rangeClosed(1, 100).mapToObj(String::valueOf).collect(Collectors.toList()));
            comboBox.getItems().addAll(IntStream.rangeClosed('A', 'Z').mapToObj(c -> "" + (char) c + (char) c + (char) c).collect(Collectors.toList()));

            VBox pane = new VBox();
            pane.getChildren().add(textField);
            pane.getChildren().add(comboBox);
            stage.setScene(new Scene(pane));
            stage.sizeToScene();
            stage.show();
        }

    }
}
