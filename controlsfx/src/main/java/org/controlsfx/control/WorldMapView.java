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
package org.controlsfx.control;

import impl.org.controlsfx.worldmap.WorldMapViewSkin;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Skin;
import javafx.scene.control.Tooltip;
import javafx.scene.shape.Circle;
import javafx.scene.shape.SVGPath;
import javafx.util.Callback;

import java.util.Locale;
import java.util.Objects;

/**
 * A simple map view showing either the entire world or a list of countries. The view
 * is not capable of displaying detailed map information. The view is based on simple SVG data found in a
 * properties file of the ControlsFX distribution. A big advantage of this approach is the fact that the view
 * can be run without a network connection. In addition to showing countries or the world the view can also
 * show locations. The map can be customized by specifying custom factories for the country and
 * location views.
 * <h2>Example: Country View Factory</h2>
 * The code snippet below shows how a custom country view factory can be used to assign individual styles
 * to all countries. In this example the style is used to color the countries differently.
 * <code>
 * worldMapView.setCountryViewFactory(country -> {
 * CountryView view = new CountryView(country);
 * if (showColorsProperty.get()) {
 * view.getStyleClass().add("country" + ((country.ordinal() % 8) + 1));
 * }
 * return view;
 * });
 * </code>
 * <h2>Example: Location View Factory</h2>
 * Each location can be visualized with its own node. The default location view factory creates a simple circle
 * shape.
 * <p>
 * <code>
 * worldMapView.setLocationViewFactory(location -> {
 * Circle circle = new Circle();
 * circle.getStyleClass().add("location");
 * circle.setRadius(4);
 * circle.setTranslateX(-4); // translate to center node on location
 * circle.setTranslateY(-4); // translate to center node on location
 * return circle;
 * });
 * </code>
 * Shown below is a screenshot of the world map view:
 * <br>
 * <center>
 * <img src="world-map.png" alt="Screenshot of WorldMapView">
 * </center>
 */
public class WorldMapView extends ControlsFXControl {

    private static final String DEFAULT_STYLE_CLASS = "world-map";

    // A shared tooltip instance to save memory
    private Tooltip tooltip = new Tooltip();

    /**
     * Constructs a new map view with an initially empty list of countries which will
     * result in the entire world to be shown.
     */
    public WorldMapView() {
        getStyleClass().add(DEFAULT_STYLE_CLASS);

        setCountryViewFactory(country -> {
            CountryView view = new CountryView(country);
            view.setOnMouseEntered(evt -> tooltip.setText(country.getLocale().getDisplayCountry()));
            Tooltip.install(view, tooltip);
            return view;
        });

        setLocationViewFactory(location -> {
            Circle circle = new Circle();
            circle.setRadius(4);
            circle.setTranslateX(-4);
            circle.setTranslateY(-4);
            circle.setOnMouseEntered(evt -> tooltip.setText(location.getName()));
            Tooltip.install(circle, tooltip);
            return circle;
        });
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new WorldMapViewSkin(this);
    }

    @Override
    public String getUserAgentStylesheet() {
        return getUserAgentStylesheet(WorldMapView.class, "world.css");
    }

    /**
     * The selection modes supported by the view.
     *
     * @see WorldMapView#setCountrySelectionMode(SelectionMode)
     * @see WorldMapView#setLocationSelectionMode(SelectionMode)
     * @see WorldMapView#getSelectedCountries()
     * @see WorldMapView#getSelectedLocations()
     */
    public enum SelectionMode {

        /**
         * The single selection mode allows the user to always only select one country and one location
         * at a time.
         */
        SINGLE,

        /**
         * The multiple selection mode allows the user to select several countries and locations at the
         * same time.
         */
        MULTIPLE
    }

    // country selection support

    private final ObjectProperty<SelectionMode> countrySelectionMode = new SimpleObjectProperty<>(this, "countrySelectionMode", SelectionMode.MULTIPLE);

    /**
     * A property used to store the selection mode that will be applied for the selection
     * of countries.
     *
     * @return the country selection mode property
     */
    public final ObjectProperty<SelectionMode> countrySelectionModeProperty() {
        return countrySelectionMode;
    }

