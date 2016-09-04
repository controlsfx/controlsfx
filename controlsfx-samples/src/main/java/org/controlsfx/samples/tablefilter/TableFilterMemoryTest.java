package org.controlsfx.samples.tablefilter;

import javafx.application.Application;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.controlsfx.control.table.TableFilter;

import java.util.Random;

/**
 * This test class helps investigate memory usage of TableFilters while created multiple times for the same table.
 * Inspection of memory is not part of the class and should be done externally.
 */
public class TableFilterMemoryTest extends Application {

    static final int ColCount = 9;
    static final Random rng = new Random(0);
    private final BorderPane root = new BorderPane();
    private final TableView<Row> table = new TableView<>();

    private int rangeStart = 1;

    public TableFilterMemoryTest() {
        root.setCenter(table);

        Button btn = new Button("Reset");
        btn.setOnAction(e -> resetTable());

        root.setTop(btn);

        // Add columns
        char letter = 'A';
        for (int i = 0; i < 9; i++) {
            TableColumn<Row, String> tc = new TableColumn<>("Column " + letter);
            int index = i;
            tc.setCellValueFactory(cdf -> new ReadOnlyStringWrapper(cdf.getValue().data[index]));
            table.getColumns().add(tc);
            letter++;
        }
    }

    private void resetTable() {
        table.setItems(null);

        // Suggest to perform garbage collection
        System.gc();

        // Add new data
        ObservableList<Row> ans = FXCollections.observableArrayList();
        for (int i = 0; i < 1024; i++) {
            ans.add(new Row());
        }
        table.setItems(ans);

        // Reset the filter
        TableFilter.forTableView(table).lazy(false).apply();

        rangeStart += 10;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Scene scene = new Scene(root, 800, 600);
        primaryStage.setTitle("TableFilter");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    class Row {
        public final String[] data;

        public Row() {
            data = new String[ColCount];
            for (int i = 0; i < ColCount; i++) {
                data[i] = String.format("%d", rangeStart + rng.nextInt(10));
            }
        }
    }
}