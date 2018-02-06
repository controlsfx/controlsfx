package org.controlsfx.samples.tablefilter;


import javafx.application.Application;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.controlsfx.control.table.TableFilter;

import java.time.LocalDate;

public final class FlightTable extends Application {

    private final ObservableList<Flight> flights = FXCollections.observableArrayList(
            new Flight(567,"ABQ","DAL", LocalDate.of(2015,5,8), 642,"23"),
            new Flight(234,"ABQ","DAL", LocalDate.of(2015,5,9), 642, "13"),
            new Flight(756,"ABQ","DAL", LocalDate.of(2015,5,11), 642, "9"),
            new Flight(268,"ABQ","DAL", LocalDate.of(2015,5,13), 642, "2"),

            new Flight(567,"DAL","HOU", LocalDate.of(2015,5,8), 244, "A5"),
            new Flight(239,"DAL","HOU", LocalDate.of(2015,5,14), 244, "B4"),
            new Flight(5923,"DAL","HOU", LocalDate.of(2015,5,17), 244, "C3"),
            new Flight(2389,"DAL","HOU", LocalDate.of(2015,5,6), 244, null),

            new Flight(287,"SEA","PHX", LocalDate.of(2015,5,8), 1100, null),
            new Flight(875,"SEA","PHX", LocalDate.of(2015,5,16), 1100,  "12"),
            new Flight(4288,"SEA","PHX", LocalDate.of(2015,5,9), 1100,  "19")
    );

    private final ObservableList<Flight> hiddenFlights = FXCollections.observableArrayList(
            new Flight(567,"BWI","MCO", LocalDate.of(2015,7,9), 898, "45"),
            new Flight(234,"MDW","PDX", LocalDate.of(2015,7,12), 2118, "B9"),
            new Flight(411,"SAN","JFK", LocalDate.of(2015,7,19), 2077, null)
    );

    private final TableView<Flight> table = new TableView<>();

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("Flight Table");
        BorderPane borderPane = new BorderPane();
        Scene scene = new Scene(borderPane, 800, 600);

        SortedList<Flight> sortedList = new SortedList<>(flights);
        table.setItems(sortedList);
        sortedList.comparatorProperty().bind(table.comparatorProperty());


        TableFilter<Flight> tableFilter = TableFilter.forTableView(table).lazy(true).apply();

        tableFilter.setSearchStrategy((input,target) -> {
            try {
                return target.matches(input);
            } catch (Exception e) {
                return false;
            }
        });

        table.setEditable(true);
        TableColumn<Flight, Integer> flightNumCol = new TableColumn<>("FLIGHT NUM");
        flightNumCol.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getFlightNumber()));
        table.getColumns().add(flightNumCol);

        TableColumn itinerary = new TableColumn("ITINERARY");

        TableColumn<Flight,String> origCol = new TableColumn<>("ORIG");
        origCol.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getOrig()));

        TableColumn<Flight,String> destCol = new TableColumn<>("DEST");
        destCol.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getDest()));

        itinerary.getColumns().addAll(origCol,destCol);
        table.getColumns().add(itinerary);

        TableColumn<Flight,LocalDate> depDateCol = new TableColumn<>("DEP DATE");
        depDateCol.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getDepartureDate()));
        table.getColumns().add(depDateCol);

        TableColumn<Flight,Integer> mileage =  new TableColumn<>("MILEAGE");
        mileage.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getMileaage()));
        table.getColumns().add(mileage);

        TableColumn<Flight,Boolean> cancelledInd =  new TableColumn<>("CANCELLED");
        cancelledInd.setCellValueFactory(cellData -> cellData.getValue().getCancelledProperty());
        cancelledInd.setEditable(true);
        table.getColumns().add(cancelledInd);

        TableColumn<Flight,String> gateNumber = new TableColumn<>("GATE NO.");
        gateNumber.setCellValueFactory(cellData -> cellData.getValue().getGateNumber());
        gateNumber.setCellFactory(TextFieldTableCell.forTableColumn());
        gateNumber.setEditable(true);
        table.getColumns().add(gateNumber);


        tableFilter.unSelectAllValues(origCol);
        tableFilter.selectValue(origCol,"ABQ");
        tableFilter.executeFilter();

        borderPane.setCenter(table);

        Button addFlightButton = new Button("APPEND");
        addFlightButton.setOnAction(e -> {
            if (hiddenFlights.size() > 0) {
                flights.add(hiddenFlights.get(0));
                hiddenFlights.remove(0);
            }
        });

        Button removeFlightButton = new Button("REMOVE");
        removeFlightButton.setOnAction(e -> {
            if (flights.size() > 0) {
                hiddenFlights.add(flights.get(0));
                flights.remove(0);
            }
        });

        VBox buttonPane = new VBox();
        buttonPane.setPadding(new Insets(5));

        buttonPane.getChildren().addAll(addFlightButton, removeFlightButton);

        borderPane.setRight(buttonPane);
        primaryStage.setScene(scene);

        primaryStage.show();
    }
    public static void main(String[] args) {
        Application.launch(args);
    }
}