    /**
     * Returns the value of {@link #countrySelectionModeProperty()}
     *
     * @return the country selection mode
     */
    public final SelectionMode getCountrySelectionMode() {
        return countrySelectionMode.get();
    }

    /**
     * Sets the value of {@link #countrySelectionModeProperty()}.
     *
     * @param mode the country selection mode
     */
    public final void setCountrySelectionMode(SelectionMode mode) {
        this.countrySelectionMode.set(mode);
    }

    // location selection support

    private final ObjectProperty<SelectionMode> locationSelectionMode = new SimpleObjectProperty<>(this, "locationSelectionMode", SelectionMode.MULTIPLE);

    /**
     * A property used to store the selection mode that will be applied for the selection
     * of locations.
     *
     * @return the location selection mode property
     */
    public final ObjectProperty<SelectionMode> locationSelectionModeProperty() {
        return locationSelectionMode;
    }

    /**
     * Returns the value of {@link #locationSelectionModeProperty()}
     *
     * @return the location selection mode
     */
    public final SelectionMode getLocationSelectionMode() {
        return locationSelectionMode.get();
    }

    /**
     * Sets the value of {@link #locationSelectionModeProperty()}.
     *
     * @param mode the location selection mode
     */
    public final void setLocationSelectionMode(SelectionMode mode) {
        this.locationSelectionMode.set(mode);
    }

    // zoom support

    private final DoubleProperty zoomFactor = new SimpleDoubleProperty(this, "zoomFactor", 1) {
        @Override
        public void set(double newValue) {
            super.set(Math.max(1, Math.min(10, newValue)));
        }
    };

    /**
     * A property used to store the current zoom factor, a value between 1 and 10.
     *
     * @return the zoom factor
     */
    public final DoubleProperty zoomFactorProperty() {
        return zoomFactor;
    }

    /**
     * Returns the value of {@link #zoomFactorProperty()}.
     *
     * @return the zoom factor
     */
    public final double getZoomFactor() {
        return zoomFactor.get();
    }

    /**
     * Sets the value of {@link #zoomFactorProperty()}.
     *
     * @param factor the zoom factor
     */
    public final void setZoomFactor(double factor) {
        this.zoomFactor.set(factor);
    }

    private final ListProperty<Country> selectedCountries = new SimpleListProperty<>(this, "selectedCountries", FXCollections.observableArrayList());

    /**
     * A property used for storing the list of selected countries (aka "the selection model").
     *
     * @return the selected countries property
     */
    public final ListProperty<Country> selectedCountriesProperty() {
        return selectedCountries;
    }

    /**
     * Returns the list of currently selected countries.
     *
     * @return the list of selected countries
     */
    public final ObservableList<Country> getSelectedCountries() {
        return selectedCountries.get();
    }

    /**
     * Sets the list of currently selected countries.
     *
     * @param countries the selected countries
     */
    public final void setSelectedCountries(ObservableList<Country> countries) {
        this.selectedCountries.set(countries);
    }

    private final ListProperty<Location> selectedLocations = new SimpleListProperty<>(this, "selectedLocations", FXCollections.observableArrayList());

    /**
     * A property used for storing the list of selected locations (aka "the selection model").
     *
     * @return the selected locations property
     */
    public final ListProperty<Location> selectedLocationsProperty() {
        return selectedLocations;
    }

    /**
     * Returns the list of currently selected locations.
     *
     * @return the list of selected locations
     */
    public final ObservableList<Location> getSelectedLocations() {
        return selectedLocations.get();
    }

    /**
     * Sets the list of currently selected locations.
     *
     * @param locations the selected locations
     */
    public final void setSelectedLocations(ObservableList<Location> locations) {
        this.selectedLocations.set(locations);
    }

    // Visible countries support.

    private final ListProperty<Country> countries = new SimpleListProperty<>(this, "countries", FXCollections.observableArrayList());

