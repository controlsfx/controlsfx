/**
 * Copyright (c) 2022, 2026, ControlsFX
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

import javafx.beans.InvalidationListener;
import javafx.beans.property.BooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.text.MessageFormat;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class CheckBitSetModelBaseTest {

    private CheckBitSetModelBase<String> model;
    private ObservableList<String> items;
    private Map<String, BooleanProperty> itemBooleanMap;

    private static final String ROW_1_VALUE = "Row 1";
    private static final String ROW_2_VALUE = "Row 2";
    private static final String ROW_3_VALUE = "Row 3";
    private static final String ROW_4_VALUE = "Row 4";
    private static final String ROW_5_VALUE = "Row 5";
    private static final String ROW_6_VALUE = "Row 6";

    @Before
    public void setUp() {
        this.itemBooleanMap = new HashMap<>();
        this.items = FXCollections.observableArrayList(ROW_1_VALUE, ROW_2_VALUE, ROW_3_VALUE, ROW_4_VALUE, ROW_5_VALUE);
        model = new CheckComboBox.CheckComboBoxBitSetCheckModel<>(items, itemBooleanMap);
    }

    @Test public void testCheckNullObject() {
        model.check(null);
        assertTrue(model.isEmpty());
    }

    @Test
    public void testSingleCheck() {
        assertFalse(model.isChecked(3));
        model.check(3);
    }

    @Test public void testMultipleChecks() {
        model.clearChecks();
        assertTrue(model.isEmpty());

        model.check(3);
        model.check(4);
        assertTrue(model.isChecked(3));
        assertTrue(model.isChecked(4));
        assertFalse(model.isChecked(2));
    }

    @Test
    public void clearCheckWithSingleCheck() {
        assertFalse(model.isChecked(3));
        model.check(3);
        assertTrue(model.isChecked(3));
        model.clearCheck(3);
        assertFalse(model.isChecked(3));
    }

    @Test public void ensureIsEmptyIsAccurate() {
        assertTrue(model.isEmpty());
        model.check(3);
        assertFalse(model.isEmpty());
        model.clearChecks();
        assertTrue(model.isEmpty());
    }

    @Test public void testSingleCheckCallsListenerOnce() {
        AtomicInteger count = new AtomicInteger();
        model.getCheckedItems().addListener((ListChangeListener<String>) change -> {
            count.getAndIncrement();
        });

        assertEquals(0, count.get());

        model.check(1);
        assertEquals(1, count.get());
        assertTrue(model.isChecked(1));
    }

    @Test
    public void testMultipleCheckCallsListenerOnce() {
        AtomicInteger count = new AtomicInteger();
        model.getCheckedItems().addListener((ListChangeListener<String>) change -> {
            count.getAndIncrement();
        });

        assertEquals(0, count.get());

        model.checkIndices(1, 2, 3);
        assertEquals(1, count.get());
        assertTrue(model.isChecked(1));
        assertTrue(model.isChecked(2));
        assertTrue(model.isChecked(3));
    }

    @Test
    public void testClearChecksCallsListenerOnce() {
        model.checkIndices(1, 2, 3);
        AtomicInteger count = new AtomicInteger();
        model.getCheckedItems().addListener((ListChangeListener<String>) change -> {
            count.getAndIncrement();
        });
        model.clearChecks();
        assertEquals(1, count.get());
    }

    @Test
    // needs junit 5
    // @RepeatedTest(value = 10, failureThreshold = 2)
    public void testSingleCheckAddsItToIndicesList() {
        ObservableList<Integer> checkedIndicesList = model.getCheckedIndices();
        assertTrue(checkedIndicesList.isEmpty());
        model.check(ROW_3_VALUE);
        assertNotEquals(-1, checkedIndicesList.indexOf(model.getItemIndex(ROW_3_VALUE)));
    }

    @Test
    public void testCheckedItemsContainsCorrectValuesAfterClearCheck() {
        // derived from https://github.com/controlsfx/controlsfx/issues/1550
        model.clearChecks();
        assertTrue(model.isEmpty());

        model.check(2);
        model.check(4);
        assertTrue(model.isChecked(2));
        assertTrue(model.isChecked(4));
        model.clearCheck(2);
        assertFalse(model.isChecked(2));
        assertTrue(model.isChecked(4));

        ObservableList<String> checkedItems = model.getCheckedItems();
        assertEquals(1, checkedItems.size());
        assertTrue(checkedItems.contains(ROW_5_VALUE));
    }

    @Test
    public void testCheckedItemsContainsCorrectValuesAscendingCheckDescendingUncheck() {
        model.clearChecks();
        assertTrue(model.isEmpty());

        ObservableList<String> checkedItems = model.getCheckedItems();

        // check indexes 0 to 4
        for (int checkIdx = 0; checkIdx < 5; checkIdx++) {
            model.check(checkIdx);
            // verify checked size and which items are checked
            assertEquals(
                MessageFormat.format("Expected checked items size to be {0} when checking from index 0 to {1}", checkIdx + 1, checkIdx),
                checkIdx + 1, checkedItems.size());

            for (int checkedIdx = 0; checkedIdx < 5; checkedIdx++) {
                assertEquals(
                    MessageFormat.format("Expected index {0} to be {1} when checking in ascending order from 0 to {2}",
                        checkedIdx,
                        checkedIdx <= checkIdx ? "checked" : "not checked",
                        checkIdx),
                    checkedIdx <= checkIdx, model.isChecked(checkedIdx));
            }
        }

        // uncheck indexes 4 to 0
        for (int uncheckIdx = 4; uncheckIdx >= 0; uncheckIdx--) {
            model.clearCheck(uncheckIdx);
            // verify checked size and which items are checked
            assertEquals(MessageFormat.format("Expected checked items size to be {0} when unchecking from index 4 to {1}", uncheckIdx, uncheckIdx),
                uncheckIdx, checkedItems.size());

            for (int checkedIdx = 0; checkedIdx < 5; checkedIdx++) {
                assertEquals(
                    MessageFormat.format("Expected index {0} to be {1} when checking in descending order from 5 to {2}",
                        checkedIdx,
                        checkedIdx < uncheckIdx ? "checked" : "not checked",
                        uncheckIdx),
                    checkedIdx < uncheckIdx, model.isChecked(checkedIdx));
            }
        }
    }

    @Test
    public void testCheckedItemsContainsCorrectValuesDescendingCheckAscendingUncheck() {
        model.clearChecks();
        assertTrue(model.isEmpty());

        ObservableList<String> checkedItems = model.getCheckedItems();

        // check indexes 0 to 4
        for (int checkIdx = 4; checkIdx >= 0; checkIdx--) {
            model.check(checkIdx);
            // verify checked size and which items are checked
            assertEquals(
                MessageFormat.format("Expected checked items size to be {0} when checking from index 4 to {1}", 5 - checkIdx, checkIdx),
                5 - checkIdx, checkedItems.size());

            for (int checkedIdx = 0; checkedIdx < 5; checkedIdx++) {
                assertEquals(
                    MessageFormat.format("Expected index {0} to be {1} when checking in descending order from 4 to {2}",
                        checkedIdx,
                        checkedIdx >= checkIdx ? "checked" : "not checked",
                        checkIdx),
                    checkedIdx >= checkIdx, model.isChecked(checkedIdx));
            }
        }

        // uncheck indexes 4 to 0
        for (int uncheckIdx = 0; uncheckIdx < 5; uncheckIdx++) {
            model.clearCheck(uncheckIdx);
            // verify checked size and which items are checked
            assertEquals(
                MessageFormat.format("Expected checked items size to be {0} when unchecking from index 0 to {1}", 4 - uncheckIdx, uncheckIdx),
                4 - uncheckIdx, checkedItems.size());

            for (int checkedIdx = 0; checkedIdx < 5; checkedIdx++) {
                assertEquals(
                     MessageFormat.format("Expected index {0} to be {1} when checking in ascending order from 0 to {2}",
                        checkedIdx,
                        checkedIdx > uncheckIdx ? "checked" : "not checked",
                        uncheckIdx),
                    checkedIdx > uncheckIdx, model.isChecked(checkedIdx));
            }
        }
    }

    @Test
    public void testCheckedItemsContainsCorrectValuesAfterCheckInOrder() {
        // derived from https://github.com/controlsfx/controlsfx/issues/1549
        // Checking index 0 last does not expose the defect the tests below reproduce: the lookup
        // shortcut of the checked indices list is only taken for indices greater than 0. This
        // scenario therefore guards against a regression of the original issue.
        model.check(1);
        model.check(2);
        model.check(0);

        assertOnlyCheckedIndicesAre(0, 1, 2);
    }


    
    @Test
    public void testCheckedItemsUnexpectedIndexAfterClear() {
        // test for issue identified here: https://github.com/controlsfx/controlsfx/issues/1531#issuecomment-1929277168
        this.itemBooleanMap = new HashMap<>();
        this.items = FXCollections.observableArrayList(ROW_1_VALUE, ROW_2_VALUE, ROW_3_VALUE);
        model = new CheckComboBox.CheckComboBoxBitSetCheckModel<>(items, itemBooleanMap);

        model.check(2);
        model.check(1);

        assertFalse(model.isChecked(0));
        assertTrue(model.isChecked(1));
        assertTrue(model.isChecked(2));

        model.clearChecks();

        assertFalse(model.isChecked(0));
        assertFalse(model.isChecked(1));
        assertFalse(model.isChecked(2));

        model.check(2);

        assertFalse(model.isChecked(0));
        assertFalse(model.isChecked(1));
        assertTrue(model.isChecked(2));
    }

    @Test
    public void testItemBooleanPropertiesAfterCheckingIntermediateIndexLast() {
        // derived from https://github.com/controlsfx/controlsfx/issues/1549#issuecomment-5542131988
        model.check(0);
        model.check(2);
        model.check(3);
        model.check(1);

        assertOnlyCheckedIndicesAre(0, 1, 2, 3);
    }

    @Test
    public void testItemBooleanPropertiesAfterCheckingTheSameIndexTwice() {
        // variant of https://github.com/controlsfx/controlsfx/issues/1549 where the same index is
        // checked twice - a no-op which still has to leave the model fit for the next check
        model.check(0);
        model.check(1);
        model.check(1);
        model.check(3);

        assertOnlyCheckedIndicesAre(0, 1, 3);
    }

    @Test
    public void testItemBooleanPropertiesAfterCheckIndicesOnIntermediateIndex() {
        // variant of https://github.com/controlsfx/controlsfx/issues/1549 going through
        // checkIndices(int...) instead of check(int)
        model.check(0);
        model.check(3);
        model.check(4);
        model.checkIndices(1);

        assertOnlyCheckedIndicesAre(0, 1, 3, 4);
    }

    @Test
    public void testItemBooleanPropertiesAfterClearCheckOnUncheckedIndex() {
        // variant of https://github.com/controlsfx/controlsfx/issues/1549 where an unchecked index
        // is cleared - a no-op on the check model - before an intermediate index is checked
        model.check(0);
        model.check(3);
        model.check(4);
        model.clearCheck(1);
        model.check(2);

        assertOnlyCheckedIndicesAre(0, 2, 3, 4);
    }

    @Test
    public void testItemBooleanPropertiesAfterCheckIndicesNotInAscendingOrder() {
        // variant of https://github.com/controlsfx/controlsfx/issues/1549 where the indices
        // handed to checkIndices(int...) are in descending order
        model.checkIndices(4, 0);

        assertOnlyCheckedIndicesAre(0, 4);
    }

    @Test
    public void testItemBooleanPropertiesAfterCheckIndicesWithDuplicateIndex() {
        // variant of https://github.com/controlsfx/controlsfx/issues/1549 where the same index is
        // handed twice to checkIndices(int...)
        model.checkIndices(4, 4);

        assertOnlyCheckedIndicesAre(4);
    }

    @Test
    public void testItemBooleanPropertiesAfterCheckIndicesAroundAnAlreadyCheckedIndex() {
        // variant of https://github.com/controlsfx/controlsfx/issues/1549 where the indices handed
        // to checkIndices(int...) surround an already checked index. The added indices are then
        // spread over two runs of the checked indices list, each of which has to be reported.
        model.check(2);
        model.checkIndices(0, 4);

        assertOnlyCheckedIndicesAre(0, 2, 4);
    }

    @Test
    public void testItemBooleanPropertiesAfterAnItemIsAddedToTheList() {
        // adding an item makes the check model follow the change of the item list
        model.check(1);

        items.add(ROW_6_VALUE);

        assertOnlyCheckedIndicesAre(1);
    }

    @Test
    public void testCheckIndicesIgnoresNegativeIndex() {
        model.checkIndices(-1);

        assertOnlyCheckedIndicesAre();
    }

    @Test
    public void testCheckIndicesIgnoresIndexBeyondItemCount() {
        model.checkIndices(items.size());

        assertOnlyCheckedIndicesAre();
    }

    @Test
    public void testCheckedIndicesKeepReportingTheSameValueBeyondTheCheckedCount() {
        // an index between size() and getItemCount() is not backed by any checked index: get(int)
        // answers -1 for it, and has to keep doing so however many times it is asked
        model.check(0);
        model.check(1);

        ObservableList<Integer> checkedIndices = model.getCheckedIndices();
        assertEquals("Unexpected value beyond the checked count",
            Integer.valueOf(-1), checkedIndices.get(3));
        assertEquals("Unexpected value beyond the checked count",
            Integer.valueOf(-1), checkedIndices.get(4));
        assertEquals("Unexpected value beyond the checked count, read a second time",
            Integer.valueOf(-1), checkedIndices.get(3));
    }

    @Test
    public void testCheckedIndicesAreCorrectAfterAReadBeyondTheCheckedCount() {
        // reading an index beyond the checked count must not disturb the iteration state get(int)
        // caches, otherwise the next read of a valid index is computed from a stale state
        model.check(0);
        model.check(1);

        ObservableList<Integer> checkedIndices = model.getCheckedIndices();
        assertEquals(Integer.valueOf(-1), checkedIndices.get(3));
        assertEquals("Unexpected checked index at position 1",
            Integer.valueOf(1), checkedIndices.get(1));
    }

    @Test
    public void testClearCheckOnAnUncheckedIndexReportsNoChange() {
        // clearing an unchecked index is a no-op on the check model, so it must not report a
        // removal: there is no position in the checked indices list such a change could describe
        model.check(0);
        model.check(2);
        List<String> changes = recordChangesOfCheckedIndices();

        model.clearCheck(1);

        assertEquals("Unexpected change reported for an unchanged check model",
            Collections.<String>emptyList(), changes);
    }

    @Test
    public void testCheckOnAnAlreadyCheckedIndexReportsNoChange() {
        // checking an already checked index is a no-op on the check model, so it must not report
        // an addition: the index is already part of the checked indices list
        model.check(1);
        List<String> changes = recordChangesOfCheckedIndices();

        model.check(1);

        assertEquals("Unexpected change reported for an unchanged check model",
            Collections.<String>emptyList(), changes);
    }

    @Test
    public void testIsCheckedOnAnItemWhichIsNotInTheList() {
        assertFalse(model.isChecked("Not in the list"));
    }

    @Test
    public void testRemovingACheckedItemKeepsTheOtherChecks() {
        model.check(0);
        model.check(4);

        items.remove(4);

        assertOnlyCheckedIndicesAre(0);
    }

    @Test
    public void testACheckFollowsItsItemWhenAnEarlierItemIsRemoved() {
        // the check made on ROW_5_VALUE, at index 4, moves down to index 3 with it
        model.check(4);

        items.remove(0);

        assertOnlyCheckedIndicesAre(3);
    }

    @Test
    public void testRemovingACheckedItemReportsTheCheckItTakesAway() {
        // the check made on ROW_5_VALUE dies with the item, and a listener of the checked indices
        // has to be told about it, otherwise it goes on showing a check the model no longer holds
        model.check(4);
        List<Integer> observedCheckedIndices = observe(model.getCheckedIndices());

        items.remove(4);

        assertEquals("The reported changes do not add up to the checked indices",
            new ArrayList<>(model.getCheckedIndices()), observedCheckedIndices);
    }

    @Test
    public void testRemovingAnItemBeforeACheckedItemReportsTheReindexedCheck() {
        // the check made on ROW_5_VALUE moves down from index 4 to index 3 with it: a listener
        // which was told index 4 is checked has to be told it is now index 3
        model.check(4);
        List<Integer> observedCheckedIndices = observe(model.getCheckedIndices());

        items.remove(0);

        assertEquals("The reported changes do not add up to the checked indices",
            new ArrayList<>(model.getCheckedIndices()), observedCheckedIndices);
    }

    @Test
    public void testRemovingACheckedItemReportsAChangeOfTheCheckedItems() {
        // CheckComboBoxSkin refreshes the text of its button from a listener of the checked items,
        // and so keeps rendering an item which is gone until that change is reported
        model.check(4);
        AtomicInteger count = new AtomicInteger();
        model.getCheckedItems().addListener((ListChangeListener<String>) change -> count.getAndIncrement());

        items.remove(4);

        assertEquals("Expected the check the removed item takes away to be reported once",
            1, count.get());
    }

    @Test
    public void testTheBooleanPropertyOfAnItemFollowsItsCheckAfterAnItemIsAdded() {
        // getItemBooleanProperty(T) is public API, handed out to be bound to: the property given
        // for an item has to go on following the checks made on that item, whatever happens to the
        // list of items. This is how the CheckBox of a CheckListView row holds it.
        BooleanProperty rowProperty = model.getItemBooleanProperty(ROW_2_VALUE);

        items.add(ROW_6_VALUE);
        model.check(1);

        assertTrue("The property handed out for the item no longer follows its check",
            rowProperty.get());
    }

    @Test
    public void testTheBooleanPropertyOfACheckedItemIsClearedWhenThatItemLeavesTheList() {
        // the property of an item is public API, handed out to be bound to, and it stands for the
        // check the model holds on that item: an item taken out of the list holds no check any
        // more, so whoever is bound to its property must not go on rendering the one it had
        BooleanProperty rowProperty = model.getItemBooleanProperty(ROW_5_VALUE);
        model.check(4);

        items.remove(ROW_5_VALUE);

        assertFalse("The property handed out for a removed item still reports its former check",
            rowProperty.get());
    }

    @Test
    public void testClearChecksOnAnUncheckedModelReportsNoChange() {
        List<String> changes = recordChangesOfCheckedIndices();

        model.clearChecks();

        assertEquals("Unexpected change reported for an unchanged check model",
            Collections.<String>emptyList(), changes);
    }

    @Test
    public void testCheckAllCallsListenerOnce() {
        AtomicInteger count = new AtomicInteger();
        model.getCheckedItems().addListener((ListChangeListener<String>) change -> {
            count.getAndIncrement();
        });

        model.checkAll();

        assertEquals("Expected a single change for the whole check", 1, count.get());
        assertOnlyCheckedIndicesAre(0, 1, 2, 3, 4);
    }

    @Test
    public void testTheBooleanPropertyOfAnItemDrivesTheCheckModelBuiltAfterIt() {
        // a control which replaces its list of items builds a new check model around the very
        // same item boolean map, and the properties already handed out live on in that map: the
        // check box bound to one of them has to go on driving the check model the control holds
        CheckBitSetModelBase<String> newModel =
            new CheckComboBox.CheckComboBoxBitSetCheckModel<>(items, itemBooleanMap);

        newModel.getItemBooleanProperty(ROW_2_VALUE).set(true);

        assertTrue("The check box of the item no longer drives the check model built after it",
            newModel.isChecked(1));
    }

    @Test
    public void testTheBooleanPropertyOfADuplicatedItemKeepsTheCheckWhichSurvives() {
        // two equal items are two rows, each holding its own check, but a single BooleanProperty:
        // once one of the rows is gone, that property has to stand for the check the other keeps
        this.itemBooleanMap = new HashMap<>();
        this.items = FXCollections.observableArrayList(ROW_1_VALUE, ROW_1_VALUE);
        model = new CheckComboBox.CheckComboBoxBitSetCheckModel<>(items, itemBooleanMap);
        model.check(0);
        model.check(1);

        items.remove(0);

        assertOnlyCheckedIndicesAre(0);
    }

    @Test
    public void testACheckWhichFollowedItsItemIsNotToggledAlongTheWay() {
        // a change of the item list reindexes the checks, it does not undo and redo them: an
        // application bound to the property of a still checked item must not be told otherwise
        model.check(1);
        model.check(3);
        List<Boolean> reportedValues = new ArrayList<>();
        model.getItemBooleanProperty(ROW_2_VALUE)
            .addListener((o, wasChecked, isChecked) -> reportedValues.add(isChecked));

        items.remove(0);

        assertEquals("Unexpected change reported on a check the removal left untouched",
            Collections.<Boolean>emptyList(), reportedValues);
    }

    @Test
    public void testCheckingFromTheBooleanPropertyOfARemovedItemReportsCoherentChanges() {
        // the property of an item is handed out to be bound to, and whoever listens to it is free
        // to check another item when it changes - the model is then asked to check while it is
        // still following a change of the item list, and what it reports has to hold all the same
        model.check(0);
        model.check(4);
        model.getItemBooleanProperty(ROW_1_VALUE).addListener((o, wasChecked, isChecked) -> {
            if (!isChecked) {
                model.check(1);
            }
        });
        List<Integer> observedCheckedIndices = observe(model.getCheckedIndices());

        items.remove(0);

        assertEquals("The reported changes do not add up to the checked indices",
            new ArrayList<>(model.getCheckedIndices()), observedCheckedIndices);
    }

    @Test
    public void testReorderingTheItemsReportsTheChangeItMakesToTheCheckedItems() {
        // the sort swaps the two checked items, so it leaves the very same indices checked but
        // not the same items sitting at them: CheckComboBoxSkin renders the checked items, and
        // goes on rendering the ones it was last told about until that change is reported
        model.check(0);
        model.check(4);
        List<String> observedCheckedItems = observe(model.getCheckedItems());

        items.sort(Comparator.reverseOrder());

        assertEquals("The reported changes do not add up to the checked items",
            new ArrayList<>(model.getCheckedItems()), observedCheckedItems);
    }

    @Test
    public void testClearingACheckIsNotUndoneByTheCheckMadeFromItsBooleanProperty() {
        // whoever holds the property of an item is free to check another one when it changes, so
        // the model ends up writing a property from within the write of that very property: what
        // it writes there must not be read back, once the outer write resumes, as the user
        // toggling the check box that property stands for
        model.check(0);
        final BooleanProperty itemBooleanProperty = model.getItemBooleanProperty(ROW_1_VALUE);
        // an InvalidationListener, which is what a binding on that property registers, and which
        // is notified in the order it was added - ahead of every ChangeListener
        itemBooleanProperty.addListener((InvalidationListener) o -> {
            if (!itemBooleanProperty.get()) {
                model.check(1);
            }
        });
        // a change of the item list has the model go over the property of every item again, and
        // must leave it listening ahead of the application all the same
        items.add(ROW_6_VALUE);

        model.clearCheck(0);

        assertOnlyCheckedIndicesAre(1);
    }

    @Test
    public void testClearingOneOfTheRowsOfADuplicatedItemClearsThemBoth() {
        // the rows holding equal items share a single check box, so clearing either of them has
        // to reach them both - as checking either of them does - otherwise the check box goes on
        // rendering a check the model only holds on the other row
        this.itemBooleanMap = new HashMap<>();
        this.items = FXCollections.observableArrayList(ROW_1_VALUE, ROW_1_VALUE, ROW_2_VALUE);
        model = new CheckComboBox.CheckComboBoxBitSetCheckModel<>(items, itemBooleanMap);
        model.check(0);

        model.clearCheck(0);

        assertOnlyCheckedIndicesAre();
        assertFalse("The item is still reported checked", model.isChecked(ROW_1_VALUE));
    }

    @Test
    public void testTogglingOneOfTheRowsOfADuplicatedItemClearsThemBoth() {
        // toggleCheckState(int) on a checked row goes through clearCheck(int), and has to leave
        // the other row of the item unchecked along with it
        this.itemBooleanMap = new HashMap<>();
        this.items = FXCollections.observableArrayList(ROW_1_VALUE, ROW_1_VALUE, ROW_2_VALUE);
        model = new CheckComboBox.CheckComboBoxBitSetCheckModel<>(items, itemBooleanMap);
        model.check(1);

        model.toggleCheckState(0);

        assertOnlyCheckedIndicesAre();
    }

    @Test
    public void testCheckedItemsAnswerNullBeyondTheCheckedCount() {
        // getCheckedItems().get(int) has always answered null for a position it does not hold,
        // as getCheckedIndices().get(int) answers -1: callers walking the two lists side by side
        // rely on neither of them throwing
        model.check(0);
        model.check(1);

        assertEquals("Unexpected item beyond the checked count", null, model.getCheckedItems().get(3));
    }

    @Test
    public void testAChangeOfTheCheckedItemsCanBeReadByEveryListenerItReaches() {
        // the model writes the item boolean properties while the change of the checked items is
        // being reported, and whoever holds one of them is free to check or clear items from
        // there: the change already handed out has to go on reporting what it was built with,
        // whatever the model holds by the time the next listener reads it
        List<String> failures = new ArrayList<>();
        model.getItemBooleanProperty(ROW_1_VALUE).addListener((o, wasChecked, isChecked) -> {
            if (isChecked) {
                model.clearChecks();
            }
        });
        model.getCheckedItems().addListener((ListChangeListener<String>) change -> {
            try {
                while (change.next()) {
                    change.getRemoved();
                    change.getAddedSubList();
                }
            } catch (RuntimeException e) {
                failures.add(e.toString());
            }
        });

        model.checkAll();

        assertEquals("A listener was given a change of the checked items it cannot read",
            Collections.<String>emptyList(), failures);
    }

    @Test
    public void testSettingTheSameItemsAgainKeepsTheChecks() {
        // an application which refetches its data and hands the very same items back has changed
        // nothing of what the user sees: the checks it made are still on the rows holding them
        model.check(1);

        items.setAll(ROW_1_VALUE, ROW_2_VALUE, ROW_3_VALUE, ROW_4_VALUE, ROW_5_VALUE);

        assertOnlyCheckedIndicesAre(1);
    }

    @Test
    public void testCheckingOneOfTheRowsOfADuplicatedItemChecksThemBoth() {
        // two equal items share a single BooleanProperty, and therefore a single check box - see
        // CheckComboBox.getItemBooleanProperty(T): the model cannot hold one of those rows
        // checked and the other not, as the check box standing for both would then render a
        // check the model denies
        this.itemBooleanMap = new HashMap<>();
        this.items = FXCollections.observableArrayList(ROW_1_VALUE, ROW_1_VALUE, ROW_2_VALUE);
        model = new CheckComboBox.CheckComboBoxBitSetCheckModel<>(items, itemBooleanMap);

        model.check(1);

        assertOnlyCheckedIndicesAre(0, 1);
    }

    @Test
    public void testTheCheckedIndicesAnswerNoPositionForAValueTheyDoNotHold() {
        // getCheckedIndices().get(int) answers -1 for a position beyond the checks it holds, and
        // that -1 goes straight back into indexOf whenever a caller round-trips a value through
        // the list
        model.check(2);

        assertEquals("Unexpected position answered for a value the checked indices do not hold",
            -1, model.getCheckedIndices().indexOf(-1));
    }

    @Test
    public void testCheckingFromTheBooleanPropertyOfAnItemChecksEveryRowOfADuplicatedItem() {
        // whoever holds the property of an item is free to check another one when it changes, and
        // the check made from there is a check like any other: the rows holding equal items share
        // a single check box, so a check on either of them has to reach them both
        this.itemBooleanMap = new HashMap<>();
        this.items = FXCollections.observableArrayList(ROW_1_VALUE, ROW_2_VALUE, ROW_2_VALUE);
        model = new CheckComboBox.CheckComboBoxBitSetCheckModel<>(items, itemBooleanMap);
        model.getItemBooleanProperty(ROW_1_VALUE).addListener((o, wasChecked, isChecked) -> {
            if (isChecked) {
                model.check(1);
            }
        });

        model.check(0);

        assertOnlyCheckedIndicesAre(0, 1, 2);
    }

    @Test
    public void testTheChecksFollowTheirItemsWhenTheItemListChangesAgainFromTheirReport() {
        // whoever is told about the reindexed checks is free to change the item list again from
        // there, and the checks have to follow that change as they follow any other: a list which
        // is changed again while it is still reporting hands the model the range it already
        // followed along with the new one
        model.check(1);
        model.check(3);
        AtomicInteger reportedChanges = new AtomicInteger();
        model.getCheckedItems().addListener((ListChangeListener<String>) change -> {
            if (reportedChanges.getAndIncrement() == 0) {
                items.remove(ROW_5_VALUE);
            }
        });

        items.remove(ROW_1_VALUE);

        // neither of the removed items was ever checked, so the checks of ROW_2 and ROW_4 are
        // still theirs - at the positions the two removals leave them at
        assertOnlyCheckedIndicesAre(0, 2);
    }

    @Test
    public void testClearingEveryCheckFromTheReportOfACheckLeavesNoCheckBoxChecked() {
        // a listener of the checked indices is free to clear the checks from there, which leaves
        // the change still being reported standing for a check the model no longer holds: what
        // the check boxes render has to be the checks the model ends up with, not that one
        model.check(2);
        AtomicInteger reportedChanges = new AtomicInteger();
        model.getCheckedIndices().addListener((ListChangeListener<Integer>) change -> {
            if (reportedChanges.getAndIncrement() == 0) {
                model.clearChecks();
            }
        });

        model.check(4);

        assertOnlyCheckedIndicesAre();
    }

    @Test
    public void testClearingEveryCheckFromTheReportOfACheckReportsCoherentChangesOfTheCheckedItems() {
        // the checks are reported as two lists, one after the other, and a listener of either of
        // them is free to change the model from there: whoever rebuilds the checked items from the
        // changes handed to him has to end up with the items the model holds, whichever of the two
        // lists the change which reached the model came from
        model.check(2);
        List<String> observedCheckedItems = observe(model.getCheckedItems());
        AtomicInteger reportedChanges = new AtomicInteger();
        model.getCheckedIndices().addListener((ListChangeListener<Integer>) change -> {
            if (reportedChanges.getAndIncrement() == 0) {
                model.clearChecks();
            }
        });

        model.check(4);

        assertEquals("The reported changes do not add up to the checked items",
            Collections.<String>emptyList(), observedCheckedItems);
    }

    @Test
    public void testCheckingThroughADetachedModelLeavesTheRowsOfItsFormerListAlone() {
        // a control which replaces its list of items detaches the model it drops, and that model
        // goes on holding the rows it had indexed: a check made through it must not reach a row
        // the list no longer has - the model reads the item of every row it reports a check for
        this.itemBooleanMap = new HashMap<>();
        this.items = FXCollections.observableArrayList(ROW_1_VALUE, ROW_2_VALUE, ROW_1_VALUE);
        model = new CheckComboBox.CheckComboBoxBitSetCheckModel<>(items, itemBooleanMap);
        model.detach();
        items.remove(2);

        model.check(0);

        assertEquals("The check reached a row beyond the end of the item list",
            Collections.singletonList(0), new ArrayList<>(model.getCheckedIndices()));
    }

    @Test
    public void testTheBooleanPropertyOfACheckedItemIsClearedWhenAModelIsBuiltOnAnotherList() {
        // a control which replaces its list of items builds a new check model around the very
        // same item boolean map, and the properties already handed out live on in that map: an
        // item which is not in the new list holds no check any more, so the property standing
        // for it must not go on reporting the one it had
        BooleanProperty rowProperty = model.getItemBooleanProperty(ROW_1_VALUE);
        model.check(0);

        new CheckComboBox.CheckComboBoxBitSetCheckModel<>(
            FXCollections.observableArrayList(ROW_6_VALUE), itemBooleanMap);

        assertFalse("The property handed out for an item of the former list still reports its check",
            rowProperty.get());
    }

    @Test
    public void testAReportedChangeStaysReadableByTheListenersItHasYetToReach() {
        // a listener of the checked indices is free to clear the checks from there, which leaves
        // the change being reported standing for checks the model no longer holds: the listeners
        // that change has yet to reach must still be able to read it, whatever the model now holds
        model.check(2);
        AtomicInteger reportedChanges = new AtomicInteger();
        model.getCheckedIndices().addListener((ListChangeListener<Integer>) change -> {
            if (reportedChanges.getAndIncrement() == 0) {
                model.clearChecks();
            }
        });
        List<String> failures = new ArrayList<>();
        model.getCheckedIndices().addListener((ListChangeListener<Integer>) change -> {
            try {
                String.valueOf(change);
            } catch (RuntimeException e) {
                failures.add(e.toString());
            }
        });

        model.check(4);

        assertEquals("A reported change could not be read",
            Collections.<String>emptyList(), failures);
    }

    @Test
    public void testAnItemIsNoLongerCheckedWhenTheModelClearsItsBooleanProperty() {
        // the property of an item stands for the check the model holds on it, and a listener of
        // that property is free to reach back into the model: what the model answers there has to
        // be what the property reports, not the check the item held before it left the list
        BooleanProperty rowProperty = model.getItemBooleanProperty(ROW_1_VALUE);
        model.check(0);
        List<Boolean> reportedByTheModel = new ArrayList<>();
        rowProperty.addListener(o -> reportedByTheModel.add(model.isChecked(ROW_1_VALUE)));

        items.remove(ROW_1_VALUE);

        assertEquals("The model reports a check for the item whose property it is clearing",
            Collections.singletonList(false), reportedByTheModel);
    }

    @Test
    public void testNoBooleanPropertyIsLeftBehindWhenTheItemsChangeWhileTheModelIsIndexingThem() {
        // a listener of an item property is free to change the list of items from there, which
        // re-indexes the model while it is walking the very items it is indexing: whatever that
        // listener leaves in the list, no property may be left behind for an item which is gone,
        // as getItemBooleanProperty(T) would hand it out as the property of an item of the list
        this.itemBooleanMap = new HashMap<>();
        this.items = FXCollections.observableArrayList(ROW_1_VALUE, ROW_2_VALUE);
        model = new CheckComboBox.CheckComboBoxBitSetCheckModel<>(items, itemBooleanMap);
        model.checkAll();
        model.getItemBooleanProperty(ROW_1_VALUE).addListener(o -> items.clear());
        model.getItemBooleanProperty(ROW_2_VALUE).addListener(o -> items.clear());

        // the control replaces its list of items: the model built for the new one clears the
        // properties of the checks it does not hold, and the listeners above empty the list
        CheckBitSetModelBase<String> newModel =
            new CheckComboBox.CheckComboBoxBitSetCheckModel<>(items, itemBooleanMap);

        assertNull("A property is left behind for an item which is no longer in the list",
            newModel.getItemBooleanProperty(ROW_1_VALUE));
        assertNull("A property is left behind for an item which is no longer in the list",
            newModel.getItemBooleanProperty(ROW_2_VALUE));
    }

    /**
     * Returns a copy of the given list of checks - {@link IndexedCheckModel#getCheckedIndices()}
     * or {@link IndexedCheckModel#getCheckedItems()} - kept up to date from the changes that list
     * reports from now on. The copy drifting away from the list itself is the only thing a
     * listener can see of a change which was never reported, or reported wrong.
     */
    private <E> List<E> observe(ObservableList<E> checks) {
        final List<E> observed = new ArrayList<>(checks);
        checks.addListener((ListChangeListener<E>) change -> {
            while (change.next()) {
                if (change.wasPermutated()) {
                    List<E> permutated = new ArrayList<>(observed.subList(change.getFrom(), change.getTo()));
                    for (int i = change.getFrom(); i < change.getTo(); i++) {
                        observed.set(change.getPermutation(i), permutated.get(i - change.getFrom()));
                    }
                } else {
                    observed.subList(change.getFrom(), change.getFrom() + change.getRemovedSize()).clear();
                    observed.addAll(change.getFrom(), change.getAddedSubList());
                }
            }
        });
        return observed;
    }

    /**
     * Records a description of every change {@link IndexedCheckModel#getCheckedIndices()} reports
     * from now on. Asserting on those descriptions rather than on a list rebuilt from the changes
     * is deliberate: JavaFX hands the exceptions thrown by a {@link ListChangeListener} over to the
     * uncaught exception handler of the thread, so a listener choking on a malformed change would
     * go unnoticed.
     */
    private List<String> recordChangesOfCheckedIndices() {
        final List<String> changes = new ArrayList<>();
        model.getCheckedIndices().addListener((ListChangeListener<Integer>) change -> {
            while (change.next()) {
                StringBuilder description = new StringBuilder();
                description.append("from ").append(change.getFrom()).append(" to ").append(change.getTo());
                if (change.wasRemoved()) {
                    description.append(", removed ").append(change.getRemoved());
                }
                if (change.wasAdded()) {
                    description.append(", added ").append(change.getAddedSubList());
                }
                changes.add(description.toString());
            }
        });
        return changes;
    }

    /**
     * Asserts that exactly the given indices are checked, both in the check model and in the
     * {@link BooleanProperty} of every item - the property each check box is bound to, and
     * therefore what the user actually sees.
     */
    private void assertOnlyCheckedIndicesAre(int... expectedCheckedIndices) {
        BitSet expected = new BitSet();
        for (int index : expectedCheckedIndices) {
            expected.set(index);
        }

        for (int index = 0; index < items.size(); index++) {
            boolean shouldBeChecked = expected.get(index);
            String state = shouldBeChecked ? "checked" : "not checked";

            assertEquals(
                MessageFormat.format("Expected index {0} to be {1} in the check model", index, state),
                shouldBeChecked, model.isChecked(index));

            BooleanProperty itemBooleanProperty = model.getItemBooleanProperty(items.get(index));
            assertNotNull(
                MessageFormat.format("Expected a BooleanProperty for index {0}", index),
                itemBooleanProperty);
            assertEquals(
                MessageFormat.format("Expected the check box of index {0} to be {1}", index, state),
                shouldBeChecked, itemBooleanProperty.get());
        }

        List<Integer> expectedIndices = expected.stream().boxed().collect(Collectors.toList());
        assertEquals("Unexpected checked indices",
            expectedIndices, new ArrayList<>(model.getCheckedIndices()));

        List<String> expectedItems = expected.stream().mapToObj(items::get).collect(Collectors.toList());
        assertEquals("Unexpected checked items",
            expectedItems, new ArrayList<>(model.getCheckedItems()));
    }
}
