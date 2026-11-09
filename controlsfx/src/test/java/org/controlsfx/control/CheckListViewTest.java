/**
 * Copyright (c) 2026, ControlsFX
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

import javafx.beans.property.BooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListCell;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.testfx.api.FxRobot;
import org.testfx.api.FxToolkit;
import org.testfx.util.WaitForAsyncUtils;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

/**
 * Tests that checking items through the check model of a {@link CheckListView} stays consistent
 * with the {@link BooleanProperty} of each item.
 *
 * <p>Both states can drift apart: the check model keeps its own bit set, while the
 * {@code CheckBoxListCell} of every row is bound to the {@link BooleanProperty} returned by
 * {@link CheckListView#getItemBooleanProperty(Object)}. Whenever a check reaches the bit set
 * without updating the matching property, {@code getCheckModel().isChecked(index)} answers
 * {@code true} while the row is still rendered unchecked.
 *
 * <p>The control is therefore shown in a scene of its own, and every assertion goes all the way
 * down to the CheckBox each row renders.
 *
 * <p>Each test below checks items in a specific order, as the defect only shows up for some
 * sequences. The same scenarios are covered against the check model alone - without going through
 * the control - by {@link CheckBitSetModelBaseTest}.
 */
public class CheckListViewTest extends FxRobot {

    private static final String ITEM_1 = "Item 1";
    private static final String ITEM_2 = "Item 2";
    private static final String ITEM_3 = "Item 3";
    private static final String ITEM_4 = "Item 4";
    private static final String ITEM_5 = "Item 5";

    private ObservableList<String> items;
    private CheckListView<String> checkListView;

    private static final int SCENE_WIDTH = 300;
    private static final int SCENE_HEIGHT = 400;
    private static final long LOOKUP_TIMEOUT_MILLIS = 5000;

    @BeforeClass
    public static void setupSpec() throws TimeoutException {
        FxToolkit.registerPrimaryStage();
    }

    @After
    public void afterEach() throws TimeoutException {
        FxToolkit.cleanupStages();
    }

    @Test
    public void testCheckFirstItemAfterFollowingItems() {
        // derived from https://github.com/controlsfx/controlsfx/issues/1549
        // Checking the first item last does not expose the defect the tests below reproduce, so
        // this scenario guards against a regression of the original issue.
        givenCheckListView(ITEM_1, ITEM_2, ITEM_3, ITEM_4);

        interact(() -> {
            IndexedCheckModel<String> checkModel = checkListView.getCheckModel();
            checkModel.check(ITEM_2);
            checkModel.check(ITEM_3);
            checkModel.check(ITEM_1);
        });

        assertCheckedIndices(0, 1, 2);
    }

    @Test
    public void testCheckItemInsideAlreadyCheckedRange() {
        // derived from https://github.com/controlsfx/controlsfx/issues/1549#issuecomment-5542131988
        // "Item 2" closes the gap left in the middle of the checked range
        givenCheckListView(ITEM_1, ITEM_2, ITEM_3, ITEM_4);

        interact(() -> {
            IndexedCheckModel<String> checkModel = checkListView.getCheckModel();
            checkModel.check(ITEM_1);
            checkModel.check(ITEM_3);
            checkModel.check(ITEM_4);
            checkModel.check(ITEM_2);
        });

        assertCheckedIndices(0, 1, 2, 3);
    }

    @Test
    public void testCheckItemAfterCheckingTheSameItemTwice() {
        // variant of https://github.com/controlsfx/controlsfx/issues/1549 where the same item is
        // checked twice - the second check being a no-op on the check model
        givenCheckListView(ITEM_1, ITEM_2, ITEM_3, ITEM_4, ITEM_5);

        interact(() -> {
            IndexedCheckModel<String> checkModel = checkListView.getCheckModel();
            checkModel.check(ITEM_1);
            checkModel.check(ITEM_2);
            checkModel.check(ITEM_2);
            checkModel.check(ITEM_4);
        });

        assertCheckedIndices(0, 1, 3);
    }

    @Test
    public void testCheckIndicesForItemInsideAlreadyCheckedRange() {
        // variant of https://github.com/controlsfx/controlsfx/issues/1549 going through
        // checkIndices(int...) instead of check(T)
        givenCheckListView(ITEM_1, ITEM_2, ITEM_3, ITEM_4, ITEM_5);

        interact(() -> {
            IndexedCheckModel<String> checkModel = checkListView.getCheckModel();
            checkModel.check(ITEM_1);
            checkModel.check(ITEM_4);
            checkModel.check(ITEM_5);
            checkModel.checkIndices(1);
        });

        assertCheckedIndices(0, 1, 3, 4);
    }

