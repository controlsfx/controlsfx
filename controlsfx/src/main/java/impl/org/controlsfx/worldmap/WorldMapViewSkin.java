/**
 * Copyright (c) 2016, ControlsFX
 * All rights reserved.
 * <p>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * * Neither the name of ControlsFX, any associated website, nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * <p>
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL CONTROLSFX BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package impl.org.controlsfx.worldmap;

import javafx.beans.Observable;
import javafx.collections.*;
import javafx.css.PseudoClass;
import javafx.event.EventTarget;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.SkinBase;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.input.ZoomEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import javafx.util.Callback;
import org.controlsfx.control.WorldMapView;

import java.io.IOException;
import java.util.*;

public class WorldMapViewSkin extends SkinBase<WorldMapView> {

    private static final PseudoClass SELECTED_PSEUDO_CLASS = PseudoClass.getPseudoClass("selected");

    private static final String DEFAULT_STYLE_LOCATION = "location";
    private static final String DEFAULT_STYLE_COUNTRY = "country";

    private static final double PREFERRED_WIDTH = 1009;
    private static final double PREFERRED_HEIGHT = 665;
    private static double MAP_OFFSET_X = -PREFERRED_WIDTH * 0.0285;
    private static double MAP_OFFSET_Y = PREFERRED_HEIGHT * 0.195;

    private final Map<WorldMapView.Country, List<? extends String>> countryPathMap = new HashMap<>();
    private final Map<WorldMapView.Country, List<? extends WorldMapView.CountryView>> countryViewMap = new HashMap<>();

    private Pane countryPane;
    private Group group;
    private Group locationsGroup;
    protected ObservableMap<WorldMapView.Location, Node> locationMap;

    public WorldMapViewSkin(WorldMapView view) {
        super(view);

        locationMap = FXCollections.observableHashMap();

        group = new Group();
        group.setManaged(false);
        group.setAutoSizeChildren(false);

        locationsGroup = new Group();
        locationsGroup.setManaged(false);
        locationsGroup.visibleProperty().bind(view.showLocationsProperty());
        locationsGroup.setAutoSizeChildren(false);

        countryPane = new Pane();
        countryPane.getChildren().add(group);

        view.getLocations().addListener(locationsListener);

        // countries
        final ListChangeListener<? super WorldMapView.Country> countriesListener = change -> buildView();
        view.getCountries().addListener(countriesListener);

        locationMap.addListener((MapChangeListener<WorldMapView.Location, Node>) change -> {
            if (change.wasRemoved()) {
                locationsGroup.getChildren().remove(change.getValueRemoved());
            }
        });


        BorderPane borderPane = new BorderPane();
        borderPane.setCenter(countryPane);

        getChildren().add(borderPane);

        view.zoomFactorProperty().addListener(it -> view.requestLayout());

        Properties mapData = loadData();
        for (WorldMapView.Country country : WorldMapView.Country.values()) {
            String countryData = (String) mapData.get(country.name());
            if (countryData == null) {
                System.out.println("Missing SVG path for country " + country.getLocale().getDisplayCountry() + " (" + country + ")");
            } else {
                StringTokenizer st = new StringTokenizer(countryData, ";");
                List<String> paths = new ArrayList<>();
                while (st.hasMoreTokens()) {
                    paths.add(st.nextToken());
                }
                countryPathMap.put(country, paths);
            }
        }

        buildView();

        view.getSelectedCountries().addListener(weakCountrySelectionListener);
        view.selectedCountriesProperty().addListener((Observable it) -> view.getSelectedCountries().addListener(weakCountrySelectionListener));

        view.getSelectedLocations().addListener(weakLocationSelectionListener);
        view.selectedLocationsProperty().addListener((Observable it) -> view.getSelectedLocations().addListener(weakLocationSelectionListener));

        view.getLocations().addListener(weakLocationsListener);
        view.locationsProperty().addListener((Observable it) -> view.getLocations().addListener(weakLocationsListener));

        view.getLocations().forEach(location -> addLocation(location));

        view.addEventHandler(ScrollEvent.SCROLL, evt -> {
            evt.consume();
        });

        view.addEventHandler(ZoomEvent.ZOOM, evt -> {
            double factor = evt.getZoomFactor();
            view.setZoomFactor(view.getZoomFactor() * factor);
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

        view.addEventHandler(MouseEvent.MOUSE_CLICKED, evt -> {
            if (evt.getClickCount() == 2) {
                view.setZoomFactor(1);
                group.setTranslateX(0);
                group.setTranslateY(0);
            } else if (evt.getButton().equals(MouseButton.PRIMARY)) {
                EventTarget target = evt.getTarget();
                if (target instanceof WorldMapView.CountryView) {
                    WorldMapView.CountryView path = (WorldMapView.CountryView) target;
                    WorldMapView.Country country = path.getCountry();
                    boolean wasSelected = view.getSelectedCountries().contains(country);
                    if (view.getCountrySelectionMode().equals(WorldMapView.SelectionMode.SINGLE) ||
                            !(evt.isShortcutDown() || evt.isShiftDown())) {
                        view.getSelectedCountries().clear();
                    }
                    if (wasSelected) {
                        view.getSelectedCountries().remove(country);
                    } else {
                        view.getSelectedCountries().add(country);
                    }
                } else if (target.equals(countryPane)){
                    view.getSelectedCountries().clear();
                } else {
                    for (WorldMapView.Location location : locationMap.keySet()) {
                        Node node = locationMap.get(location);
                        if (target.equals(node)) {
                            boolean wasSelected = view.getSelectedLocations().contains(location);
                            if (view.getLocationSelectionMode().equals(WorldMapView.SelectionMode.SINGLE) ||
                                    !(evt.isShortcutDown() || evt.isShiftDown())) {
                                view.getSelectedLocations().clear();
                            }
                            if (wasSelected) {
                                view.getSelectedLocations().remove(location);
                            } else {
                                view.getSelectedLocations().add(location);
                            }
                            break;
                        }
                    }
                }
            }
        });

        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(view.widthProperty());
        clip.heightProperty().bind(view.heightProperty());
        view.setClip(clip);

        view.countryViewFactoryProperty().addListener(it -> buildView());
        view.locationViewFactoryProperty().addListener(it -> buildView());
    }

    private double dragX;
    private double dragY;

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

    // country selections

    private final ListChangeListener<? super WorldMapView.Country> countrySelectionListener = change -> {
        while (change.next()) {
            if (change.wasAdded()) {
                change.getAddedSubList().forEach(
                        country -> countryViewMap.get(country).forEach(path -> path.pseudoClassStateChanged(SELECTED_PSEUDO_CLASS, true)));
            } else if (change.wasRemoved()) {
                change.getRemoved().forEach(
                        country -> countryViewMap.get(country).forEach(path -> path.pseudoClassStateChanged(SELECTED_PSEUDO_CLASS, false)));
            }
        }
    };

    private final WeakListChangeListener weakCountrySelectionListener = new WeakListChangeListener(countrySelectionListener);

    // location selections

    private final ListChangeListener<? super WorldMapView.Location> locationSelectionListener = change -> {
        while (change.next()) {
            if (change.wasAdded()) {
                change.getAddedSubList().forEach(
                        location -> locationMap.get(location).pseudoClassStateChanged(SELECTED_PSEUDO_CLASS, true));
            } else if (change.wasRemoved()) {
                change.getRemoved().forEach(
                        location -> locationMap.get(location).pseudoClassStateChanged(SELECTED_PSEUDO_CLASS, false));
            }
        }
    };

    private final WeakListChangeListener weakLocationSelectionListener = new WeakListChangeListener(locationSelectionListener);

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
        view.getStyleClass().add(DEFAULT_STYLE_LOCATION);
        view.setManaged(false);
        locationsGroup.getChildren().add(view);
        view.applyCss();
        view.resizeRelocate(coordinates.getX(), coordinates.getY(), view.prefWidth(-1), view.prefHeight(-1));
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

        Callback<WorldMapView.Country, WorldMapView.CountryView> factory = getSkinnable().getCountryViewFactory();
        for (WorldMapView.Country country : WorldMapView.Country.values()) {
            if (getSkinnable().getCountries().isEmpty() || getSkinnable().getCountries().contains(country)) {
                List<WorldMapView.CountryView> countryViews = new ArrayList<>();
                for (String svgPath : countryPathMap.get(country)) {
                    WorldMapView.CountryView view = factory.call(country);
                    if (view != null) {
                        view.setContent(svgPath);
                        view.getStyleClass().add(0, DEFAULT_STYLE_COUNTRY);
                        group.getChildren().addAll(view);
                        countryViews.add(view);
                    }
                }
                countryViewMap.put(country, countryViews);
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

        group.setTranslateX(-group.getLayoutBounds().getMinX());
        group.setTranslateY(-group.getLayoutBounds().getMinY());

        group.setScaleX(scale);
        group.setScaleY(scale);

        group.setLayoutX((contentWidth - prefWidth) / 2);
        group.setLayoutY((contentHeight - prefHeight) / 2);
    }

    /**
     * Loads the properties file that is storing the SVG path information for each country. This method
     * can be overriden to provide a different, maybe more detailed, data set. However, the default data
     * set used by ControlsFX has to be small in order to keep the distribution small. The structure of
     * the properties file needs to look like this:
     * <pre>
     *     AE=M619.87,393.72L620.37,393.57L620.48,394.41L622.67,393.93 ....
     *     AF=M646.88,356.9L649.74,358.2L651.85,357.74L652.44,356.1 ....
     *     ......
     * </pre>
     *
     * @return the properties file storing the SVG path data for each country
     */
    protected Properties loadData() {
        Properties mapData = new Properties();
        try {
            mapData.load(WorldMapView.class.getResourceAsStream("worldmap-small.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return mapData;
    }
}