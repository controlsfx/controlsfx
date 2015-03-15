package org.controlsfx.control.table;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.math.BigDecimal;

public class TableFilterTest extends Application {
 
    private TableView<Airport> table = new TableView<>();
    private final ObservableList<Airport> data = buildAirports();
    final HBox hb = new HBox();
 
    public static void main(String[] args) {
        launch(args);
    }
 
    @Override
    public void start(Stage stage) {
        Scene scene = new Scene(new Group());
        stage.setTitle("Table View Sample");
        stage.setWidth(450);
        stage.setHeight(550);


        TableFilter.forTable(table);

        stage.setScene(scene);
        stage.show();
    }


    private static final ObservableList<Airport> buildAirports()  {
        try {
            final File file = new File("airports.csv");
            final ObservableList<Airport> airports = FXCollections.observableArrayList();
            new BufferedReader(new FileReader(file)).lines().map(l -> new Airport(l))
                    .forEach(a -> airports.add(a));

            return airports;
        } catch (Exception e) {
            throw new RuntimeException("Data initialization failed!");
        }

    }

    private static final class Airport {
        private final int airPortId;
        private final String name;
        private final String city;
        private final String country;
        private final String iataFaa;
        private final String icao;
        private final BigDecimal latitude;
        private final BigDecimal longitude;
        private final BigDecimal altitude;
        private final int timezone;
        private final String dst;
        private final String tzDbTimezone;

        public Airport(String csvLine) {
            String[] prop = csvLine.split(",");
            airPortId = Integer.valueOf(prop[0]).intValue();
            name = prop[1];
            city = prop[2];
            country = prop[3];
            iataFaa = prop[4];
            icao = prop[5];
            latitude = new BigDecimal(prop[6]);
            longitude = new BigDecimal(prop[7]);
            altitude = new BigDecimal(prop[8]);
            timezone = Integer.valueOf(prop[9]).intValue();
            dst = prop[10];
            tzDbTimezone = prop[11];
        }
    }
}