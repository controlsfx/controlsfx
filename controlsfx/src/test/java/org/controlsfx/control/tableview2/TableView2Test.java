/**
 * Copyright (c) 2021, 2026, ControlsFX
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
package org.controlsfx.control.tableview2;

import impl.org.controlsfx.tableview2.SouthTableColumnHeader;
import impl.org.controlsfx.tableview2.SouthTableHeaderRow;
import impl.org.controlsfx.tableview2.TableRow2;
import impl.org.controlsfx.tableview2.TableRow2Skin;
import impl.org.controlsfx.tableview2.TableView2Skin;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.stage.Screen;
import javafx.collections.ObservableList;
import javafx.scene.AccessibleAttribute;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.util.Callback;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.testfx.api.FxRobot;
import org.testfx.api.FxToolkit;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

public class TableView2Test extends FxRobot {

    private static class RowItem {

        public final SimpleStringProperty[] values;

        public RowItem(int rowIndex, int columnCount) {
            values = new SimpleStringProperty[columnCount];
            for (int i = 0; i < this.values.length; i++) {
                this.values[i] = new SimpleStringProperty(rowIndex + "x" + i);
            }
        }
    }

    private static final int TABLE_WIDTH = 800;
    private static final int TABLE_HEIGHT = 600;

    private static final int THREADS = 4;
    private static final int NUMBER_OF_ROWS = 100;
    private static final int NUMBER_OF_COLUMNS = 500;
    private static final int NUMBER_OF_SELECTION_CHANGES = 20;
    private static final Duration MAX_DURATION_FOR_SELECTION_CHANGES = Duration.ofSeconds(NUMBER_OF_SELECTION_CHANGES);

    private TableView2<RowItem> tableView;
    private ObservableList<RowItem> data;
    private AtomicBoolean allowedToRun;
    private ExecutorService dataManipulators;
    private CountDownLatch dataManipulationCountdown;
    private AtomicLong numberRowChildrenModifications;

    @BeforeClass
    public static void beforeAll() throws TimeoutException {
        FxToolkit.registerPrimaryStage();
    }

    @Before
    public void beforeEach() throws TimeoutException {
        tableView = new TableView2<>();
        tableView.setMaxWidth(TABLE_WIDTH);
        tableView.setMaxHeight(TABLE_HEIGHT);
        tableView.setPrefSize(TABLE_WIDTH, TABLE_HEIGHT);
        dataManipulators = Executors.newFixedThreadPool(THREADS);

        populateTable();
        setupManipulators();

        FxToolkit.setupStage(stage -> {
            stage.setMaxWidth(TABLE_WIDTH);
            stage.setMaxHeight(TABLE_HEIGHT);
            stage.setScene(new Scene(tableView));
            stage.show();
            stage.toFront();
        });
    }

    private void populateTable() {
        tableView.skinProperty()
                .addListener(e -> {
                    tableView.rowFactoryProperty()
                            .unbind();
                    tableView.setRowFactory(param -> {
                        final TableRow2<RowItem> rowTableRow2 = new TableRow2<>(tableView);
                        rowTableRow2.skinProperty()
                                .addListener(observable -> {
                                    final TableRow2Skin<?> skin = (TableRow2Skin<?>) rowTableRow2.getSkin();
                                    skin.getChildren()
                                            .addListener((ListChangeListener<? super Node>) c -> numberRowChildrenModifications.incrementAndGet());
                                });
                        return rowTableRow2;
                    });
                });
        List<TableColumn2<RowItem, String>> fixed = new ArrayList<>();
        tableView.getColumns()
                .setAll(IntStream.range(0, NUMBER_OF_COLUMNS)
                        .mapToObj(index -> {
                            TableColumn2<RowItem, String> column = new TableColumn2<>();
                            column.setId("column_" + index);
                            column.setText("#" + index);
                            column.setCellValueFactory(param -> param.getValue().values[index]);
                            if (index < 3) {
                                fixed.add(column);
                            }
                            final Callback<TableColumn<RowItem, String>, TableCell<RowItem, String>> cellFactory = column.getCellFactory();
                            column.setCellFactory(cellFactory);
                            return column;
                        })
                        .collect(Collectors.toList()));
        tableView.getFixedColumns()
                .setAll(fixed);
        data = FXCollections.observableArrayList();
        tableView.setItems(data);
        numberRowChildrenModifications = new AtomicLong();
    }

    private void fillTableData() {
        data.setAll(IntStream.range(0, NUMBER_OF_ROWS)
                .mapToObj((int rowIndex) -> new RowItem(rowIndex, NUMBER_OF_COLUMNS))
                .collect(Collectors.toList()));
    }

    private void setupManipulators() {
        dataManipulationCountdown = new CountDownLatch(1);
        allowedToRun = new AtomicBoolean(true);
        for (int i = 0; i < THREADS; i++) {
            this.dataManipulators.submit(() -> {
                Random random = new SecureRandom();

                try {
                    dataManipulationCountdown.await();
                } catch (InterruptedException e) {
                    // return silently
                    return;
                }
                while (allowedToRun.get()) {
                    RowItem rowItem = tableView.getItems()
                            .get(random.nextInt(NUMBER_OF_ROWS));
                    rowItem.values[random.nextInt(rowItem.values.length)].set((100 + random.nextInt(100)) + "");
                }
            });
        }
    }

    @After
    public void afterEach() throws TimeoutException {
        allowedToRun.set(false);
        dataManipulators.shutdownNow();
        FxToolkit.cleanupStages();
    }

    @Test
    public void shouldNotFreeze_When_SelectionIsChangedUnderLoad() {
        fillTableData();
        produceUpdateLoad();

        Duration timeTaken = measure(this::changeSelection);
        assertThat(timeTaken, is(lessThan(MAX_DURATION_FOR_SELECTION_CHANGES)));
    }

    @Test
    public void shouldNotFreeze_When_ScrollingVerticallyUnderLoad() {
        fillTableData();
        produceUpdateLoad();

        Duration timeTaken = measure(this::scrollDownAndUp);
        assertThat(timeTaken, is(lessThan(MAX_DURATION_FOR_SELECTION_CHANGES)));
    }

    @Test
    public void shouldReuseItems_When_ScrollingVertically() {
        fillTableData();
        measure(() -> scrollDown(60));
        numberRowChildrenModifications.set(0);
        measure(() -> scrollUp(60));
        assertThat(numberRowChildrenModifications.get(), is(0L));
    }

    @Test
    public void shouldHaveNoArtifacts_When_ScrollingVertically() {
        fillTableData();
        measure(() -> scrollDown(40));
        TableView2Skin<?> skin = (TableView2Skin<?>) tableView.getSkin();
        TableRow2<?> firstVisibleRow = (TableRow2<?>) skin.getRow(0);
        assertThat(firstVisibleRow, is(inSyncWithTable(tableView)));
        measure(() -> scrollUp(40));
        assertThat(firstVisibleRow, is(inSyncWithTable(tableView)));
    }

    @Test
    public void shouldReuseSouthHeader_When_ColumnIsAdded() {
        fillTableData();

        final TableColumn2<RowItem, String> firstColumn = (TableColumn2<RowItem, String>) tableView.getColumns().get(0);
        final AtomicReference<SouthTableColumnHeader> firstHeader = new AtomicReference<>();

        interact(() -> firstHeader.set(getSouthHeaderRow().getSouthColumnHeaderFor(firstColumn)));
        assertNotNull(firstHeader.get());

        interact(() -> tableView.getColumns().add(createExtraColumn("column_extra")));

        final AtomicReference<SouthTableColumnHeader> afterHeader = new AtomicReference<>();
        interact(() -> afterHeader.set(getSouthHeaderRow().getSouthColumnHeaderFor(firstColumn)));
        assertSame(firstHeader.get(), afterHeader.get());
    }

    @Test
    public void shouldRemoveSouthHeader_When_ColumnIsRemoved() {
        fillTableData();

        final TableColumn2<RowItem, String> secondColumn = (TableColumn2<RowItem, String>) tableView.getColumns().get(1);
        final AtomicReference<SouthTableColumnHeader> secondHeader = new AtomicReference<>();

        interact(() -> secondHeader.set(getSouthHeaderRow().getSouthColumnHeaderFor(secondColumn)));
        assertNotNull(secondHeader.get());

        interact(() -> tableView.getColumns().remove(secondColumn));

        final AtomicReference<SouthTableColumnHeader> headerAfterRemovalHolder = new AtomicReference<>();
        final AtomicBoolean stillInList = new AtomicBoolean();
        interact(() -> {
            SouthTableHeaderRow southHeaderRow = getSouthHeaderRow();
            headerAfterRemovalHolder.set(southHeaderRow.getSouthColumnHeaderFor(secondColumn));
            stillInList.set(southHeaderRow.getSouthColumnHeaders().contains(secondHeader.get()));
        });
        assertNull(headerAfterRemovalHolder.get());
        assertFalse(stillInList.get());
    }

    /**
     * Reorders columns and verifies existing headers for unaffected/persistent columns are reused
     * Confirms south header count stays aligned with visible leaf columns
     */
    @Test
    public void shouldReuseSouthHeaders_When_ColumnsAreReordered() {
        fillTableData();

        final TableColumn2<RowItem, String> tenthColumn = (TableColumn2<RowItem, String>) tableView.getColumns().get(10);
        final TableColumn2<RowItem, String> unaffectedColumn = (TableColumn2<RowItem, String>) tableView.getColumns().get(50);
        final AtomicReference<SouthTableColumnHeader> tenthHeader = new AtomicReference<>();
        final AtomicReference<SouthTableColumnHeader> unaffectedHeader = new AtomicReference<>();

        interact(() -> {
            SouthTableHeaderRow southHeaderRow = getSouthHeaderRow();
            tenthHeader.set(southHeaderRow.getSouthColumnHeaderFor(tenthColumn));
            unaffectedHeader.set(southHeaderRow.getSouthColumnHeaderFor(unaffectedColumn));
        });
        assertNotNull(tenthHeader.get());
        assertNotNull(unaffectedHeader.get());

        interact(() -> {
            // swap columns 10 and 11
            TableColumn<RowItem, ?> moved = tableView.getColumns().remove(11);
            tableView.getColumns().add(10, moved);
        });

        final AtomicReference<SouthTableColumnHeader> tenthHeaderAfterHolder = new AtomicReference<>();
        final AtomicReference<SouthTableColumnHeader> unaffectedHeaderAfterHolder = new AtomicReference<>();
        final AtomicInteger southHeaderCount = new AtomicInteger();
        final AtomicInteger visibleLeafCount  = new AtomicInteger();
        interact(() -> {
            SouthTableHeaderRow southHeaderRow = getSouthHeaderRow();
            tenthHeaderAfterHolder.set(southHeaderRow.getSouthColumnHeaderFor(tenthColumn));
            unaffectedHeaderAfterHolder.set(southHeaderRow.getSouthColumnHeaderFor(unaffectedColumn));
            southHeaderCount.set(southHeaderRow.getSouthColumnHeaders().size());
            visibleLeafCount.set(tableView.getVisibleLeafColumns().size());
        });
        assertSame(tenthHeader.get(), tenthHeaderAfterHolder.get());
        assertSame(unaffectedHeader.get(), unaffectedHeaderAfterHolder.get());
        assertEquals(southHeaderCount.get(), visibleLeafCount.get());
    }

    /**
     * Asserts header is removed when column becomes invisible and recreated when visible again
     */
    @Test
    public void shouldRecreateSouthHeader_When_ColumnVisibilityIsToggled() {
        fillTableData();

        final TableColumn2<RowItem, String> toggledColumn = (TableColumn2<RowItem, String>) tableView.getColumns().get(15);
        final AtomicReference<SouthTableColumnHeader> originalHeader = new AtomicReference<>();

        interact(() -> originalHeader.set(getSouthHeaderRow().getSouthColumnHeaderFor(toggledColumn)));
        assertNotNull(originalHeader.get());

        interact(() -> toggledColumn.setVisible(false));

        final AtomicReference<SouthTableColumnHeader> headerAfterHideHolder = new AtomicReference<>();
        final AtomicBoolean stillInListAfterHide = new AtomicBoolean();
        interact(() -> {
            SouthTableHeaderRow southHeaderRow = getSouthHeaderRow();
            headerAfterHideHolder.set(southHeaderRow.getSouthColumnHeaderFor(toggledColumn));
            stillInListAfterHide.set(southHeaderRow.getSouthColumnHeaders().contains(originalHeader.get()));
        });
        assertNull(headerAfterHideHolder.get());
        assertFalse(stillInListAfterHide.get());

        interact(() -> toggledColumn.setVisible(true));

        final AtomicReference<SouthTableColumnHeader> recreatedHeader = new AtomicReference<>();
        interact(() -> recreatedHeader.set(getSouthHeaderRow().getSouthColumnHeaderFor(toggledColumn)));
        assertNotNull(recreatedHeader.get());
        assertNotSame(originalHeader.get(), recreatedHeader.get());
    }

    /**
     * Verifies SouthTableColumnHeader keeps exactly one child when southNode is replaced
     */
    @Test
    public void shouldKeepSingleSouthNodeChild_When_SouthNodeChanges() {
        fillTableData();

        final TableColumn2<RowItem, String> column = (TableColumn2<RowItem, String>) tableView.getColumns().get(20);
        final Label firstNode = new Label("first");
        final Label secondNode = new Label("second");

        interact(() -> column.setSouthNode(firstNode));

        final AtomicInteger childCount = new AtomicInteger();
        final AtomicReference<Node> childNode = new AtomicReference<>();
        interact(() -> {
            SouthTableColumnHeader header = getSouthHeaderRow().getSouthColumnHeaderFor(column);
            childCount.set(header != null ? header.getChildrenUnmodifiable().size() : -1);
            childNode.set((header != null && !header.getChildrenUnmodifiable().isEmpty())
                    ? header.getChildrenUnmodifiable().get(0) : null);
        });
        assertEquals(1, childCount.get());
        assertSame(firstNode, childNode.get());

        interact(() -> column.setSouthNode(secondNode));

        interact(() -> {
            SouthTableColumnHeader header = getSouthHeaderRow().getSouthColumnHeaderFor(column);
            childCount.set(header != null ? header.getChildrenUnmodifiable().size() : -1);
            childNode.set((header != null && !header.getChildrenUnmodifiable().isEmpty())
                    ? header.getChildrenUnmodifiable().get(0) : null);
        });
        assertEquals(1, childCount.get());
        assertSame(secondNode, childNode.get());
    }

    /**
     * Add 50 columns one by one, to check size remains consistent, added columns have a south header, previous columns
     * keep the same south header instance.
     * Remove 50 columns in reverse, one by one, to check size remains consistent, removed columns don't have a south header,
     * previous columns keep the same south header instance.
     */
    @Test
    public void shouldKeepSouthHeaderCountConsistent_When_ManyColumnsAreAddedAndRemoved() {
        fillTableData();

        final int iterations = 50;
        final List<TableColumn2<RowItem, String>> addedColumns = new ArrayList<>();
        final TableColumn2<RowItem, String> stableColumn = (TableColumn2<RowItem, String>) tableView.getColumns().get(0);
        final AtomicReference<SouthTableColumnHeader> stableHeader = new AtomicReference<>();

        interact(() -> stableHeader.set(getSouthHeaderRow().getSouthColumnHeaderFor(stableColumn)));
        assertNotNull(stableHeader.get());

        final AtomicInteger southHeaderCount = new AtomicInteger();
        final AtomicInteger visibleLeafCount =  new AtomicInteger();
        final AtomicReference<SouthTableColumnHeader> addedHeader = new AtomicReference<>();
        final AtomicReference<SouthTableColumnHeader> stableHeaderAfter = new AtomicReference<>();

        for (int i = 0; i < iterations; i++) {
            final TableColumn2<RowItem, String> added = createExtraColumn("column_stress_" + i);
            addedColumns.add(added);

            interact(() -> tableView.getColumns().add(added));
            interact(() -> {
                SouthTableHeaderRow southHeaderRow = getSouthHeaderRow();
                southHeaderCount.set(southHeaderRow.getSouthColumnHeaders().size());
                visibleLeafCount.set(tableView.getVisibleLeafColumns().size());
                addedHeader.set(southHeaderRow.getSouthColumnHeaderFor(added));
                stableHeaderAfter.set(southHeaderRow.getSouthColumnHeaderFor(stableColumn));
            });
            assertEquals("south header count after add", visibleLeafCount.get(), southHeaderCount.get());
            assertNotNull("added column should have a south header", addedHeader.get());
            assertSame("stable column header should be reused after add", stableHeader.get(), stableHeaderAfter.get());
        }

        final AtomicReference<SouthTableColumnHeader> removedHeader = new AtomicReference<>();

        for (int i = addedColumns.size() - 1; i >= 0; i--) {
            final TableColumn2<RowItem, String> removed = addedColumns.get(i);

            interact(() -> tableView.getColumns().remove(removed));
            interact(() -> {
                SouthTableHeaderRow southHeaderRow = getSouthHeaderRow();
                southHeaderCount.set(southHeaderRow.getSouthColumnHeaders().size());
                visibleLeafCount.set(tableView.getVisibleLeafColumns().size());
                removedHeader.set(southHeaderRow.getSouthColumnHeaderFor(removed));
                stableHeaderAfter.set(southHeaderRow.getSouthColumnHeaderFor(stableColumn));
            });
            assertEquals("south header count after remove", visibleLeafCount.get(), southHeaderCount.get());
            assertNull("removed column should have no south header", removedHeader.get());
            assertSame("stable column header should be reused after remove", stableHeader.get(), stableHeaderAfter.get());
        }
    }

    /**
     * Verifies that computePrefWidth sums snapped column widths, consistent with how
     * layoutChildren accumulates x positions. Without the fix (issue #1576), the raw
     * (unsnapped) sum was used, causing a mismatch with the snapped layout positions.
     */
    @Test
    public void prefWidthShouldMatchSnappedColumnWidthSum_When_ColumnsHaveFractionalWidth() {
        fillTableData();

        final double fractionalWidth = 75.3;
        interact(() -> tableView.getVisibleLeafColumns()
                .forEach(col -> col.setPrefWidth(fractionalWidth)));

        final AtomicReference<Double> actualPrefWidthHolder = new AtomicReference<>();
        final AtomicReference<Double> expectedPrefWidthHolder = new AtomicReference<>();

        interact(() -> {
            TableView2Skin<?> skin = (TableView2Skin<?>) tableView.getSkin();
            TableRow2<?> row = (TableRow2<?>) skin.getRow(0);
            actualPrefWidthHolder.set(row.prefWidth(-1));

            // Replicate snapSizeX: rounds up to the nearest physical pixel
            double scale = Screen.getPrimary().getOutputScaleX();
            double snappedColWidth = Math.ceil(fractionalWidth * scale) / scale;
            expectedPrefWidthHolder.set(tableView.getVisibleLeafColumns().size() * snappedColWidth);
        });

        assertEquals(expectedPrefWidthHolder.get(), actualPrefWidthHolder.get(), 0.001);
    }

    private Duration measure(Runnable operation) {
        LocalTime start = LocalTime.now();
        operation.run();
        LocalTime end = LocalTime.now();
        return Duration.between(start, end);
    }

    private void produceUpdateLoad() {
        dataManipulationCountdown.countDown();
    }

    private void changeSelection() {
        final TableView.TableViewSelectionModel<RowItem> sm = tableView.getSelectionModel();
        final TableColumn<RowItem, ?> column = tableView.getColumns()
                .get(0);
        // back and forth
        for (int i = 0; i < NUMBER_OF_SELECTION_CHANGES; i++) {
            final int selectedIndex = i % 2;
            interact(() -> sm.clearAndSelect(selectedIndex, column));
        }
    }

    private void scrollDownAndUp() {
        this.scrollDown(NUMBER_OF_SELECTION_CHANGES);
        this.scrollUp(NUMBER_OF_SELECTION_CHANGES);
    }

    private void scrollDown(int times) {
        ScrollBar scrollBar = (ScrollBar) tableView.queryAccessibleAttribute(AccessibleAttribute.VERTICAL_SCROLLBAR);
        for (int i = 0; i < times; i++) {
            final double value = i * 0.01;
            interact(() -> scrollBar.setValue(value));
        }

    }

    private void scrollUp(int times) {
        ScrollBar scrollBar = (ScrollBar) tableView.queryAccessibleAttribute(AccessibleAttribute.VERTICAL_SCROLLBAR);
        for (int i = times; i >= 0; i--) {
            final double value = i * 0.01;
            interact(() -> scrollBar.setValue(value));
        }
    }

    private SouthTableHeaderRow getSouthHeaderRow() {
        return tableView.lookupAll(".south-header")
                .stream()
                .filter(SouthTableHeaderRow.class::isInstance)
                .map(SouthTableHeaderRow.class::cast)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Expected SouthTableHeaderRow in lookupAll('.south-header')"));
    }

    private TableColumn2<RowItem, String> createExtraColumn(String id) {
        TableColumn2<RowItem, String> column = new TableColumn2<>();
        column.setId(id);
        column.setText(id);
        column.setCellValueFactory(param -> new SimpleStringProperty("extra"));
        return column;
    }

    private static Matcher<TableRow2<?>> inSyncWithTable(TableView2<RowItem> tableView) {
        return new BaseMatcher<>() {

            private int firstMismatch = -1;
            private Object expected = null;
            private Object actual = null;

            @Override
            public void describeTo(Description description) {
                description.appendText("that cell at index " + firstMismatch + " contains ");
                description.appendValue(expected);
            }

            @Override
            public void describeMismatch(Object item, Description description) {
                description.appendText("cell at index " + firstMismatch + " contains ");
                description.appendValue(actual);
            }

            @Override
            public boolean matches(Object unknown) {
                TableRow2<?> tableRow = (TableRow2<?>) unknown;
                RowItem item = tableView.getItems()
                        .get(tableRow.getIndex());
                List<Node> children = new ArrayList<>(tableRow.getChildrenUnmodifiable());
                for (Node child : children) {
                    if (child instanceof TableCell) {
                        TableCell<?, ?> cell = (TableCell<?, ?>) child;
                        int index = Integer.parseInt(cell.getTableColumn()
                                .getId()
                                .split("_")[1]);
                        actual = cell.getText();
                        expected = item.values[index].get();
                        if (!Objects.equals(actual, expected)) {
                            firstMismatch = index;
                            return false;
                        }
                    }
                }
                return true;
            }
        };
    }
}
