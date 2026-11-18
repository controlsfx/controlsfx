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

import impl.org.controlsfx.skin.GridViewSkin;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.beans.Observable;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.SelectionMode;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import org.junit.BeforeClass;
import org.junit.Test;
import org.testfx.api.FxToolkit;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.*;

public class GridViewTest {
    @BeforeClass
    public static void setupToolkit() throws TimeoutException {
        FxToolkit.registerPrimaryStage();
    }

    @Test
    public void supportsSingleAndMultipleSelectionWithReadOnlyObservableLists() throws TimeoutException {
        FxToolkit.setupFixture(() -> {
            GridView<String> grid = strings();
            MultipleSelectionModel<String> model = grid.getSelectionModel();
            assertEquals(SelectionMode.SINGLE, model.getSelectionMode());
            assertTrue(model.isEmpty());
            model.selectIndices(0, 2, 1);
            assertEquals(List.of(1), model.getSelectedIndices());
            assertEquals("b", model.getSelectedItem());
            model.setSelectionMode(SelectionMode.MULTIPLE);
            model.getSelectedIndices().addListener((ListChangeListener<Integer>) change ->
                    assertEquals(model.getSelectedIndices().stream().map(grid.getItems()::get)
                            .collect(Collectors.toList()), model.getSelectedItems()));
            model.selectIndices(2, 0, 2, -5, 20);
            assertEquals(List.of(0, 1, 2), model.getSelectedIndices());
            assertEquals(List.of("a", "b", "c"), model.getSelectedItems());
            assertThrows(UnsupportedOperationException.class, () -> model.getSelectedIndices().add(3));
            assertThrows(UnsupportedOperationException.class, () -> model.getSelectedItems().clear());
            model.clearSelection(2);
            assertEquals(List.of(0, 1), model.getSelectedIndices());
            assertEquals(1, model.getSelectedIndex());
            model.setSelectionMode(SelectionMode.SINGLE);
            assertEquals(List.of(1), model.getSelectedIndices());
            model.clearSelection();
            assertNull(model.getSelectedItem());
            assertEquals(-1, model.getSelectedIndex());
        });
    }

    @Test
    public void supportsRangeAllAndAdjacentSelection() throws TimeoutException {
        FxToolkit.setupFixture(() -> {
            MultipleSelectionModel<String> model = strings().getSelectionModel();
            model.selectAll();
            assertTrue(model.isEmpty());
            model.selectFirst();
            model.selectNext();
            assertEquals(1, model.getSelectedIndex());
            model.selectPrevious();
            assertEquals(0, model.getSelectedIndex());
            model.selectLast();
            assertEquals(3, model.getSelectedIndex());
            model.clearSelection();
            model.setSelectionMode(SelectionMode.MULTIPLE);
            model.selectRange(3, 0);
            assertEquals(List.of(1, 2, 3), model.getSelectedIndices());
            assertEquals(1, model.getSelectedIndex());
            model.selectAll();
            assertEquals(List.of(0, 1, 2, 3), model.getSelectedIndices());
            model.select(-1);
            assertTrue(model.isEmpty());
        });
    }

    @Test
    public void selectionFollowsInsertionsRemovalsReplacementsAndPermutations() throws TimeoutException {
        FxToolkit.setupFixture(() -> {
            GridView<String> grid = strings();
            MultipleSelectionModel<String> model = grid.getSelectionModel();
            model.setSelectionMode(SelectionMode.MULTIPLE);
            model.selectIndices(1, 3);
            grid.getItems().add(0, "z");
            assertEquals(List.of(2, 4), model.getSelectedIndices());
            assertEquals(List.of("b", "d"), model.getSelectedItems());
            grid.getItems().remove(2);
            assertEquals(List.of(3), model.getSelectedIndices());
            grid.getItems().set(3, "e");
            assertEquals("e", model.getSelectedItem());
            assertEquals(List.of("e"), model.getSelectedItems());
            assertEquals("e", grid.getFocusModel().getFocusedItem());
            FXCollections.sort(grid.getItems());
            assertEquals(List.of(2), model.getSelectedIndices());
            assertEquals("e", model.getSelectedItem());
            grid.getItems().clear();
            assertTrue(model.isEmpty());
            assertEquals(-1, grid.getFocusModel().getFocusedIndex());
        });
    }

