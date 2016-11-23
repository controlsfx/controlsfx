package impl.org.controlsfx.worldmap;

import javafx.beans.Observable;
import javafx.collections.*;
import javafx.css.PseudoClass;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.SkinBase;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.input.ZoomEvent;
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
    private static double MAP_OFFSET_X = -PREFERRED_WIDTH * 0.0285;
    private static double MAP_OFFSET_Y = PREFERRED_HEIGHT * 0.195;

    private final Map<WorldMapView.Country, List<? extends WorldMapView.CountryView>> countryPathMap = new HashMap<>();

    private Pane countryPane;
    private Group group;
    private Group locationsGroup;
    protected ObservableMap<WorldMapView.Location, Node> locationMap;


    public WorldMapViewSkin(WorldMapView view) {
        super(view);

        locationMap = FXCollections.observableHashMap();

        group = new Group();
        group.setManaged(false);

        locationsGroup = new Group();
        locationsGroup.setManaged(false);
        locationsGroup.visibleProperty().bind(view.showLocationsProperty());

        countryPane = new Pane();
        countryPane.getChildren().add(group);

        view.getLocations().addListener(locationsListener);

        // countries
        final ListChangeListener<? super WorldMapView.Country> countriesListener = change -> buildView();
        view.getCountries().addListener(countriesListener);

        locationMap.addListener((MapChangeListener<WorldMapView.Location, Node>) change -> {
            if (change.wasAdded()) {
                locationsGroup.getChildren().add(change.getValueAdded());
            } else if (change.wasRemoved()) {
                locationsGroup.getChildren().remove(change.getValueRemoved());
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

        view.addEventHandler(ScrollEvent.SCROLL, evt -> {
            evt.consume();
        });

        view.addEventHandler(ZoomEvent.ZOOM, evt -> {
            double factor = evt.getZoomFactor();
            System.out.println(factor);
            double tx = group.getTranslateX();
            double ty = group.getTranslateY();
            view.setZoomFactor(view.getZoomFactor() * factor);
//            group.setTranslateX(tx - ((factor-1) * PREFERRED_WIDTH));
//            group.setTranslateY(ty - ((factor-1) * PREFERRED_HEIGHT));
            evt.consume();
        });

        view.addEventHandler(MouseEvent.MOUSE_PRESSED, evt -> {
            dragX = evt.getX();
            dragY = evt.getY();
        });

        view.addEventHandler(MouseEvent.MOUSE_DRAGGED, evt -> {
            double deltaX = evt.getX() - dragX;
            double deltaY = evt.getY() - dragY;
            group.setTranslateX(group.getTranslateX() + deltaX);
            group.setTranslateY(group.getTranslateY() + deltaY);
            dragX = evt.getX();
            dragY = evt.getY();
        });
    }

    private double clamp(final double MIN, final double MAX, final double VALUE) {
        if (VALUE < MIN) return MIN;
        if (VALUE > MAX) return MAX;
        return VALUE;
    }

    private double dragX;
    private double dragY;
    private double zoomSceneX;
    private double zoomSceneY;

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

    private Point2D getLocationCoordinates(WorldMapView.Location location) {
        double x = (location.getLongitude() + 180) * (PREFERRED_WIDTH / 360) + MAP_OFFSET_X;
        double y = (PREFERRED_HEIGHT / 2) - (PREFERRED_WIDTH * (Math.log(Math.tan((Math.PI / 4) + (Math.toRadians(location.getLatitude()) / 2)))) / (2 * Math.PI)) + MAP_OFFSET_Y;
        return new Point2D(x, y);
    }

    private void addLocation(WorldMapView.Location location) {
        Point2D coordinates = getLocationCoordinates(location);
        Callback<WorldMapView.Location, Node> locationViewFactory = getSkinnable().getLocationViewFactory();
        Node view = locationViewFactory.call(location);
        if (view == null) {
            throw new IllegalArgumentException("location view factory returned NULL");
        }
        view.applyCss();
        view.setLayoutX(coordinates.getX() - view.prefWidth(-1) / 2);
        view.setLayoutY(coordinates.getY() - view.prefHeight(-1) / 2);
        locationMap.put(location, view);
    }

    private void removeLocation(WorldMapView.Location location) {
        locationMap.remove(location);
    }

    private void buildView() {
        group.getChildren().clear();
        locationsGroup.getChildren().clear();

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

        for (WorldMapView.Location location : locationMap.keySet()) {
            Point2D coordinates = getLocationCoordinates(location);
            if (group.getLayoutBounds().contains(coordinates)) {
                locationsGroup.getChildren().add(locationMap.get(location));
            }
        }

        group.getChildren().add(locationsGroup);

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