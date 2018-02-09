/**
 * Copyright (c) 2018 ControlsFX
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

import impl.org.controlsfx.skin.ListActionViewSkin;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.Cell;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Skin;
import javafx.util.Callback;
import org.controlsfx.control.action.Action;

import java.util.function.Consumer;

/**
 * A control used to perform actions on a ListView. Actions can be
 * added by accessing the {@link #getActions() action list}. These actions are
 * represented as buttons on the control. These buttons can be moved to any side
 * of the ListView using the {@link #sideProperty()}.
 *
 * <h3>Screenshot</h3>
 *
 * <center><img src="list-action-view.png" alt="Screenshot of ListActionView"></center>
 *
 * <h3>Code Example</h3>
 *
 * <pre>
 *     ListActionView<String> view = new ListActionView<>();
 *     view.getItems().add("One", "Two", "Three");
 *     view.getActions().add(new ListActionView.ListAction<String>() {
 *         {
 *             setGraphic(new FontAwesome().create(FontAwesome.Glyph.BOLT));
 *         }
 *
 *        {@literal @}Override
 *         public void initialize(ListView<String> listView) {
 *             setEventHandler(e -> System.out.println("Action fired!"));
 *         }
 *     });
 *     view.getActions().add(ActionUtils.ACTION_SEPARATOR);
 * </pre>
 *
 * @param <T> Type of ListActionView.
 */
public class ListActionView<T> extends ControlsFXControl {

    private static final String DEFAULT_STYLE = "list-action-view";

    public ListActionView() {
        getStyleClass().add(DEFAULT_STYLE);
    }

    // -- items
    private final ObservableList<T> items = FXCollections.observableArrayList();

    /**
     * The list of items for the list view.
     *
     * @return An ObservableList of T.
     */
    public final ObservableList<T> getItems() {
        return items;
    }

    // -- actions
    private final ObservableList<Action> actions = FXCollections.observableArrayList();

    /**
     * The list of actions shown on one of the sides of the ListView.
     * All actions except, {@link org.controlsfx.control.action.ActionUtils#ACTION_SEPARATOR}
     * and {@link org.controlsfx.control.action.ActionUtils#ACTION_SPAN}, are represented as buttons.
     *
     * <p>For actions dependent on the internal ListView, an instance of {@link ListAction} should
     * be used.</p>
     *
     * @return An ObservableList of actions.
     */
    public final ObservableList<Action> getActions() {
        return actions;
    }

    // -- side
    private ObjectProperty<Side> side;

    /**
     * The current position of the action buttons in the ListActionView.
     *
     * @defaultValue {@link Side#LEFT}
     * @return The current position of the action buttons in the ListActionView.
     */
    public Side getSide() {
        return side == null ? null : side.get();
    }

    /**
     * The position of the action buttons in the ListActionView.
     */
    public ObjectProperty<Side> sideProperty() {
        if (side == null) {
            side = new SimpleObjectProperty<>(this, "side", Side.LEFT);
        }
        return side;
    }

    /**
     * The position to place the action buttons in this ListActionView.
     * Whenever this changes the ListActionView will immediately update the
     * location of the action buttons to reflect the same.
     *
     * @param side The side to place the action buttons.
     */
    public void setSide(Side side) {
        this.side.set(side);
    }

    // -- Cell Factory
    private ObjectProperty<Callback<ListView<T>, ListCell<T>>> cellFactory;

    /**
     * Sets a new cell factory for the list view. This forces all old
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
     * Setting a custom cell factory has the effect of deferring all cell
     * creation, allowing for total customization of the cell. Internally, the
     * ListView is responsible for reusing ListCells - all that is necessary is
     * for the custom cell factory to return from this function a ListCell which
     * might be usable for representing any item in the ListView.
     *
     * <p>Refer to the {@link Cell} class documentation for more detail.</p>
     */
    public final ObjectProperty<Callback<ListView<T>, ListCell<T>>> cellFactoryProperty() {
        if (cellFactory == null) {
            cellFactory = new SimpleObjectProperty<>(this, "cellFactory");
        }
        return cellFactory;
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new ListActionViewSkin<>(this);
    }

    @Override
    public String getUserAgentStylesheet() {
        return getUserAgentStylesheet(ListActionView.class, "listactionview.css");
    }

    /**
     * Specialized actions for ListActionView which get access to the internal ListView. A user can add a custom action to the
     * control by extending this class and adding its instance to the {@link ListActionView#getActions() action list}.
     *
     * @param <T> Type of ListActionView to which this ListAction will be added.
     */
    public static abstract class ListAction<T> extends Action {

        /**
         * Creates a new instance of ListAction with the graphic node.
         * @param graphic Graphic to be shown in relation to this action.
         */
        public ListAction(Node graphic) {
            this(graphic, "");
        }

        /**
         * Creates a new instance of ListAction with the provided graphic and text.
         * @param graphic Graphic to be shown in relation to this action.
         * @param text The text for the Action.
         */
        public ListAction(Node graphic, String text) {
            super(text);
            setGraphic(graphic);
        }

        /**
         * Can be used to define properties or bindings for actions
         * which are directly dependent on the list view.
         * @param listView The list view
         */
        public abstract void initialize(ListView<T> listView);

        @Override
        protected final void setEventHandler(Consumer<ActionEvent> eventHandler) {
            super.setEventHandler(eventHandler);
        }
    }
}
