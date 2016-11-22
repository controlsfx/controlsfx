package impl.org.controlsfx.worldmap;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;
import javafx.scene.Group;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.Shape;
import javafx.util.Callback;
import org.controlsfx.control.WorldMapView;

import javafx.beans.Observable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WorldMapViewSkin extends SkinBase<WorldMapView> {

    private static final double PREFERRED_WIDTH = 1009;
    private static final double PREFERRED_HEIGHT = 665;
    private static double MAP_OFFSET_X = -PREFERRED_WIDTH * 0.0285;
    private static double MAP_OFFSET_Y = PREFERRED_HEIGHT * 0.195;

    protected Pane countryPane;
    protected Group group;
    protected Map<String, List<WorldMapView.CountryPath>> countryPaths;
    protected ObservableMap<WorldMapView.Location, Shape> locationMap;


    public WorldMapViewSkin(WorldMapView view) {
        super(view);

        countryPaths = new HashMap<>();
        locationMap = FXCollections.observableHashMap();

        group = new Group();
        group.setManaged(false);

        countryPane = new Pane();
        countryPane.getChildren().add(group);

        // locations
        final ListChangeListener<? super WorldMapView.Location> locationsListener = change -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    change.getAddedSubList().forEach(location -> addLocation(location));
                } else if (change.wasRemoved()) {
                    change.getRemoved().forEach(location -> removeLocation(location));
                }
            }
        };

        view.getLocations().addListener(locationsListener);
//        view.locationsProperty().addListener((Observable it) -> view.getLocations().addListener(locationsListener));

        // countries
        final ListChangeListener<? super WorldMapView.Country> countriesListener = change -> buildView();
        view.getCountries().addListener(countriesListener);

        locationMap.addListener((MapChangeListener<WorldMapView.Location, Shape>) change -> {
            if (change.wasAdded()) {
                countryPane.getChildren().add(change.getValueAdded());
            } else if (change.wasRemoved()) {
                countryPane.getChildren().remove(change.getValueRemoved());
            }
        });


        BorderPane borderPane = new BorderPane();
        borderPane.setCenter(countryPane);

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
        group.getChildren().clear();

        if (Double.compare(getSkinnable().getPrefWidth(), 0.0) <= 0 || Double.compare(getSkinnable().getPrefHeight(), 0.0) <= 0 ||
                Double.compare(getSkinnable().getWidth(), 0.0) <= 0 || Double.compare(getSkinnable().getHeight(), 0.0) <= 0) {
            if (getSkinnable().getPrefWidth() > 0 && getSkinnable().getPrefHeight() > 0) {
                getSkinnable().setPrefSize(getSkinnable().getPrefWidth(), getSkinnable().getPrefHeight());
            } else {
                getSkinnable().setPrefSize(PREFERRED_WIDTH, PREFERRED_HEIGHT);
            }
        }

        Callback<WorldMapView.Country, List<WorldMapView.CountryPath>> factory = getSkinnable().getCountryFactory();
        for (WorldMapView.Country country : WorldMapView.Country.values()) {
            if (getSkinnable().getCountries().isEmpty() || getSkinnable().getCountries().contains(country)) {
                List<WorldMapView.CountryPath> paths = factory.call(country);
                group.getChildren().addAll(paths);
                countryPaths.put(country.name(), paths);
            }
        }

        System.out.println("group bounds: " + group.getLayoutBounds());

        getSkinnable().requestLayout();
    }

    @Override
    protected void layoutChildren(double contentX, double contentY, double contentWidth, double contentHeight) {
        super.layoutChildren(contentX, contentY, contentWidth, contentHeight);

        double offsetX = group.getLayoutX();
        double offsetY = group.getLayoutY();

        double prefWidth = group.prefWidth(-1);
        double prefHeight = group.prefHeight(-1);

        double scaleX = contentWidth / prefWidth;
        double scaleY = contentHeight / prefHeight;

        double scale = Math.min(scaleX, scaleY);

        group.setTranslateX(-group.getLayoutBounds().getMinX());
        group.setTranslateY(-group.getLayoutBounds().getMinY());

        group.setScaleX(scale);
        group.setScaleY(scale);

        group.setLayoutX((contentWidth - prefWidth) / 2);
        group.setLayoutY((contentHeight - prefHeight) / 2);
    }
}