    /**
     * A property used to store the list of countries that should be shown by the map. If this
     * list is empty then the view will show the entire world.
     *
     * @return the list of countries shown by the map
     */
    public final ListProperty<Country> countriesProperty() {
        return countries;
    }

    /**
     * Returns the list of countries that will be shown by the map.
     *
     * @return the list of countries shown by the map
     */
    public final ObservableList<Country> getCountries() {
        return countries.get();
    }

    /**
     * Sets the list of countries that will be shown by the map.
     *
     * @param countries the list of countries shown by the map
     */
    public final void setCountries(ObservableList<Country> countries) {
        this.countries.set(countries);
    }

    private final ListProperty<Location> locations = new SimpleListProperty<>(this, "locations", FXCollections.observableArrayList());

    /**
     * A property used to store the list of locations shown by the map.
     */
    public final ListProperty<Location> locationsProperty() {
        return locations;
    }

    /**
     * Returns the list of locations shown by the map.
     *
     * @return the list of locations
     */
    public final ObservableList<Location> getLocations() {
        return locations.get();
    }

    /**
     * Sets the list of locations shown by the map.
     *
     * @param locations the list of locations
     */
    public final void setLocations(ObservableList<Location> locations) {
        this.locations.set(locations);
    }

    private final BooleanProperty showLocations = new SimpleBooleanProperty(this, "showLocations", true);

    /**
     * A property used to control whether locations will be shown by the map or not.
     *
     * @return a property to control the visibility of locations
     */
    public final BooleanProperty showLocationsProperty() {
        return showLocations;
    }

    /**
     * Returns the value of {@link #showLocationsProperty()}.
     *
     * @return true if locations are shown
     */
    public final boolean isShowLocations() {
        return showLocations.get();
    }

    /**
     * Sets the value of {@link #showLocationsProperty()}.
     *
     * @param show if true then locations are shown
     */
    public final void setShowLocations(boolean show) {
        this.showLocations.set(show);
    }

    // Location view factory.

    private final ObjectProperty<Callback<Location, Node>> locationViewFactory = new SimpleObjectProperty<>(this, "locationViewFactory");

    /**
     * A property used to store a factory callback for creating new location views (nodes).
     *
     * @return the location view factory property
     */
    public final ObjectProperty<Callback<Location, Node>> locationViewFactoryProperty() {
        return locationViewFactory;
    }

    /**
     * Returns the value of {@link #locationViewFactoryProperty()}.
     *
     * @return the location view factory
     */
    public final Callback<Location, Node> getLocationViewFactory() {
        return locationViewFactory.get();
    }

    /**
     * Sets the value of {@link #locationViewFactoryProperty()}.
     *
     * @param factory the location view factory
     */
    public final void setLocationViewFactory(Callback<Location, Node> factory) {
        this.locationViewFactory.set(factory);
    }

    // Country view factory.

    private final ObjectProperty<Callback<Country, CountryView>> countryViewFactory = new SimpleObjectProperty(this, "countryViewFactory");

    /**
     * A property used to store a factory callback for creating new country views.
     *
     * @return the country view factory property
     */
    public final ObjectProperty<Callback<Country, CountryView>> countryViewFactoryProperty() {
        return countryViewFactory;
    }

    /**
     * Returns the value of {@link #countryViewFactoryProperty()}.
     *
     * @return the country view factory
     */
    public final Callback<Country, CountryView> getCountryViewFactory() {
        return countryViewFactory.get();
    }

    /**
     * Sets the value of {@link #countryViewFactoryProperty()}.
     *
     * @param factory the country view factory
     */
    public final void setCountryViewFactory(Callback<Country, CountryView> factory) {
        this.countryViewFactory.set(factory);
    }

    /**
     * A view used to visualize the bounds of a country via SVG. The SVG information
     * will be set on the view as part of the skin implementation of the map view. Applications
     * can provide subclasses of this view in order to install their own event handling or
     * tooltips.
     *
     * @see WorldMapView#setCountryViewFactory(Callback)
     */
    public static class CountryView extends SVGPath {

        private final Country country;

