package org.controlsfx.control.tableview2;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.input.KeyCode;
import javafx.util.Callback;
import org.controlsfx.control.action.ActionUtils;
import org.controlsfx.control.tableview2.actions.RowFixAction;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.testfx.api.FxRobot;
import org.testfx.api.FxToolkit;
import org.testfx.service.query.NodeQuery;
import org.testfx.util.WaitForAsyncUtils;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.controlsfx.control.tableview2.TableView2TestUtils.measure;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class TableView2SelectionTest extends FxRobot {

    private static class RowItem {

        public final SimpleStringProperty[] values;

        public RowItem(int rowIndex, int columnCount) {
            values = new SimpleStringProperty[columnCount];
            for (int i = 0; i < values.length; i++) {
                values[i] = new SimpleStringProperty(i + "x" + rowIndex);
            }
        }

        @Override
        public String toString() {
            return "RowItem{" + "values=" + Arrays.toString(values) + '}';
        }
    }

    // needs to be removed once https://github.com/TestFX/TestFX/issues/566 is resolved
    private static final boolean HEADLESS_TEST_FX_BUG_NOT_FIXED = Boolean.parseBoolean(System.getProperty("testfx.headless", "false"));

    private static final int TABLE_WIDTH = 800;
    private static final int TABLE_HEIGHT = 600;

    private static final int NUMBER_OF_COLUMNS = 10;

    private TableView2<RowItem> tableView;
    private ObservableList<RowItem> data;

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
        tableView.getSelectionModel()
                .setSelectionMode(SelectionMode.MULTIPLE);

        tableView.getColumns()
                .setAll(IntStream.range(0, NUMBER_OF_COLUMNS)
                        .mapToObj(index -> {
                            TableColumn2<RowItem, String> column = new TableColumn2<>();
                            column.setId("column_" + index);
                            column.setText("#" + index);
                            column.setCellValueFactory(param -> {
                                return param.getValue().values[index];
                            });
                            final Callback<TableColumn<RowItem, String>, TableCell<RowItem, String>> cellFactory = column.getCellFactory();
                            column.setCellFactory(cellFactory);
                            return column;
                        })
                        .collect(Collectors.toList()));

        tableView.setColumnFixingEnabled(true);
        tableView.setRowFixingEnabled(true);

        tableView.getSelectionModel().setCellSelectionEnabled(true);

        data = FXCollections.observableArrayList();
        tableView.setItems(data);

        tableView.getFixedColumns().add(tableView.getColumns().get(2));

        tableView.setRowHeaderVisible(true);
        tableView.setRowHeaderContextMenuFactory((i, rowItem) -> {
            return ActionUtils.createContextMenu(List.of(new RowFixAction(tableView, i)));
        });

        FxToolkit.setupStage(stage -> {
            stage.setMaxWidth(TABLE_WIDTH);
            stage.setMaxHeight(TABLE_HEIGHT);
            stage.setScene(new Scene(tableView));
            stage.show();
            stage.toFront();
        });
    }

    @After
    public void afterEach() throws TimeoutException {
        FxToolkit.cleanupStages();
        WaitForAsyncUtils.clearExceptions();
    }

    @Test
    public void shouldNotChangeSelection_When_elementsAreAddedOnTop() {
        for (int i = 0; i < 5; i++) {
            data.add(0, new RowItem(5 - i, NUMBER_OF_COLUMNS));
        }
        waitForProcessingInDisplayThread();

        interact(() -> {
            tableView.getSelectionModel().select(0);
        });

        interact(() -> {
            data.add(0, new RowItem(6, NUMBER_OF_COLUMNS));
        });

        waitForProcessingInDisplayThread();

        RowItem actualSelectedItem = tableView.getSelectionModel()
                .getSelectedItem();
        RowItem expectedItem = data.get(1);

        assertEquals(expectedItem, actualSelectedItem);
    }

    @Test
    public void shouldRemoveItemsFromSelection_When_elementsAreRemovedFromTheUnderlyingList() {
        for (int i = 0; i < 5; i++) {
            data.add(0, new RowItem(5 - i, NUMBER_OF_COLUMNS));
        }
        waitForProcessingInDisplayThread();

        interact(() -> {
            tableView.getSelectionModel().selectAll();
        });

        interact(() -> {
            data.remove(0, 3);
        });

        final Object[] actualSelectedItems = tableView.getSelectionModel()
                .getSelectedItems()
                .toArray();
        final Object[] expectedItems = data.toArray();
        assertArrayEquals(expectedItems, actualSelectedItems);
    }

    @Test
    public void shouldNotFreeze_When_selectingAllElementsInATableWithManyEntries() {
        var elementCount = 10_000;
        for (var i = 0; i < elementCount; i++) {
            data.add(0, new RowItem(elementCount - i, NUMBER_OF_COLUMNS));
        }
        waitForProcessingInDisplayThread();

        var timeTaken = measure(() -> {
            interact(() -> {
                tableView.getSelectionModel().selectAll();
            });
        });
        assertThat(timeTaken, is(lessThan(Duration.ofSeconds(9))));
    }

    @Test
    public void rowHeaderAndColumnHeaderAreHighlighted_When_selectingASingleCell() {
        var elementCount = 10_000;
        for (var i = 0; i < elementCount; i++) {
            data.add(0, new RowItem(elementCount - i, NUMBER_OF_COLUMNS));
        }
        waitForProcessingInDisplayThread();

        interact(() -> {
            selectCellWithMouse(1, 1);
        });

        var selectedColumnHeaders = findSelectedColumnHeaders();
        var selectedRowHeaders = findSelectedRowHeaders();

        assertThat(selectedColumnHeaders, hasSize(1));
        assertThat(selectedRowHeaders, hasSize(1));
    }

    @Test
    public void rowHeadersAndColumnHeadersAreHighlighted_When_multiSelectingCells() {
        var elementCount = 10_000;
        for (var i = 0; i < elementCount; i++) {
            data.add(0, new RowItem(elementCount - i, NUMBER_OF_COLUMNS));
        }
        waitForProcessingInDisplayThread();

        interact(() -> {
            if (HEADLESS_TEST_FX_BUG_NOT_FIXED) {
                var selectionModel = tableView.getSelectionModel();
                selectionModel.selectRange(1, tableView.getColumns().get(1), 2, tableView.getColumns().get(2));
            } else {
                selectCellWithMouse(1, 1);
                press(KeyCode.SHIFT);
                selectCellWithMouse(2, 2);
                release(KeyCode.SHIFT);
            }
        });

        var selectedColumnHeaders = findSelectedColumnHeaders();
        var selectedRowHeaders = findSelectedRowHeaders();

        assertThat(selectedColumnHeaders, hasSize(2));
        assertThat(selectedRowHeaders, hasSize(2));
    }

    @Test
    public void rowHeaderAndAllVisibleColumnHeaderAreHighlighted_When_selectingASingleRow() {
        var elementCount = 10_000;
        for (var i = 0; i < elementCount; i++) {
            data.add(0, new RowItem(elementCount - i, NUMBER_OF_COLUMNS));
        }
        waitForProcessingInDisplayThread();

        interact(() -> {
            selectRowHeaderWithMouse(1);
        });

        var selectedColumnHeaders = findSelectedColumnHeaders();
        var selectedRowHeaders = findSelectedRowHeaders();

        assertThat(selectedColumnHeaders, hasSize(10));
        assertThat(selectedRowHeaders, hasSize(1));
    }

    @Test
    public void rowHeadersAndAllVisibleColumnHeaderAreHighlighted_When_multiSelectingRows() {
        var elementCount = 10_000;
        for (var i = 0; i < elementCount; i++) {
            data.add(0, new RowItem(elementCount - i, NUMBER_OF_COLUMNS));
        }
        waitForProcessingInDisplayThread();

        interact(() -> {
            selectRowHeaderWithMouse(1);
            press(KeyCode.SHIFT);
            selectRowHeaderWithMouse(2);
            release(KeyCode.SHIFT);
        });

        var selectedColumnHeaders = findSelectedColumnHeaders();
        var selectedRowHeaders = findSelectedRowHeaders();

        assertThat(selectedColumnHeaders, hasSize(10));
        assertThat(selectedRowHeaders, hasSize(2));
    }

    private void waitForProcessingInDisplayThread() {
        interact(() -> {
        });
    }

    private void selectCellWithMouse(int rowIndex, int columnIndex) {
        var column = tableView.getColumns().get(columnIndex);
        Set<Node> cells = tableView.getSkin().getNode().lookupAll(".table-row-cell > .tableview2-cell");
        var cellNode = cells.stream()
                .filter(node -> {
                    return node instanceof TableCell;
                })
                .map(TableCell.class::cast)
                .filter(cell -> {
                    return cell.getIndex() == rowIndex && cell.getTableColumn().equals(column);
                })
                .findFirst()
                .orElse(null);
        clickOn(cellNode);
    }

    private void selectRowHeaderWithMouse(int rowIndex) {
        Node rowHeader = findAllRowHeaderCells().match(node -> {
            final boolean isCell = node instanceof TableCell;
            if (isCell) {
                var cell = (TableCell) node;
                return cell.getIndex() == rowIndex;
            }
            return false;
        }).query();
        clickOn(rowHeader);
    }

    private Set<Node> findSelectedColumnHeaders() {
        return lookup(".table-view2 .column-header.south-header.selected").queryAll();
    }

    private Set<Node> findSelectedRowHeaders() {
        return findAllRowHeaderCells()
                .match(node -> {
                    return node.getParent().getPseudoClassStates().contains(PseudoClass.getPseudoClass("selected"));
                })
                .queryAll();
    }

    private NodeQuery findAllRowHeaderCells() {
        return lookup(".row-header > .table-view2 > .virtual-flow > .clipped-container > .sheet > .table-row-cell > .tableview2-cell");
    }

}