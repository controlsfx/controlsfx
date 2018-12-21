/**
 * Copyright (c) 2014, 2018 ControlsFX
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *     * Neither the name of ControlsFX, any associated website, nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
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

import impl.org.controlsfx.skin.ListSelectionViewSkin;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.Cell;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Skin;
import javafx.scene.input.KeyCombination;
import javafx.util.Callback;
import org.controlsfx.control.action.Action;
import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.Glyph;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static impl.org.controlsfx.i18n.Localization.asKey;
import static impl.org.controlsfx.i18n.Localization.localize;
import static javafx.geometry.Orientation.HORIZONTAL;
import static org.controlsfx.glyphfont.FontAwesome.Glyph.*;

/**
 * A control used to perform a multi-selection via the help of two list views.
 * Items can be moved from one list (source) to the other (target). This can be
 * done by either double clicking on the list items or by using one of the
 * "move" buttons between the two lists. Buttons can be added or removed by
 * altering the {@link #getActions() list of actions}.
 * Each list can be decorated with a header and a footer node.
 * The default header nodes are simply two labels ("Available", "Selected").
 *
 * <h3>Screenshot</h3>
 *
 * <center><img src="list-selection-view.png" alt="Screenshot of ListSelectionView"></center>
 *
 * <h3>Code Example</h3>
 *
 * <pre>
 * ListSelectionView&lt;String&gt; view = new ListSelectionView&lt;&gt;();
 * view.getSourceItems().add(&quot;One&quot;, &quot;Two&quot;, &quot;Three&quot;);
 * view.getTargetItems().add(&quot;Four&quot;, &quot;Five&quot;);
 * </pre>
 *
 * @param <T>
 *            the type of the list items
 */
public class ListSelectionView<T> extends ControlsFXControl {

    private static final String DEFAULT_STYLECLASS = "list-selection-view";

    /**
     * Constructs a new dual list view.
     */
    public ListSelectionView() {
        getStyleClass().add(DEFAULT_STYLECLASS);

        Label sourceHeader = new Label(
                localize(asKey("listSelectionView.header.source")));
        sourceHeader.getStyleClass().add("list-header-label");
        sourceHeader.setId("source-header-label");
        setSourceHeader(sourceHeader);

        Label targetHeader = new Label(
                localize(asKey("listSelectionView.header.target")));
        targetHeader.getStyleClass().add("list-header-label");
        targetHeader.setId("target-header-label");
        setTargetHeader(targetHeader);
    }

    @Override
    protected Skin<ListSelectionView<T>> createDefaultSkin() {
        return new ListSelectionViewSkin<>(this);
    }

    /** {@inheritDoc} */
    @Override public String getUserAgentStylesheet() {
        return getUserAgentStylesheet(ListSelectionView.class, "listselectionview.css");
    }

    private final ObjectProperty<Node> sourceHeader = new SimpleObjectProperty<>(
            this, "sourceHeader");

    /**
     * A property used to store a reference to a node that will be displayed
     * above the source list view. The default node is a {@link Label}
     * displaying the text "Available".
     *
     * @return the property used to store the source header node
     */
    public final ObjectProperty<Node> sourceHeaderProperty() {
        return sourceHeader;
    }

    /**
     * Returns the value of {@link #sourceHeaderProperty()}.
     *
     * @return the source header node
     */
    public final Node getSourceHeader() {
        return sourceHeader.get();
    }

    /**
     * Sets the value of {@link #sourceHeaderProperty()}.
     *
     * @param node
     *            the new header node to use for the source list
     */
    public final void setSourceHeader(Node node) {
        sourceHeader.set(node);
    }

    private final ObjectProperty<Node> sourceFooter = new SimpleObjectProperty<>(
            this, "sourceFooter");

    /**
     * A property used to store a reference to a node that will be displayed
     * below the source list view. The default node is a node with two buttons
     * for easily selecting / deselecting all elements in the list view.
     *
     * @return the property used to store the source footer node
     */
    public final ObjectProperty<Node> sourceFooterProperty() {
        return sourceFooter;
    }

    /**
     * Returns the value of {@link #sourceFooterProperty()}.
     *
     * @return the source footer node
     */
    public final Node getSourceFooter() {
        return sourceFooter.get();
    }

    /**
     * Sets the value of {@link #sourceFooterProperty()}.
     *
     * @param node
     *            the new node shown below the source list
     */
    public final void setSourceFooter(Node node) {
        sourceFooter.set(node);
    }

    private final ObjectProperty<Node> targetHeader = new SimpleObjectProperty<>(
            this, "targetHeader");