    @Test
    public void testCheckItemAfterClearCheckOnUncheckedItem() {
        // variant of https://github.com/controlsfx/controlsfx/issues/1549 where the check of the
        // unchecked "Item 2" is cleared - a no-op on the model - before "Item 3" is checked
        givenCheckListView(ITEM_1, ITEM_2, ITEM_3, ITEM_4, ITEM_5);

        interact(() -> {
            IndexedCheckModel<String> checkModel = checkListView.getCheckModel();
            checkModel.check(ITEM_1);
            checkModel.check(ITEM_4);
            checkModel.check(ITEM_5);
            checkModel.clearCheck(ITEM_2);
            checkModel.check(ITEM_3);
        });

        assertCheckedIndices(0, 2, 3, 4);
    }

    @Test
    public void testCheckIndicesNotInAscendingOrder() {
        // derived from https://github.com/controlsfx/controlsfx/issues/1549
        // checkIndices(int...) is given the indices of "Item 5" and "Item 1" in descending order
        givenCheckListView(ITEM_1, ITEM_2, ITEM_3, ITEM_4, ITEM_5);

        interact(() -> checkListView.getCheckModel().checkIndices(4, 0));

        assertCheckedIndices(0, 4);
    }

    @Test
    public void testCheckIndicesWithDuplicateIndex() {
        // variant of https://github.com/controlsfx/controlsfx/issues/1549 where checkIndices(int...)
        // is given the same index twice
        givenCheckListView(ITEM_1, ITEM_2, ITEM_3, ITEM_4, ITEM_5);

        interact(() -> checkListView.getCheckModel().checkIndices(4, 4));

        assertCheckedIndices(4);
    }

    @Test
    public void testCheckIndicesAroundAnAlreadyCheckedItem() {
        // variant of https://github.com/controlsfx/controlsfx/issues/1549 where the items handed to
        // checkIndices(int...) surround the already checked "Item 3"
        givenCheckListView(ITEM_1, ITEM_2, ITEM_3, ITEM_4, ITEM_5);

        interact(() -> {
            IndexedCheckModel<String> checkModel = checkListView.getCheckModel();
            checkModel.check(ITEM_3);
            checkModel.checkIndices(0, 4);
        });

        assertCheckedIndices(0, 2, 4);
    }

    @Test
    public void testCheckedItemStaysCheckedAfterAnItemIsAdded() {
        // adding an item makes the check model follow the change of the item list
        givenCheckListView(ITEM_1, ITEM_2, ITEM_3, ITEM_4);

        interact(() -> {
            checkListView.getCheckModel().check(ITEM_2);
            items.add(ITEM_5);
        });

        assertCheckedIndices(1);
    }

    @Test
    public void testCheckingAnItemAfterAnItemIsAddedChecksItsCheckBox() {
        // the CheckBox of an already rendered row stays bound to the property it was given, so
        // the item has to keep that very property across the change of the item list
        givenCheckListView(ITEM_1, ITEM_2, ITEM_3, ITEM_4);

        interact(() -> items.add(ITEM_5));
        interact(() -> checkListView.getCheckModel().check(ITEM_2));

        assertCheckedIndices(1);
    }

    @Test
    public void testReplacingTheItemsAndSettingThemBackKeepsTheChecksMadeAfterwards() {
        // a control whose list of items is replaced builds a new check model, and the one it
        // drops has to let go of the list it was built on: both otherwise follow the next change
        // of that list, the stale one writing the item boolean properties - and through them the
        // check model the control now holds - back to the checks it still holds itself
        givenCheckListView(ITEM_1, ITEM_2, ITEM_3);
        ObservableList<String> firstItems = items;
        ObservableList<String> otherItems = FXCollections.observableArrayList(ITEM_4, ITEM_5);

        interact(() -> checkListView.getCheckModel().check(ITEM_1));
        interact(() -> checkListView.setItems(otherItems));
        interact(() -> checkListView.setItems(firstItems));
        interact(() -> checkListView.getCheckModel().check(ITEM_2));
        interact(() -> firstItems.add(ITEM_4));

        assertCheckedIndices(1);
    }

