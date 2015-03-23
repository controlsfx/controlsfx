package org.controlsfx.control.table;


import javafx.application.Application;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.time.LocalDate;

public final class FlightTable extends Application {

    private final ObservableList<Flight> flights = FXCollections.observableArrayList(
            new Flight(567,"ABQ","DAL", LocalDate.of(2015,5,8), 642),
            new Flight(234,"ABQ","DAL", LocalDate.of(2015,5,9), 642),
            new Flight(756,"ABQ","DAL", LocalDate.of(2015,5,11), 642),
            new Flight(268,"ABQ","DAL", LocalDate.of(2015,5,13), 642),

            new Flight(567,"DAL","HOU", LocalDate.of(2015,5,8), 244),
            new Flight(239,"DAL","HOU", LocalDate.of(2015,5,14), 244),
            new Flight(5923,"DAL","HOU", LocalDate.of(2015,5,17), 244),
            new Flight(2389,"DAL","HOU", LocalDate.of(2015,5,6), 244),

            new Flight(287,"SEA","PHX", LocalDate.of(2015,5,8), 1426),
            new Flight(875,"SEA","PHX", LocalDate.of(2015,5,16), 1426),
            new Flight(4288,"SEA","PHX", LocalDate.of(2015,5,9), 1426)
    );

    private final TableView<Flight> table = new TableView<>();

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("Flight Table");
        BorderPane borderPane = new BorderPane();
        Scene scene = new Scene(borderPane, 800, 600);

        table.setItems(flights);
        table.setEditable(true);
        TableColumn<Flight, Integer> flightNumCol = new TableColumn<>("FLIGHT NUM");
        flightNumCol.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper(cellData.getValue().getFlightNumber()));
        table.getColumns().add(flightNumCol);

        TableColumn<Flight,String> origCol = new TableColumn<>("ORIG");
        origCol.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper(cellData.getValue().getOrig()));
        table.getColumns().add(origCol);

        TableColumn<Flight,String> destCol = new TableColumn<>("DEST");
        destCol.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper(cellData.getValue().getDest()));
        table.getColumns().add(destCol);

        TableColumn<Flight,LocalDate> depDateCol = new TableColumn<>("DEP DATE");
        depDateCol.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper(cellData.getValue().getDepartureDate()));
        table.getColumns().add(depDateCol);

        TableColumn<Flight,BigDecimal> mileage =  new TableColumn<>("MILEAGE");
        mileage.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper(cellData.getValue().getMileaage()));
        table.getColumns().add(mileage);
/*
        TableColumn<Flight,String> gateNumber = new TableColumn<>("GATE");
        gateNumber.setCellValueFactory(cellData -> cellData.getValue().getGateNumber());
        gateNumber.setCellFactory(TextFieldTableCell.forTableColumn());
        gateNumber.setEditable(true);
        table.getColumns().add(gateNumber);
*/

        TableColumn<Flight,Boolean> cancelledInd =  new TableColumn<>("CANCELLED");
        cancelledInd.setCellValueFactory(cellData -> cellData.getValue().getCancelledProperty());
        cancelledInd.setEditable(true);
        table.getColumns().add(cancelledInd);

        TableFilter.forTable(table);

        borderPane.setCenter(table);

        primaryStage.setScene(scene);

        primaryStage.show();
    }

    public static void main(String[] args) {
        Application.launch(args);
    }
}
