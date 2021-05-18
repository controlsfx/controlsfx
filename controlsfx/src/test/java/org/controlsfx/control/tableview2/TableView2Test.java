package org.controlsfx.control.tableview2;

import impl.org.controlsfx.tableview2.TableRow2;
import impl.org.controlsfx.tableview2.TableRow2Skin;
import impl.org.controlsfx.tableview2.TableView2Skin;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.AccessibleAttribute;
import javafx.scene.Node;
import javafx.scene.Scene;
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
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.lessThan;

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