    @Test
    public void testAStaleCheckModelNoLongerDrivesTheOneTheControlNowHolds() {
        // getCheckModel() hands out the model the control holds, and an application is free to
        // keep that reference: a control whose list of items is replaced builds a new model around
        // the very same item boolean properties, and the one it drops has to let go of them. A
        // check made through the stale model is rendered by no row, and must therefore not reach
        // the model the control now holds.
        givenCheckListView(ITEM_1, ITEM_2, ITEM_3);
        ObservableList<String> firstItems = items;
        ObservableList<String> otherItems = FXCollections.observableArrayList(ITEM_4, ITEM_5);
        IndexedCheckModel<String> staleModel = checkListView.getCheckModel();

        interact(() -> checkListView.setItems(otherItems));
        interact(() -> checkListView.setItems(firstItems));
        interact(() -> staleModel.check(ITEM_2));

        assertCheckedIndices();
    }

    @Test
    public void testTheCheckModelTheControlIsGivenBackGoesOnDrivingTheCheckBoxes() {
        // setCheckModel(IndexedCheckModel) is public API, and an application is free to take the
        // model of the control away and to hand it back - the control holds none in between, which
        // its own key handler already caters for: the model it is given back is the model it has,
        // and the checks made through it are rendered by the rows holding them
        givenCheckListView(ITEM_1, ITEM_2, ITEM_3);
        IndexedCheckModel<String> builtInModel = checkListView.getCheckModel();

        interact(() -> checkListView.setCheckModel(null));
        interact(() -> checkListView.setCheckModel(builtInModel));
        interact(() -> checkListView.getCheckModel().check(ITEM_2));

        assertCheckedIndices(1);
    }

    @Test
    public void testTheChecksFollowTheirItemsWhenTheItemListChangesAgainFromTheirReport() {
        // whoever is told about the reindexed checks is free to change the item list again from
        // there, and the checks - with the CheckBox rendering each of them - have to follow that
        // change as they follow any other: a list which is changed again while it is still
        // reporting hands the check model the range it already followed along with the new one
        givenCheckListView(ITEM_1, ITEM_2, ITEM_3, ITEM_4, ITEM_5);
        AtomicInteger reportedChanges = new AtomicInteger();

        interact(() -> {
            IndexedCheckModel<String> checkModel = checkListView.getCheckModel();
            checkModel.check(ITEM_2);
            checkModel.check(ITEM_4);
            checkModel.getCheckedItems().addListener((ListChangeListener<String>) change -> {
                if (reportedChanges.getAndIncrement() == 0) {
                    items.remove(ITEM_5);
                }
            });
            items.remove(ITEM_1);
        });

        // neither of the removed items was ever checked, so the checks of "Item 2" and "Item 4"
        // are still theirs - at the positions the two removals leave them at
        assertCheckedIndices(0, 2);
    }

    @Test
    public void testClearingEveryCheckFromTheReportOfACheckUnchecksEveryCheckBox() {
        // a listener of the checked indices is free to clear the checks from there, which leaves
        // the change still being reported standing for a check the model no longer holds: each
        // row renders the check its model ends up with, not that one
        givenCheckListView(ITEM_1, ITEM_2, ITEM_3, ITEM_4, ITEM_5);
        AtomicInteger reportedChanges = new AtomicInteger();

        interact(() -> {
            IndexedCheckModel<String> checkModel = checkListView.getCheckModel();
            checkModel.check(ITEM_3);
            checkModel.getCheckedIndices().addListener((ListChangeListener<Integer>) change -> {
                if (reportedChanges.getAndIncrement() == 0) {
                    checkModel.clearChecks();
                }
            });
            checkModel.check(ITEM_5);
        });

        assertCheckedIndices();
    }

    @Test
    public void testTheBooleanPropertyOfACheckedItemIsClearedWhenTheItemListIsReplaced() {
        // getItemBooleanProperty(T) is public API, handed out to be bound to, and it stands for
        // the check the control holds on that item: an item which is not in the new list holds no
        // check any more, so whoever is bound to its property must not go on rendering the one it
        // had. The same scenario is covered against the check model alone by
        // CheckBitSetModelBaseTest.
        givenCheckListView(ITEM_1, ITEM_2);
        BooleanProperty itemProperty = checkListView.getItemBooleanProperty(ITEM_1);

        interact(() -> checkListView.getCheckModel().check(ITEM_1));
        interact(() -> checkListView.setItems(FXCollections.observableArrayList(ITEM_3, ITEM_4)));

        assertFalse("The property handed out for an item of the former list still reports its check",
            itemProperty.get());
    }

