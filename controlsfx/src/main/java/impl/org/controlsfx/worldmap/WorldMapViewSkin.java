package impl.org.controlsfx.worldmap;

import javafx.collections.*;
import javafx.css.PseudoClass;
import javafx.scene.Group;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Shape;
import javafx.util.Callback;
import org.controlsfx.control.WorldMapView;

import java.io.IOException;
import java.util.*;

public class WorldMapViewSkin extends SkinBase<WorldMapView> {

    private static final PseudoClass SELECTED_PSEUDO_CLASS = PseudoClass.getPseudoClass("selected");

    private static final double PREFERRED_WIDTH = 1009;
    private static final double PREFERRED_HEIGHT = 665;
    private static double MAP_OFFSET_X = -PREFERRED_WIDTH * 0.0285;
    private static double MAP_OFFSET_Y = PREFERRED_HEIGHT * 0.195;

    private final Map<WorldMapView.Country, List<? extends WorldMapView.CountryPath>> countryPathMap = new HashMap<>();

    protected Pane countryPane;
    protected Group group;
    protected ObservableMap<WorldMapView.Location, Shape> locationMap;


    public WorldMapViewSkin(WorldMapView view) {
        super(view);

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

        view.zoomFactorProperty().addListener(it -> view.requestLayout());

        view.setCountryFactory(country -> countryPathMap.get(country));

        Properties data = loadData();
        for (WorldMapView.Country country : WorldMapView.Country.values()) {
            String path = (String) data.get(country.name());
            if (path == null) {
                System.out.println("Missing SVG path for country " + country.getLocale().getDisplayCountry() + " (" + country + ")");

            } else {
                StringTokenizer st = new StringTokenizer(path, ";");
                List<WorldMapView.CountryPath> countryPaths = new ArrayList<>();
                while (st.hasMoreTokens()) {
                    String token = st.nextToken();
                    WorldMapView.CountryPath countryPath = new WorldMapView.CountryPath(country, token);
                    countryPath.getStyleClass().add("country");
                    countryPaths.add(countryPath);
                }
                countryPathMap.put(country, countryPaths);
            }
        }

        buildView();

        view.getSelectedCountries().addListener(weakSelectionListener);
        view.selectedCountriesProperty().addListener((javafx.beans.Observable it) -> view.getSelectedCountries().addListener(weakSelectionListener));
    }

    private final ListChangeListener<? super WorldMapView.Country> selectionListener = change -> {
        while (change.next()) {
            if (change.wasAdded()) {
                change.getAddedSubList().forEach(
                        country -> countryPathMap.get(country).forEach(path -> path.pseudoClassStateChanged(SELECTED_PSEUDO_CLASS, true)));
            } else if (change.wasRemoved()) {
                change.getRemoved().forEach(
                        country -> countryPathMap.get(country).forEach(path -> path.pseudoClassStateChanged(SELECTED_PSEUDO_CLASS, false)));
            }
        }
    };

    private final WeakListChangeListener weakSelectionListener = new WeakListChangeListener(selectionListener);

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

        Callback<WorldMapView.Country, List<? extends WorldMapView.CountryPath>> factory = getSkinnable().getCountryFactory();
        for (WorldMapView.Country country : WorldMapView.Country.values()) {
            if (getSkinnable().getCountries().isEmpty() || getSkinnable().getCountries().contains(country)) {
                List<? extends WorldMapView.CountryPath> paths = factory.call(country);
                if (paths != null) {
                    group.getChildren().addAll(paths);
                }
            }
        }

        getSkinnable().requestLayout();
    }

    @Override
    protected void layoutChildren(double contentX, double contentY, double contentWidth, double contentHeight) {
        super.layoutChildren(contentX, contentY, contentWidth, contentHeight);

        double prefWidth = group.prefWidth(-1);
        double prefHeight = group.prefHeight(-1);

        double scaleX = contentWidth / prefWidth;
        double scaleY = contentHeight / prefHeight;

        double scale = Math.min(scaleX, scaleY) * getSkinnable().getZoomFactor();

        group.setTranslateX(-group.getLayoutBounds().getMinX());
        group.setTranslateY(-group.getLayoutBounds().getMinY());

        group.setScaleX(scale);
        group.setScaleY(scale);

        group.setLayoutX((contentWidth - prefWidth) / 2);
        group.setLayoutY((contentHeight - prefHeight) / 2);
    }

    protected Properties loadData() {
        Properties mapData = new Properties();
        try {
            mapData.load(WorldMapView.class.getResourceAsStream("worldmap-large.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return mapData;
    }
}