    @Test
    public void duplicateItemsRemainIndependentlySelectable() throws TimeoutException {
        FxToolkit.setupFixture(() -> {
            GridView<String> grid = new GridView<>(FXCollections.observableArrayList("same", "same", "other"));
            MultipleSelectionModel<String> model = grid.getSelectionModel();
            model.setSelectionMode(SelectionMode.MULTIPLE);
            model.selectIndices(0, 1);
            assertEquals(List.of("same", "same"), model.getSelectedItems());
            grid.getItems().remove(0);
            assertEquals(List.of(0), model.getSelectedIndices());
            assertEquals(List.of("same"), model.getSelectedItems());
        });
    }

    @Test
    public void replacingItemsClearsSelectionAndDetachesOldList() throws TimeoutException {
        FxToolkit.setupFixture(() -> {
            GridView<String> grid = strings();
            StackPane root = layout(grid);
            ObservableList<String> old = grid.getItems();
            grid.getSelectionModel().select(2);
            grid.setItems(FXCollections.observableArrayList("new", "items"));
            root.layout();
            assertTrue(grid.getSelectionModel().isEmpty());
            grid.getSelectionModel().select(1);
            old.clear();
            assertEquals("items", grid.getSelectionModel().getSelectedItem());
            grid.setItems(null);
            root.layout();
            assertTrue(grid.getSelectionModel().isEmpty());
            assertEquals(-1, grid.getFocusModel().getFocusedIndex());
            grid.setItems(FXCollections.observableArrayList("restored"));
            root.applyCss();
            root.layout();
            assertEquals("restored", cell(grid, 0).getItem());
        });
    }

    @Test
    public void mouseSupportsPlainShortcutShiftAndContextSelection() throws TimeoutException {
        FxToolkit.setupFixture(() -> {
            GridView<String> grid = strings();
            layout(grid);
            MultipleSelectionModel<String> model = grid.getSelectionModel();
            model.setSelectionMode(SelectionMode.MULTIPLE);
            click(cell(grid, 1), MouseButton.PRIMARY, false, false);
            click(cell(grid, 3), MouseButton.PRIMARY, false, true);
            assertEquals(List.of(1, 3), model.getSelectedIndices());
            click(cell(grid, 3), MouseButton.SECONDARY, false, false);
            assertEquals(List.of(1, 3), model.getSelectedIndices());
            click(cell(grid, 1), MouseButton.PRIMARY, false, true);
            assertEquals(List.of(3), model.getSelectedIndices());
            click(cell(grid, 0), MouseButton.PRIMARY, true, false);
            assertEquals(List.of(0, 1), model.getSelectedIndices());
            click(cell(grid, 2), MouseButton.SECONDARY, false, false);
            assertEquals(List.of(2), model.getSelectedIndices());
        });
    }

    @Test
    public void keyboardUsesGridColumnsAndSupportsFocusAndRanges() throws TimeoutException {
        FxToolkit.setupFixture(() -> {
            GridView<Integer> grid = numbers(200);
            StackPane root = layout(grid);
            grid.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            int columns = ((GridViewSkin<?>) grid.getSkin()).computeMaxCellsInRow();
            grid.getSelectionModel().select(0);
            key(grid, KeyCode.DOWN, false, false);
            assertEquals(columns, grid.getSelectionModel().getSelectedIndex());
            key(grid, KeyCode.RIGHT, false, true);
            assertEquals(columns, grid.getSelectionModel().getSelectedIndex());
            assertEquals(columns + 1, grid.getFocusModel().getFocusedIndex());
            key(grid, KeyCode.SPACE, false, true);
            assertEquals(List.of(columns, columns + 1), grid.getSelectionModel().getSelectedIndices());
            key(grid, KeyCode.RIGHT, true, false);
            assertEquals(List.of(columns + 1, columns + 2), grid.getSelectionModel().getSelectedIndices());
            key(grid, KeyCode.END, false, false);
            root.layout();
            assertEquals(199, grid.getSelectionModel().getSelectedIndex());
            assertTrue(cell(grid, 199).isSelected());
            key(grid, KeyCode.A, false, true);
            assertEquals(200, grid.getSelectionModel().getSelectedIndices().size());
        });
    }

