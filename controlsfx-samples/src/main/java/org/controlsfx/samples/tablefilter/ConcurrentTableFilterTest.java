package org.controlsfx.samples.tablefilter;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;
import org.controlsfx.control.table.TableFilter;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

public final class ConcurrentTableFilterTest extends Application {

    private static final ExecutorService exec = Executors.newFixedThreadPool(5);

    @Override
    public void start(Stage primaryStage) throws Exception {

        TableView<DataItem> tableView = new TableView<>();

        tableView.setItems(FXCollections.observableArrayList());
        IntStream.range(0,500).mapToObj(i -> new DataItem()).forEach(d -> tableView.getItems().add(d));

        TableColumn<DataItem,Integer> smallInt = new TableColumn<>("Small Int");
        smallInt.setCellValueFactory(cb -> new ReadOnlyObjectWrapper<>(cb.getValue().getSmallIntValue()));

        TableColumn<DataItem,Integer> largeInt = new TableColumn<>("Large Int");
        largeInt.setCellValueFactory(cb -> new ReadOnlyObjectWrapper<>(cb.getValue().getLargeIntValue()));

        TableColumn<DataItem,String> randomLetter = new TableColumn<>("Letter");
        randomLetter.setCellValueFactory(cb -> new ReadOnlyObjectWrapper<>(cb.getValue().getRandomLetter()));

        TableColumn<DataItem,Number> concurrentNumber = new TableColumn<>("Concurrent Number");
        concurrentNumber.setCellValueFactory(cb ->
            cb.getValue().getConcurrentNumber()
        );

        tableView.getColumns().addAll(smallInt, largeInt, randomLetter, concurrentNumber);

        Platform.runLater(() -> new TableFilter<>(tableView));

        GridPane grp = new GridPane();

        GridPane.setFillHeight(tableView, true);
        GridPane.setFillWidth(tableView, true);
        GridPane.setHgrow(tableView, Priority.ALWAYS);
        GridPane.setVgrow(tableView, Priority.ALWAYS);
        grp.getChildren().add(tableView);

        Scene scene = new Scene(grp);

        primaryStage.setScene(scene);

        primaryStage.show();

    }

    @Override
    public void stop() throws Exception {
        exec.shutdown();
    }

    private static final class DataItem {

        private final int smallIntValue = new Random().nextInt(100);
        private final int largeIntValue = new Random().nextInt(10000);
        private final String randomLetter = String.valueOf((char) (new Random().nextInt(26) + 'a'));

        private final Property<Number> concurrentNumber = new SimpleIntegerProperty();

        private DataItem() {
            exec.execute(() -> {
                try {
                    Thread.sleep(1000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Platform.runLater(() -> {
                    concurrentNumber.setValue(new Random().nextInt(10000));
                });
            });
        }
        public int getLargeIntValue() {
            return largeIntValue;
        }
        public int getSmallIntValue() {
            return smallIntValue;
        }
        public String getRandomLetter() {
            return randomLetter;
        }
        public Property<Number> getConcurrentNumber() {
            return concurrentNumber;
        }


    }

    public static void main(String[] args) {
        Application.launch(args);
    }

}
