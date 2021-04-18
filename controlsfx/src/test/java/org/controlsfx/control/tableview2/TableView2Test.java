package org.controlsfx.control.tableview2;

import impl.org.controlsfx.tableview2.TableRow2;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.testfx.api.FxRobot;
import org.testfx.api.FxToolkit;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static junit.framework.TestCase.assertNotNull;

public class TableView2Test extends FxRobot {

    private static class Row {
        public final SimpleStringProperty[] values;

        public Row(int rowIndex, int columnCount) {
            values = new SimpleStringProperty[columnCount];
            for (int i = 0; i < this.values.length; i++) {
                this.values[i] = new SimpleStringProperty(rowIndex + "x" + i);
            }
        }
    }


    private static final int THREADS = 4;
    private static final int NUMBER_OF_ROWS = 100;
    private static final int NUMBER_OF_COLUMNS = 500;

    private Stage stage;

    private TableView2<Row> tableView;
    private AtomicBoolean allowedToRun;
    private ExecutorService dataManipulators;
    private CountDownLatch dataManipulationCountdown;

    @Rule
    public Timeout globalTimeout = new Timeout(20, TimeUnit.SECONDS);

    @Before
    public void beforeEach() throws TimeoutException {
        stage = FxToolkit.registerPrimaryStage();
        stage.setMaxHeight(600);
        stage.setMaxWidth(800);
        tableView = new TableView2<>();
        dataManipulators = Executors.newFixedThreadPool(THREADS);

        populateTable();
        setupManipulators();

        this.interact(() -> {
            stage.setScene(new Scene(tableView));
            stage.show();
            stage.toFront();
        });
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
                    Row row = tableView.getItems().get(random.nextInt(NUMBER_OF_ROWS));
                    row.values[random.nextInt(row.values.length)].set((100 + random.nextInt(100)) + "");
                }
            });
        }
    }

    private void populateTable() {
        tableView.rowFactoryProperty()
                .unbind();
        tableView.setRowFactory(param -> new TableRow2<>(tableView));
        tableView.getItems()
                .setAll(IntStream.range(0, NUMBER_OF_ROWS)
                        .mapToObj((int rowIndex) -> new Row(rowIndex, NUMBER_OF_COLUMNS))
                        .collect(Collectors.toList()));
        List<TableColumn<Row, String>> fixed = new ArrayList<>();
        tableView.getColumns()
                .setAll(IntStream.range(0, NUMBER_OF_COLUMNS)
                        .mapToObj(index -> {
                            TableColumn<Row, String> column = new TableColumn<>();
                            column.setId("column_" + index);
                            column.setText("#" + index);
                            column.setCellValueFactory(param -> param.getValue().values[index]);
                            if (index < 3) {
                                fixed.add(column);
                            }
                            return column;
                        })
                        .collect(Collectors.toList()));
        tableView.getFixedColumns()
                .setAll(fixed);
    }

    @After
    public void afterEach() throws TimeoutException {
        allowedToRun.set(false);
        dataManipulators.shutdownNow();
        FxToolkit.cleanupStages();
    }

    private void produceUpdateLoad() {
        dataManipulationCountdown.countDown();
    }

    private void changeSelection() {
        final TableView.TableViewSelectionModel<Row> sm = tableView.getSelectionModel();
        final TableColumn<Row, ?> column = tableView.getColumns().get(0);
        // back and forth
        final int times = 20;
        Phaser phaser = new Phaser();
        phaser.bulkRegister(times + 1);
        for (int i = 0; i < times; i++) {
            final int selectedIndex = i % 2;
            this.interact(() -> {
                sm.clearAndSelect(selectedIndex, column);
                phaser.arriveAndDeregister();
            });
        }
        phaser.arriveAndAwaitAdvance();
    }

    @Test
    public void shouldNotFreeze_When_SelectionIsChangedUnderLoad() {
        produceUpdateLoad();

        changeSelection();

        assertNotNull(tableView.getSelectionModel()
                .getSelectedItem());
    }
}