    @Test
    public void recycledCellsAndReplacementModelsDoNotKeepStaleSelection() throws TimeoutException {
        FxToolkit.setupFixture(() -> {
            GridView<String> grid = strings();
            GridCell<String> cell = new GridCell<>();
            cell.updateGridView(grid);
            cell.updateIndex(1);
            MultipleSelectionModel<String> old = grid.getSelectionModel();
            old.select(1);
            assertTrue(cell.isSelected());
            cell.updateIndex(2);
            assertFalse(cell.isSelected());
            assertEquals("c", cell.getItem());
            grid.setSelectionModel(null);
            old.select(2);
            assertFalse(cell.isSelected());
            MultipleSelectionModel<String> replacement = strings().getSelectionModel();
            grid.setSelectionModel(replacement);
            replacement.select(2);
            assertTrue(cell.isSelected());
            cell.updateIndex(100);
            assertTrue(cell.isEmpty());
            assertFalse(cell.isSelected());
            cell.updateGridView(null);
            assertNull(cell.getItem());
        });
    }

    @Test
    public void virtualizationAndSkinReplacementKeepSelectionAndInputInSync() throws TimeoutException {
        FxToolkit.setupFixture(() -> {
            GridView<Integer> grid = numbers(200);
            StackPane root = layout(grid);
            grid.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            grid.getSelectionModel().select(0);
            key(grid, KeyCode.END, false, true);
            root.layout();
            assertEquals(List.of(0), grid.getSelectionModel().getSelectedIndices());
            assertFalse(cell(grid, 199).isSelected());
            key(grid, KeyCode.HOME, false, true);
            root.layout();
            assertTrue(cell(grid, 0).isSelected());
            // Control ignores assigning another skin of the same class unless
            // the old skin is cleared first.
            grid.setSkin(null);
            grid.setSkin(new GridViewSkin<>(grid));
            root.applyCss();
            root.layout();
            click(cell(grid, 1), MouseButton.PRIMARY, false, true);
            assertEquals(List.of(0, 1), grid.getSelectionModel().getSelectedIndices());
            root.resize(600, 300);
            root.layout();
            assertTrue(cell(grid, 0).isSelected());
            assertTrue(cell(grid, 1).isSelected());
        });
    }

    @Test
    public void extractorUpdatesReachSelectedItemsObservers() throws TimeoutException {
        FxToolkit.setupFixture(() -> {
            ObservableList<StringProperty> values = FXCollections.observableArrayList(
                    value -> new Observable[] {value});
            values.add(new SimpleStringProperty("before"));
            GridView<StringProperty> grid = new GridView<>(values);
            grid.getSelectionModel().select(0);
            AtomicBoolean updated = new AtomicBoolean();
            grid.getSelectionModel().getSelectedItems().addListener((ListChangeListener<StringProperty>) change -> {
                while (change.next()) updated.set(updated.get() || change.wasUpdated());
            });
            values.get(0).set("after");
            assertTrue(updated.get());
            assertEquals("after", grid.getSelectionModel().getSelectedItem().get());
            grid.getSelectionModel().clearAndSelect(-1);
            assertTrue(grid.getSelectionModel().isEmpty());
        });
    }

    private static GridView<String> strings() {
        return new GridView<>(FXCollections.observableArrayList("a", "b", "c", "d"));
    }

    private static GridView<Integer> numbers(int count) {
        return new GridView<>(FXCollections.observableArrayList(
                IntStream.range(0, count).boxed().collect(Collectors.toList())));
    }

    private static StackPane layout(GridView<?> grid) {
        StackPane root = new StackPane(grid);
        new Scene(root, 400, 300);
        root.resize(400, 300);
        root.applyCss();
        root.layout();
        return root;
    }

    private static GridCell<?> cell(GridView<?> grid, int index) {
        return grid.lookupAll(".grid-cell").stream().map(node -> (GridCell<?>) node)
                .filter(cell -> cell.getIndex() == index).findFirst()
                .orElseThrow(() -> new AssertionError("Cell not visible: " + index));
    }

    private static boolean mac() { return System.getProperty("os.name").startsWith("Mac"); }

    private static void click(Node cell, MouseButton button, boolean shift, boolean shortcut) {
        Event.fireEvent(cell, new MouseEvent(MouseEvent.MOUSE_PRESSED, 1, 1, 1, 1, button, 1,
                shift, shortcut && !mac(), false, shortcut && mac(),
                button == MouseButton.PRIMARY, false, button == MouseButton.SECONDARY,
                false, button == MouseButton.SECONDARY, true, null));
    }

    private static void key(GridView<?> grid, KeyCode code, boolean shift, boolean shortcut) {
        Event.fireEvent(grid, new KeyEvent(KeyEvent.KEY_PRESSED, "", "", code,
                shift, shortcut && !mac(), false, shortcut && mac()));
    }
}