    /**
     * A property used to store a reference to a node that will be displayed
     * above the target list view. The default node is a {@link Label}
     * displaying the text "Selected".
     *
     * @return the property used to store the target header node
     */
    public final ObjectProperty<Node> targetHeaderProperty() {
        return targetHeader;
    }

    /**
     * Returns the value of {@link #targetHeaderProperty()}.
     *
     * @return the source header node
     */
    public final Node getTargetHeader() {
        return targetHeader.get();
    }

    /**
     * Sets the value of {@link #targetHeaderProperty()}.
     *
     * @param node
     *            the new node shown above the target list
     */
    public final void setTargetHeader(Node node) {
        targetHeader.set(node);
    }

    private final ObjectProperty<Node> targetFooter = new SimpleObjectProperty<>(
            this, "targetFooter");

    /**
     * A property used to store a reference to a node that will be displayed
     * below the target list view. The default node is a node with two buttons
     * for easily selecting / deselecting all elements in the list view.
     *
     * @return the property used to store the source footer node
     */
    public final ObjectProperty<Node> targetFooterProperty() {
        return targetFooter;
    }

    /**
     * Returns the value of {@link #targetFooterProperty()}.
     *
     * @return the source header node
     */
    public final Node getTargetFooter() {
        return targetFooter.get();
    }

    /**
     * Sets the value of {@link #targetFooterProperty()}.
     *
     * @param node
     *            the new node shown below the target list
     */
    public final void setTargetFooter(Node node) {
        targetFooter.set(node);
    }

    private ObjectProperty<ObservableList<T>> sourceItems;

    /**
     * Sets the underlying data model for the ListView. Note that it has a
     * generic type that must match the type of the ListView itself.
     */
    public final void setSourceItems(ObservableList<T> value) {
        sourceItemsProperty().set(value);
    }

    /**
     * Returns an {@link ObservableList} that contains the items currently being
     * shown to the user in the source list. This may be null if
     * {@link #setSourceItems(javafx.collections.ObservableList)} has previously
     * been called, however, by default it is an empty ObservableList.
     *
     * @return An ObservableList containing the items to be shown to the user in
     *         the source list, or null if the items have previously been set to
     *         null.
     */
    public final ObservableList<T> getSourceItems() {
        return sourceItemsProperty().get();
    }

    /**
     * The underlying data model for the source list view. Note that it has a
     * generic type that must match the type of the source list view itself.
     */
    public final ObjectProperty<ObservableList<T>> sourceItemsProperty() {
        if (sourceItems == null) {
            sourceItems = new SimpleObjectProperty<>(this, "sourceItems",
                    FXCollections.observableArrayList());
        }
        return sourceItems;
    }

    private ObjectProperty<ObservableList<T>> targetItems;

    /**
     * Sets the underlying data model for the ListView. Note that it has a
     * generic type that must match the type of the ListView itself.
     */
    public final void setTargetItems(ObservableList<T> value) {
        targetItemsProperty().set(value);
    }

    /**
     * Returns an {@link ObservableList} that contains the items currently being
     * shown to the user in the target list. This may be null if
     * {@link #setTargetItems(javafx.collections.ObservableList)} has previously
     * been called, however, by default it is an empty ObservableList.
     *
     * @return An ObservableList containing the items to be shown to the user in
     *         the target list, or null if the items have previously been set to
     *         null.
     */
    public final ObservableList<T> getTargetItems() {
        return targetItemsProperty().get();
    }

    /**
     * The underlying data model for the target list view. Note that it has a
     * generic type that must match the type of the source list view itself.
     */
    public final ObjectProperty<ObservableList<T>> targetItemsProperty() {
        if (targetItems == null) {
            targetItems = new SimpleObjectProperty<>(this, "targetItems",
                    FXCollections.observableArrayList());
        }
        return targetItems;
    }

    // --- Orientation
    private final ObjectProperty<Orientation> orientation = new SimpleObjectProperty<>(
            this, "orientation", HORIZONTAL); //$NON-NLS-1$;

    /**
     * The {@link Orientation} of the {@code ListSelectionView} - this can
     * either be horizontal or vertical.
     */
    public final ObjectProperty<Orientation> orientationProperty() {
        return orientation;
    }

    /**
     * Sets the {@link Orientation} of the {@code ListSelectionView} - this can
     * either be horizontal or vertical.
     */
    public final void setOrientation(Orientation value) {
        orientationProperty().set(value);
    };

    /**
     * Returns the {@link Orientation} of the {@code ListSelectionView} - this
     * can either be horizontal or vertical.
     */
    public final Orientation getOrientation() {
        return orientation.get();
    }

    // --- Cell Factory
    private ObjectProperty<Callback<ListView<T>, ListCell<T>>> cellFactory;