        public CountryView(Country country) {
            super();
            this.country = Objects.requireNonNull(country);
        }

        public final Country getCountry() {
            return country;
        }

        public String getName() {
            return country.name();
        }
    }

    /**
     * An enumerator listing all countries of the world.
     *
     * @see WorldMapView#setLocationViewFactory(Callback)
     * @see WorldMapView#getCountries()
     */
    public enum Country {
        AE,
        AO,
        AR,
        AT,
        AU,
        AZ,
        BA,
        BD,
        BE,
        BF,
        BG,
        BI,
        BJ,
        BN,
        BO,
        BR,
        BS,
        BT,
        BW,
        BY,
        BZ,
        CA,
        CD,
        CF,
        CG,
        CH,
        CI,
        CL,
        CM,
        CN,
        CO,
        CR,
        CU,
        CY,
        CZ,
        DE,
        DJ,
        DK,
        DO,
        DZ,
        EC,
        EE,
        EG,
        EH,
        ER,
        ES,
        ET,
        FK,
        FI,
        FJ,
        FR,
        GA,
        GB,
        GE,
        GF,
        GH,
        GL,
        GM,
        GN,
        GQ,
        GR,
        GT,
        GW,
        GY,
        HN,
        HR,
        HT,
        HU,
        ID,
        IE,
        IL,
        IN,
        IQ,
        IR,
        IS,
        IT,
        JM,
        JO,
        JP,
        KE,
        KG,
        KH,
        KP,
        KR,
        XK,
        KW,
        KZ,
        LA,
        LB,
        LK,
        LR,
        LS,
        LT,
        LU,
        LV,
        LY,
        MA,
        MD,
        ME,
        MG,
        MK,
        ML,
        MM,
        MN,
        MR,
        MW,
        MX,
        MY,
        MZ,
        NA,
        NC,
        NE,
        NG,
        NI,
        NL,
        NO,
        NP,
        NZ,
        OM,
        PA,
        PE,
        PG,
        PH,
        PL,
        PK,
        PR,
        PS,
        PT,
        PY,
        QA,
        RO,
        RS,
        RU,
        RW,
        SA,
        SB,
        SD,
        SE,
        SI,
        SJ,
        SK,
        SL,
        SN,
        SO,
        SR,
        SS,
        SV,
        SY,
        SZ,
        TD,
        TF,
        TG,
        TH,
        TJ,
        TL,
        TM,
        TN,
        TR,
        TT,
        TW,
        TZ,
        UA,
        UG,
        US,
        UY,
        UZ,
        VE,
        VN,
        VU,
        YE,
        ZA,
        ZM,
        ZW;

        private final Locale locale;

        Country(final String... p) {
            this.locale = new Locale("", name());
        }

        /**
         * Returns the locale for the given country. The locale can be used to lookup
         * the display name of the country.
         *
         * @return the locale matching the country
         */
        public Locale getLocale() {
            return locale;
        }
    }

    /**
     * An object using latitude and longitude information to specify a location
     * in the real world.
     *
     * @see WorldMapView#setLocationViewFactory(Callback)
     */
    public static class Location {

        private String name;
        private double latitude;
        private double longitude;

        /**
         * Constructs a new location.
         *
         * @param latitude the latitude data
         * @param longitude the longitude data
         */
        public Location(double latitude, double longitude) {
            this("", latitude, longitude);
        }

        /**
         * Constructs a new location.
         *
         * @param name the name of the location (e.g. "Zurich")
         * @param latitude the latitude data
         * @param longitude the longitude data
         */
        public Location(String name, double latitude, double longitude) {
            this.name = name;
            this.latitude = latitude;
            this.longitude = longitude;
        }

        /**
         * Returns the name of the location.
         *
         * @return the location's name
         */
        public final String getName() {
            return name;
        }

        /**
         * Returns the latitude of the location.
         *
         * @return the latitude
         */
        public final double getLatitude() {
            return latitude;
        }

        /**
         * Returns the longitude of the location.
         *
         * @return the longitude
         */
        public final double getLongitude() {
            return longitude;
        }
    }
}