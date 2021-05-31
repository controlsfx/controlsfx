package org.controlsfx.control.tableview2;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.testfx.api.FxRobot;
import org.testfx.api.FxToolkit;

import java.util.Arrays;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class TableView2SelectionTest extends FxRobot {

    private static class RowItem {

        public final SimpleStringProperty[] values;

        public RowItem(int rowIndex, int columnCount) {
            values = new SimpleStringProperty[columnCount];
            for (int i = 0; i < this.values.length; i++) {
                this.values[i] = new SimpleStringProperty(rowIndex + "x" + i);
            }
        }

        @Override
        public String toString() {
            return "RowItem{" +
                    "values=" + Arrays.toString(values) +
                    '}';
        }
    }

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
        tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        tableView.getColumns()
                .setAll(IntStream.range(0, NUMBER_OF_COLUMNS)
                        .mapToObj(index -> {
                            TableColumn2<RowItem, String> column = new TableColumn2<>();
                            column.setId("column_" + index);
                            column.setText("#" + index);
                            column.setCellValueFactory(param -> param.getValue().values[index]);
                            final Callback<TableColumn<RowItem, String>, TableCell<RowItem, String>> cellFactory = column.getCellFactory();
                            column.setCellFactory(cellFactory);
                            return column;
                        })
                        .collect(Collectors.toList()));


        data = FXCollections.observableArrayList();
        tableView.setItems(data);

        FxToolkit.setupStage(stage -> {
            stage.setMaxWidth(TABLE_WIDTH);
            stage.setMaxHeight(TABLE_HEIGHT);
            stage.setScene(new Scene(tableView));
            stage.show();
            stage.toFront();
        });
    }

    private void waitUntilTableIsShown() {
        this.interact(() -> {
        });
    }

    @Test
    public void shouldNotChangeSelection_When_elementsAreAddedOnTop() {
        for (int i = 0; i < 5; i++) {
            data.add(0, new RowItem(5 - i, NUMBER_OF_COLUMNS));
        }
        waitUntilTableIsShown();

        this.interact(() -> {
            this.tableView.getSelectionModel().select(0);
        });

        this.interact(() -> {
            this.data.add(0, new RowItem(6, NUMBER_OF_COLUMNS));
        });

        RowItem actualSelectedItem = this.tableView.getSelectionModel()
                .getSelectedItem();
        RowItem expectedItem = this.data.get(1);

        assertEquals(expectedItem, actualSelectedItem);
    }

    @Test
    public void shouldRemoveItemsFromSelection_When_elementsAreRemovedFromTheUnderlyingList() {
        for (int i = 0; i < 5; i++) {
            data.add(0, new RowItem(5 - i, NUMBER_OF_COLUMNS));
        }
        waitUntilTableIsShown();

        this.interact(() -> {
            this.tableView.getSelectionModel().selectAll();
        });

        this.interact(() -> {
            this.data.remove(0, 3);
        });

        final Object[] actualSelectedItems = this.tableView.getSelectionModel()
                .getSelectedItems().toArray();
        final Object[] expectedItems = this.data.toArray();
        assertArrayEquals(expectedItems, actualSelectedItems);
    }

}