    /**
     * Sets a new cell factory to use by both list views. This forces all old
     * {@link ListCell}'s to be thrown away, and new ListCell's created with the
     * new cell factory.
     */
    public final void setCellFactory(Callback<ListView<T>, ListCell<T>> value) {
        cellFactoryProperty().set(value);
    }

    /**
     * Returns the current cell factory.
     */
    public final Callback<ListView<T>, ListCell<T>> getCellFactory() {
        return cellFactory == null ? null : cellFactory.get();
    }

    /**
     * <p>
     * Setting a custom cell factory has the effect of deferring all cell
     * creation, allowing for total customization of the cell. Internally, the
     * ListView is responsible for reusing ListCells - all that is necessary is
     * for the custom cell factory to return from this function a ListCell which
     * might be usable for representing any item in the ListView.
     *
     * <p>
     * Refer to the {@link Cell} class documentation for more detail.
     */
    public final ObjectProperty<Callback<ListView<T>, ListCell<T>>> cellFactoryProperty() {
        if (cellFactory == null) {
            cellFactory = new SimpleObjectProperty<>(this, "cellFactory");
        }
        return cellFactory;
    }

    // -- actions
    private ObservableList<Action> actions = FXCollections.observableArrayList(new MoveToTarget(), new MoveToTargetAll(), new MoveToSource(), new MoveToSourceAll());

    /**
     * The list of actions to be shown in between the two list views.
     * All actions except, {@link org.controlsfx.control.action.ActionUtils#ACTION_SEPARATOR}
     * and {@link org.controlsfx.control.action.ActionUtils#ACTION_SPAN}, are represented as buttons.
     *
     * <p>For actions dependent on both the internal list views, an instance of {@link ListSelectionAction} should
     * be used.</p>
     *
     * <p>By default, the list has 4 actions - {@link MoveToTarget}, {@link MoveToTargetAll},
     * {@link MoveToSource} and {@link MoveToSourceAll}. A user may choose to add on top of
     * these actions or replace them depending on their use case.</p>
     *
     * @return An ObservableList of actions.
     */
    public final ObservableList<Action> getActions() {
        return actions;
    }

    // -- source actions
    private ObservableList<Action> sourceActions = FXCollections.observableArrayList();


    /**
     * These actions are shown beside the source list view. An instance of
     * {@link ListActionView.ListAction} should be used where actions are
     * dependent on the source ListView.
     *
     * @return An ObservableList of actions.
     * @see ListActionView.ListAction
     */
    public final ObservableList<Action> getSourceActions() {
        return sourceActions;
    }

    // -- target actions
    private ObservableList<Action> targetActions = FXCollections.observableArrayList();

    /**
     * These actions are shown beside the target list view. An instance of
     * {@link ListActionView.ListAction} should be used where actions are
     * dependent on the target ListView.
     *
     * @return An ObservableList of actions.
     * @see ListActionView.ListAction
     */
    public final ObservableList<Action> getTargetActions() {
        return targetActions;
    }


    /**
     * Specialized actions for ListSelectionView which get access to both the internal list views.
     * A user can add a custom action to the control by extending this class and adding its instance
     * to the {@link ListSelectionView#getActions() action list}.
     *
     * @param <T> Type of ListSelectionView to which this ListSelectionAction will be added.
     */
    public static abstract class ListSelectionAction<T> extends Action {

        /**
         * Creates a new instance of ListSelectionAction with the graphic node.
         * @param graphic Graphic to be shown in relation to this action.
         */
        public ListSelectionAction(Node graphic) {
            this(graphic, "");
        }

        /**
         * Creates a new instance of ListSelectionAction with the provided graphic and text.
         * @param graphic Graphic to be shown in relation to this action.
         * @param text The text for the Action.
         */
        public ListSelectionAction(Node graphic, String text) {
            super(text);
            setGraphic(graphic);
        }

        /**
         * Can be used to define properties or bindings for actions which are directly dependent
         * on the list views.
         * @param sourceListView The source list view
         * @param targetListView The target list view
         */
        public abstract void initialize(ListView<T> sourceListView, ListView<T> targetListView);

        @Override
        protected final void setEventHandler(Consumer<ActionEvent> eventHandler) {
            super.setEventHandler(eventHandler);
        }
    }

    /**
     * Action use to move the selected items from the
     * source list view to the target list view.
     */
    public class MoveToTarget extends ListSelectionAction<T> {

        public MoveToTarget() {
            super(getGlyph(ANGLE_RIGHT));
            getStyleClass().add("move-to-target-button");
            setAccelerator(KeyCombination.keyCombination("CTRL+RIGHT"));
            graphicProperty().bind(Bindings.createObjectBinding(() -> (getOrientation() == HORIZONTAL ? getGlyph(ANGLE_RIGHT) : getGlyph(ANGLE_DOWN)), orientationProperty()));

        }

