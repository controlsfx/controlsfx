package org.controlsfx.control;

import impl.org.controlsfx.worldmap.WorldMapViewSkin;
import javafx.beans.property.*;
import javafx.collections.*;
import javafx.event.EventTarget;
import javafx.scene.Node;
import javafx.scene.control.Skin;
import javafx.scene.input.*;
import javafx.scene.shape.Circle;
import javafx.scene.shape.SVGPath;
import javafx.util.Callback;

import java.util.*;


public class WorldMapView extends ControlsFXControl {

    private static final String DEFAULT_STYLE_CLASS = "world-map";

    public WorldMapView() {
        getStyleClass().add(DEFAULT_STYLE_CLASS);

        setLocationViewFactory(location -> {
            Circle circle = new Circle();
            circle.getStyleClass().add("location");
            circle.setRadius(4);
            return circle;
        });

        addEventFilter(MouseEvent.MOUSE_CLICKED, evt -> {
            if (evt.getButton().equals(MouseButton.PRIMARY)) {
                EventTarget target = evt.getTarget();
                if (target instanceof CountryView) {
                    CountryView path = (CountryView) target;
                    boolean wasSelected = getSelectedCountries().contains(target);
                    if (getSelectionMode().equals(SelectionMode.SINGLE) ||
                            !(evt.isShortcutDown() || evt.isShiftDown())) {
                        getSelectedCountries().clear();
                    }
                    Country country = path.getCountry();
                    if (wasSelected) {
                        getSelectedCountries().remove(country);
                    } else {
                        getSelectedCountries().add(country);
                    }
                } else {
                    getSelectedCountries().clear();
                }
            }
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

    public enum SelectionMode {
        SINGLE,
        MULTIPLE;
    }

    private final ObjectProperty<SelectionMode> selectionMode = new SimpleObjectProperty<>(this, "selectionMode", SelectionMode.MULTIPLE);

    public final ObjectProperty<SelectionMode> selectionModeProperty() {
        return selectionMode;
    }

    public final SelectionMode getSelectionMode() {
        return selectionMode.get();
    }

    public final void setSelectionMode(SelectionMode selectionMode) {
        this.selectionMode.set(selectionMode);
    }

    private final DoubleProperty zoomFactor = new SimpleDoubleProperty(this, "zoomFactor", 1) {
        @Override
        public void set(double newValue) {
            super.set(Math.max(1, Math.min(10, newValue)));
        }
    };

    public final DoubleProperty zoomFactorProperty() {
        return zoomFactor;
    }

    public final double getZoomFactor() {
        return zoomFactor.get();
    }

    public final void setZoomFactor(double zoomFactor) {
        this.zoomFactor.set(zoomFactor);
    }

    private final ListProperty<Country> selectedCountries = new SimpleListProperty<>(this, "selectedCountries", FXCollections.observableArrayList());

    public final ListProperty<Country> selectedCountriesProperty() {
        return selectedCountries;
    }

    public final ObservableList<Country> getSelectedCountries() {
        return selectedCountries.get();
    }

    public final void setSelectedCountries(ObservableList<Country> selectedCountries) {
        this.selectedCountries.set(selectedCountries);
    }

    private final ListProperty<Country> countries = new SimpleListProperty<>(this, "countries", FXCollections.observableArrayList());

    public final ListProperty<Country> countriesProperty() {
        return countries;
    }

    public final ObservableList<Country> getCountries() {
        return countries.get();
    }

    public final void setCountries(ObservableList<Country> countries) {
        this.countries.set(countries);
    }

    private final ListProperty<Location> locations = new SimpleListProperty<>(this, "locations", FXCollections.observableArrayList());

    public final ListProperty<Location> locationsProperty() {
        return locations;
    }

    public final ObservableList<Location> getLocations() {
        return locations.get();
    }

    public final void setLocations(ObservableList<Location> locations) {
        this.locations.set(locations);
    }

    private final BooleanProperty showLocations = new SimpleBooleanProperty(this, "showLocations", true);

    public final BooleanProperty showLocationsProperty() {
        return showLocations;
    }

    public final boolean isShowLocations() {
        return showLocations.get();
    }

    public final void setShowLocations(boolean showLocations) {
        this.showLocations.set(showLocations);
    }

    // Location view factory.

    private final ObjectProperty<Callback<Location, Node>> locationViewFactory = new SimpleObjectProperty<>(this, "locationViewFactory");

    public final ObjectProperty<Callback<Location, Node>> locationViewFactoryProperty() {
        return locationViewFactory;
    }

    public final Callback<Location, Node> getLocationViewFactory() {
        return locationViewFactory.get();
    }

    public final void setLocationViewFactory(Callback<Location, Node> locationViewFactory) {
        this.locationViewFactory.set(locationViewFactory);
    }

    // Country view factory.

    private final ObjectProperty<Callback<Country, List<? extends CountryView>>> countryViewFactory = new SimpleObjectProperty(this, "countryViewFactory");

    public final ObjectProperty<Callback<Country, List<? extends CountryView>>> countryViewFactoryProperty() {
        return countryViewFactory;
    }

    public final void setCountryViewFactory(Callback<Country, List<? extends CountryView>> countryViewFactory) {
        this.countryViewFactory.set(countryViewFactory);
    }

    public final Callback<Country, List<? extends CountryView>> getCountryViewFactory() {
        return countryViewFactory.get();
    }

    public static class CountryView extends SVGPath {

        private final Country country;

        public CountryView(Country country, String path) {
            super();
            setContent(path);
            this.country = Objects.requireNonNull(country);
        }

        public Country getCountry() {
            return country;
        }

        public String getName() {
            return country.name();
        }
    }

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

        public Locale getLocale() {
            return locale;
        }
    }

    public static class Location {

        private static final double EARTH_RADIUS = 6_371_000;

        private String name;
        private double latitude;
        private double longitude;


        public Location(double latitude, double longitude) {
            this("", latitude, longitude);
        }

        public Location(String name, double latitude, double longitude) {
            this.name = name;
            this.latitude = latitude;
            this.longitude = longitude;
        }

        public final String getName() {
            return name;
        }

        public final double getLatitude() {
            return latitude;
        }

        public final double getLongitude() {
            return longitude;
        }

        public final double getDistanceTo(Location location) {
            return calcDistanceInMeter(this, location);
        }

        public final double calcDistanceInMeter(Location p1, Location p2) {
            return calcDistanceInMeter(p1.getLatitude(), p1.getLongitude(), p2.getLatitude(), p2.getLongitude());
        }

        public final double calcDistanceInKilometer(Location p1, Location p2) {
            return calcDistanceInMeter(p1, p2) / 1000.0;
        }

        public double calcDistanceInMeter(double LAT_1, double LON_1, double LAT_2, double LON_2) {
            final double LAT_1_RADIANS = Math.toRadians(LAT_1);
            final double LAT_2_RADIANS = Math.toRadians(LAT_2);
            final double DELTA_LAT_RADIANS = Math.toRadians(LAT_2 - LAT_1);
            final double DELTA_LON_RADIANS = Math.toRadians(LON_2 - LON_1);

            final double A = Math.sin(DELTA_LAT_RADIANS * 0.5) * Math.sin(DELTA_LAT_RADIANS * 0.5) + Math.cos(LAT_1_RADIANS) * Math.cos(LAT_2_RADIANS) * Math.sin(DELTA_LON_RADIANS * 0.5) * Math.sin(DELTA_LON_RADIANS * 0.5);
            final double C = 2 * Math.atan2(Math.sqrt(A), Math.sqrt(1 - A));

            final double DISTANCE = EARTH_RADIUS * C;

            return DISTANCE;
        }
    }
}