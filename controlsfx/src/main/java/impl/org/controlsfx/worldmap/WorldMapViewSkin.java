package impl.org.controlsfx.worldmap;

import javafx.beans.Observable;
import javafx.collections.*;
import javafx.css.PseudoClass;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.util.Callback;
import org.controlsfx.control.WorldMapView;

import java.io.IOException;

import java.util.*;

public class WorldMapViewSkin extends SkinBase<WorldMapView> {

    private static final PseudoClass SELECTED_PSEUDO_CLASS = PseudoClass.getPseudoClass("selected");

    private static final double PREFERRED_WIDTH = 1009;
    private static final double PREFERRED_HEIGHT = 665;

    private final Map<WorldMapView.Country, List<? extends WorldMapView.CountryView>> countryPathMap = new HashMap<>();

    protected Pane countryPane;
    protected Group group;
    protected ObservableMap<WorldMapView.Location, Node> locationMap;


    public WorldMapViewSkin(WorldMapView view) {
        super(view);

        locationMap = FXCollections.observableHashMap();

        group = new Group();
        group.setManaged(false);

        countryPane = new Pane();
        countryPane.getChildren().add(group);

        view.getLocations().addListener(locationsListener);

        // countries
        final ListChangeListener<? super WorldMapView.Country> countriesListener = change -> buildView();
        view.getCountries().addListener(countriesListener);

        locationMap.addListener((MapChangeListener<WorldMapView.Location, Node>) change -> {
            if (change.wasAdded()) {
                group.getChildren().add(change.getValueAdded());
            } else if (change.wasRemoved()) {
                group.getChildren().remove(change.getValueRemoved());
            }
        });


        BorderPane borderPane = new BorderPane();
        borderPane.setCenter(countryPane);

        getChildren().add(borderPane);

        view.zoomFactorProperty().addListener(it -> view.requestLayout());

        view.setCountryViewFactory(country -> countryPathMap.get(country));

        Properties data = loadData();
        for (WorldMapView.Country country : WorldMapView.Country.values()) {
            String path = (String) data.get(country.name());
            if (path == null) {
                System.out.println("Missing SVG path for country " + country.getLocale().getDisplayCountry() + " (" + country + ")");

            } else {
                StringTokenizer st = new StringTokenizer(path, ";");
                List<WorldMapView.CountryView> countryViews = new ArrayList<>();
                while (st.hasMoreTokens()) {
                    String token = st.nextToken();
                    WorldMapView.CountryView countryView = new WorldMapView.CountryView(country, token);
                    countryView.getStyleClass().add("country");
                    countryViews.add(countryView);
                }
                countryPathMap.put(country, countryViews);
            }
        }

        buildView();

        view.getSelectedCountries().addListener(weakSelectionListener);
        view.selectedCountriesProperty().addListener((Observable it) -> view.getSelectedCountries().addListener(weakSelectionListener));

        view.getLocations().addListener(weakLocationsListener);
        view.locationsProperty().addListener((Observable it) -> view.getLocations().addListener(weakLocationsListener));

        view.getLocations().forEach(location -> addLocation(location));
    }

    // locations
    private final ListChangeListener<? super WorldMapView.Location> locationsListener = change -> {
        while (change.next()) {
            if (change.wasAdded()) {
                change.getAddedSubList().forEach(location -> addLocation(location));
            } else if (change.wasRemoved()) {
                change.getRemoved().forEach(location -> removeLocation(location));
            }
        }
    };

    private final WeakListChangeListener weakLocationsListener = new WeakListChangeListener(locationsListener);

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

    private void addLocation(WorldMapView.Location location) {
        double x = (location.getLongitude() + 180) * (group.prefWidth(-1) / 360);
        double y = (group.prefHeight(-1) / 2) - (group.prefWidth(-1) * (Math.log(Math.tan((Math.PI / 4) + (Math.toRadians(location.getLatitude()) / 2)))) / (2 * Math.PI));

        Callback<WorldMapView.Location, Node> locationViewFactory = getSkinnable().getLocationViewFactory();
        Node view = locationViewFactory.call(location);
        view.setLayoutX(x);
        view.setLayoutY(y);
        locationMap.put(location, view);
    }

    private void removeLocation(WorldMapView.Location location) {
        locationMap.remove(location);
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

        Callback<WorldMapView.Country, List<? extends WorldMapView.CountryView>> factory = getSkinnable().getCountryViewFactory();
        for (WorldMapView.Country country : WorldMapView.Country.values()) {
            if (getSkinnable().getCountries().isEmpty() || getSkinnable().getCountries().contains(country)) {
                List<? extends WorldMapView.CountryView> paths = factory.call(country);
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