    @Test
    public void testReplacingTheItemsReportsNoCheckOnTheCheckModelTheControlDrops() {
        // a control whose list of items is replaced builds a new check model around the very same
        // item boolean properties: the model it drops has to let go of those properties before the
        // new one writes them, or the two drive each other through them - and an application which
        // is still holding the dropped model is told of checks nothing renders any more
        givenCheckListView(ITEM_1, ITEM_2);
        IndexedCheckModel<String> droppedModel = checkListView.getCheckModel();
        interact(() -> droppedModel.check(ITEM_1));
        AtomicInteger reportedChanges = new AtomicInteger();
        droppedModel.getCheckedItems().addListener(
            (ListChangeListener<String>) change -> reportedChanges.incrementAndGet());

        interact(() -> checkListView.setItems(FXCollections.observableArrayList(ITEM_1, ITEM_3)));

        assertEquals("The check model the control dropped still reports changes of its checks",
            0, reportedChanges.get());
    }

    /**
     * Shows a {@link CheckListView} holding the given items. The control is put in a scene of its
     * own, and that scene shown, so that every row is really rendered: a CheckBox is only bound to
     * the property of its item once the cell holding it exists.
     */
    private void givenCheckListView(String... itemValues) {
        items = FXCollections.observableArrayList(itemValues);
        try {
            FxToolkit.setupStage(stage -> {
                checkListView = new CheckListView<>(items);
                stage.setScene(new Scene(checkListView, SCENE_WIDTH, SCENE_HEIGHT));
                stage.show();
                stage.toFront();
            });
        } catch (TimeoutException e) {
            throw new AssertionError("The CheckListView could not be shown", e);
        }
        WaitForAsyncUtils.waitForFxEvents();
    }

    /**
     * Returns the CheckBox rendered by the row of the given index, or {@code null} when that row
     * holds no cell - which the caller is expected to report rather than to skip over.
     */
    private CheckBox renderedCheckBoxOf(int index) {
        return WaitForAsyncUtils.waitForAsyncFx(LOOKUP_TIMEOUT_MILLIS, () -> {
            for (Node node : checkListView.lookupAll(".list-cell")) {
                ListCell<?> cell = (ListCell<?>) node;
                if (!cell.isEmpty() && cell.getIndex() == index) {
                    return (CheckBox) cell.getGraphic();
                }
            }
            return null;
        });
    }

    /**
     * Asserts that exactly the given indices are checked - in the check model, in the
     * {@link BooleanProperty} of every item, and in the CheckBox each row renders, which is what
     * the user actually sees.
     */
    private void assertCheckedIndices(int... expectedCheckedIndices) {
        // let the rows the checks reached be laid out again before reading what they render
        WaitForAsyncUtils.waitForFxEvents();

        BitSet expected = new BitSet();
        for (int index : expectedCheckedIndices) {
            expected.set(index);
        }

        IndexedCheckModel<String> checkModel = checkListView.getCheckModel();
        for (int index = 0; index < items.size(); index++) {
            String item = items.get(index);
            String expectedState = expected.get(index) ? "checked" : "not checked";

            assertEquals(
                MessageFormat.format("Expected the check model to report index {0} ({1}) as {2}",
                    index, item, expectedState),
                expected.get(index), checkModel.isChecked(index));

            BooleanProperty itemProperty = checkListView.getItemBooleanProperty(item);
            assertNotNull(
                MessageFormat.format("Expected a BooleanProperty for index {0} ({1})", index, item),
                itemProperty);
            assertEquals(
                MessageFormat.format("Expected the BooleanProperty of index {0} ({1}) to be {2}",
                    index, item, expectedState),
                expected.get(index), itemProperty.get());

            CheckBox renderedCheckBox = renderedCheckBoxOf(index);
            assertNotNull(
                MessageFormat.format("Expected index {0} ({1}) to be rendered by a row of its own",
                    index, item),
                renderedCheckBox);
            assertEquals(
                MessageFormat.format("Expected the CheckBox of index {0} ({1}) to be rendered as {2}",
                    index, item, expectedState),
                expected.get(index), renderedCheckBox.isSelected());
        }

        List<Integer> expectedIndices = expected.stream().boxed().collect(Collectors.toList());
        assertEquals("Unexpected checked indices",
            expectedIndices, new ArrayList<>(checkModel.getCheckedIndices()));

        List<String> expectedItems = expected.stream().mapToObj(items::get).collect(Collectors.toList());
        assertEquals("Unexpected checked items",
            expectedItems, new ArrayList<>(checkModel.getCheckedItems()));
    }
}