        @Override
        public void initialize(ListView<T> sourceListView, ListView<T> targetListView) {
            disabledProperty().bind(Bindings.isEmpty(sourceListView.getSelectionModel().getSelectedItems()));
            setEventHandler(ae -> moveToTarget(sourceListView, targetListView));
        }
    }


    /**
     * Action use to move all the items from the
     * source list view to the target list view.
     */
    public class MoveToTargetAll extends ListSelectionAction<T> {

        public MoveToTargetAll() {
            super(getGlyph(ANGLE_DOUBLE_RIGHT));
            getStyleClass().add("move-to-target-all-button");
            graphicProperty().bind(Bindings.createObjectBinding(() -> (getOrientation() == HORIZONTAL ? getGlyph(ANGLE_DOUBLE_RIGHT) : getGlyph(ANGLE_DOUBLE_DOWN)), orientationProperty()));
            setAccelerator(KeyCombination.keyCombination("CTRL+SHIFT+RIGHT"));
        }

        @Override
        public void initialize(ListView<T> sourceListView, ListView<T> targetListView) {
            disabledProperty().bind(Bindings.isEmpty(sourceListView.getItems()));
            setEventHandler(ae -> moveToTargetAll(sourceListView, targetListView));
        }
    }


    /**
     * Action use to move the selected items from the
     * target list view to the source list view.
     */
    public class MoveToSource extends ListSelectionAction<T> {

        public MoveToSource() {
            super(getGlyph(ANGLE_LEFT));
            getStyleClass().add("move-to-source-button");
            graphicProperty().bind(Bindings.createObjectBinding(() -> (getOrientation() == HORIZONTAL ? getGlyph(ANGLE_LEFT) : getGlyph(ANGLE_UP)), orientationProperty()));
            setAccelerator(KeyCombination.keyCombination("CTRL+LEFT"));
        }

        @Override
        public void initialize(ListView<T> sourceListView, ListView<T> targetListView) {
            disabledProperty().bind(Bindings.isEmpty(targetListView.getSelectionModel().getSelectedItems()));
            setEventHandler(ae -> moveToSource(sourceListView, targetListView));
        }
    }


    /**
     * Action use to all the items from the
     * target list view to the source list view.
     */
    public class MoveToSourceAll extends ListSelectionAction<T> {

        public MoveToSourceAll() {
            super(getGlyph(ANGLE_DOUBLE_LEFT));
            getStyleClass().add("move-to-source-all-button");
            graphicProperty().bind(Bindings.createObjectBinding(() -> (getOrientation() == HORIZONTAL ? getGlyph(ANGLE_DOUBLE_LEFT) : getGlyph(ANGLE_DOUBLE_UP)), orientationProperty()));
            setAccelerator(KeyCombination.keyCombination("CTRL+SHIFT+LEFT"));
        }

        @Override
        public void initialize(ListView<T> sourceListView, ListView<T> targetListView) {
            disabledProperty().bind(Bindings.isEmpty(targetListView.getItems()));
            setEventHandler(ae -> moveToSourceAll(sourceListView, targetListView));
        }
    }

    private static Glyph getGlyph(FontAwesome.Glyph angleDoubleDown) {
        return new FontAwesome().create(angleDoubleDown);
    }

    private static <T> void moveToTarget(ListView<T> sourceListView, ListView<T> targetListView) {
        move(sourceListView, targetListView);
        sourceListView.getSelectionModel().clearSelection();
    }

    private static <T> void moveToTargetAll(ListView<T> sourceListView, ListView<T> targetListView) {
        move(sourceListView, targetListView, new ArrayList<>(sourceListView.getItems()));
        sourceListView.getSelectionModel().clearSelection();
    }

    private static <T> void moveToSource(ListView<T> sourceListView, ListView<T> targetListView) {
        move(targetListView, sourceListView);
        targetListView.getSelectionModel().clearSelection();
    }

    private static <T> void moveToSourceAll(ListView<T> sourceListView, ListView<T> targetListView) {
        move(targetListView, sourceListView, new ArrayList<>(targetListView.getItems()));
        targetListView.getSelectionModel().clearSelection();
    }

    private static <T> void move(ListView<T> source, ListView<T> target) {
        List<T> selectedItems = new ArrayList<>(source.getSelectionModel().getSelectedItems());
        move(source, target, selectedItems);
    }

    private static <T> void move(ListView<T> source, ListView<T> target, List<T> items) {
        source.getItems().removeAll(items);
        target.getItems().addAll(items);
    }
}
