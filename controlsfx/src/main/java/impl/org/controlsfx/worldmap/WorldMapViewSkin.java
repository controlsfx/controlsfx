package impl.org.controlsfx.worldmap;

import javafx.collections.*;
import javafx.scene.CacheHint;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Shape;
import org.controlsfx.control.ScalableContentPane;
import org.controlsfx.control.WorldMapView;

import java.util.*;


public class WorldMapViewSkin extends SkinBase<WorldMapView> {

    private static final double PREFERRED_WIDTH = 1009;
    private static final double PREFERRED_HEIGHT = 665;
    private static final double MINIMUM_WIDTH = 100;
    private static final double MINIMUM_HEIGHT = 66;
    private static final double MAXIMUM_WIDTH = 2018;
    private static final double MAXIMUM_HEIGHT = 1330;
    private static double MAP_OFFSET_X = -PREFERRED_WIDTH * 0.0285;
    private static double MAP_OFFSET_Y = PREFERRED_HEIGHT * 0.195;
    private static final double ASPECT_RATIO = PREFERRED_HEIGHT / PREFERRED_WIDTH;
    private double width;
    private double height;

    protected Pane countryPane;
    protected ScalableContentPane scalableContentPane;
    protected Map<String, List<WorldMapView.CountryPath>> countryPaths;
    protected ObservableMap<WorldMapView.Location, Shape> locationMap;


    public WorldMapViewSkin(WorldMapView view) {
        super(view);

        countryPaths = new HashMap<>();
        locationMap = FXCollections.observableHashMap();

        countryPane = new Pane();

        scalableContentPane = new ScalableContentPane(countryPane);
        scalableContentPane.setPrefWidth(PREFERRED_WIDTH);
        scalableContentPane.setPrefHeight(PREFERRED_HEIGHT);
        scalableContentPane.setAutoRescale(true);
        scalableContentPane.setFitToHeight(true);
        scalableContentPane.setFitToWidth(true);
        scalableContentPane.widthProperty().addListener(it -> System.out.println(scalableContentPane.getWidth() + " / " + scalableContentPane.getHeight()));

        ListChangeListener<? super WorldMapView.Location> locationsListener = change -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    change.getAddedSubList().forEach(location -> addLocation(location));
                } else if (change.wasRemoved()) {
                    change.getRemoved().forEach(location -> removeLocation(location));
                }
            }
        };

        view.getLocations().addListener(locationsListener);

        locationMap.addListener((MapChangeListener<WorldMapView.Location, Shape>) change -> {
            if (change.wasAdded()) {
                countryPane.getChildren().add(change.getValueAdded());
            } else if (change.wasRemoved()) {
                countryPane.getChildren().remove(change.getValueRemoved());
            }
        });

        BorderPane borderPane = new BorderPane();
        borderPane.setCenter(scalableContentPane);

        getChildren().add(borderPane);

        buildView();
    }

    private void addLocation(final WorldMapView.Location LOCATION) {
        double x = (LOCATION.getLongitude() + 180) * (PREFERRED_WIDTH / 360) + MAP_OFFSET_X;
        double y = (PREFERRED_HEIGHT / 2) - (PREFERRED_WIDTH * (Math.log(Math.tan((Math.PI / 4) + (Math.toRadians(LOCATION.getLatitude()) / 2)))) / (2 * Math.PI)) + MAP_OFFSET_Y;

        Shape locationIcon = new Circle(x, y, 3);
//        locationIcon.setFill(null == LOCATION.getColor() ? Color.RED : LOCATION.getColor());

        StringBuilder tooltipBuilder = new StringBuilder();
        if (!LOCATION.getName().isEmpty()) tooltipBuilder.append(LOCATION.getName());
//        if (!LOCATION.getInfo().isEmpty()) tooltipBuilder.append("\n").append(LOCATION.getInfo());
//        String tooltipText = tooltipBuilder.toString();
//        if (!tooltipText.isEmpty()) {
//            Tooltip.install(locationIcon, new Tooltip(tooltipText));
//        }

        locationMap.put(LOCATION, locationIcon);
    }

    private void removeLocation(final WorldMapView.Location LOCATION) {
        locationMap.remove(LOCATION);
    }

    private void buildView() {
        countryPane.getChildren().clear();

        if (Double.compare(getSkinnable().getPrefWidth(), 0.0) <= 0 || Double.compare(getSkinnable().getPrefHeight(), 0.0) <= 0 ||
                Double.compare(getSkinnable().getWidth(), 0.0) <= 0 || Double.compare(getSkinnable().getHeight(), 0.0) <= 0) {
            if (getSkinnable().getPrefWidth() > 0 && getSkinnable().getPrefHeight() > 0) {
                getSkinnable().setPrefSize(getSkinnable().getPrefWidth(), getSkinnable().getPrefHeight());
            } else {
                getSkinnable().setPrefSize(PREFERRED_WIDTH, PREFERRED_HEIGHT);
            }
        }

        for (WorldMapView.Country country : WorldMapView.Country.values()) {
            List<WorldMapView.CountryPath> paths = country.getPaths();
            countryPane.getChildren().addAll(paths);
            countryPaths.put(country.name(), paths);
        }
    }

//        @Override
//    protected void layoutChildren(double contentX, double contentY, double contentWidth, double contentHeight) {
//                super.layoutChildren(contentX, contentY, contentWidth, contentHeight);
//            scalableContentPane.requestScale();
//        System.out.println("laying out");
//        width = getSkinnable().getWidth() - getSkinnable().getInsets().getLeft() - getSkinnable().getInsets().getRight();
//        height = getSkinnable().getHeight() - getSkinnable().getInsets().getTop() - getSkinnable().getInsets().getBottom();
//
//        if (ASPECT_RATIO * width > height) {
//            width = 1 / (ASPECT_RATIO / height);
//        } else if (1 / (ASPECT_RATIO / height) > width) {
//            height = ASPECT_RATIO * width;
//        }
//
//        if (width > 0 && height > 0) {
//            countryPane.setCache(true);
//            countryPane.setCacheHint(CacheHint.SCALE);
//
////            scalableContentPane.setMaxSize(width, height);
////            scalableContentPane.setPrefSize(width, height);
//
//            countryPane.setCache(false);
//        }
//    }
}