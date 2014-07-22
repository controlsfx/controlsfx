/**
 * Copyright (c) 2014, ControlsFX
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

import static java.util.Objects.requireNonNull;
import static javafx.scene.control.SelectionMode.MULTIPLE;
import impl.org.controlsfx.skin.ListSelectionViewSkin;

import java.util.ArrayList;
import java.util.List;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Skin;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;

/**
 * A control used to perform a multi-selection via the help of two list views.
 * Items can be moved from one list (source) to the other (target). This can be
 * done by either double clicking on the list items or by using one of the
 * "move" buttons between the two lists. Each list can be decorated with a
 * header and a footer node. The default header nodes are simply two labels
 * ("Available", "Selected"). The two footer nodes are displaying two buttons
 * each for easy selection / deselection of all items. <h2>Code Example</h2>
 * 
 * <pre>
 * DualListView&lt;String&gt; view = new DualListView&lt;&gt;();
 * view.getSourceListView().getItems().add(&quot;One&quot;, &quot;Two&quot;, &quot;Three&quot;);
 * view.getTargetListView().getItems().add(&quot;Four&quot;, &quot;Five&quot;);
 * </pre>
 * 
 * @param <T>
 *            the type of the list items
 */
public class ListSelectionView<T> extends Control {
    // TODO: add an image to the javadocs

    private static final String DEFAULT_STYLECLASS = "dual-list-view";

    private ListView<T> sourceListView;

    private ListView<T> targetListView;

    /**
     * Constructs a new dual list view.
     */
    public ListSelectionView() {
        getStyleClass().add(DEFAULT_STYLECLASS);

        this.sourceListView = requireNonNull(createSourceListView(),
                "source list view can not be null");
        this.sourceListView.setId("source-list-view");

        this.targetListView = requireNonNull(createTargetListView(),
                "target list view can not be null");
        this.targetListView.setId("target-list-view");

        Label sourceHeader = new Label("Available");
        sourceHeader.getStyleClass().add("list-header-label");
        sourceHeader.setId("source-header-label");
        setSourceHeader(sourceHeader);

        Label targetHeader = new Label("Selected");
        targetHeader.getStyleClass().add("list-header-label");
        targetHeader.setId("target-header-label");
        setTargetHeader(targetHeader);

        setSourceFooter(createFooterControl(sourceListView));
        setTargetFooter(createFooterControl(targetListView));

        sourceListView.addEventHandler(MouseEvent.MOUSE_CLICKED,
                new EventHandler<MouseEvent>() {
                    public void handle(MouseEvent event) {
                        if (event.getButton() == MouseButton.PRIMARY
                                && event.getClickCount() == 2) {
                            moveToTarget();
                        }
                    };
                });

        targetListView.addEventHandler(MouseEvent.MOUSE_CLICKED,
                new EventHandler<MouseEvent>() {
                    public void handle(MouseEvent event) {
                        if (event.getButton() == MouseButton.PRIMARY
                                && event.getClickCount() == 2) {
                            moveToSource();
                        }
                    };
                });
    }

    protected Skin<ListSelectionView<T>> createDefaultSkin() {
        return new ListSelectionViewSkin<T>(this);
    }

    @Override
    protected String getUserAgentStylesheet() {
        return ListSelectionView.class.getResource("listselectionview.css")
                .toExternalForm();
    }

    /**
     * Returns the source list view (shown on the left-hand side).
     * 
     * @return the source list view
     */
    public final ListView<T> getSourceListView() {
        return sourceListView;
    }

    /**
     * Returns the target list view (shown on the right-hand side).
     * 
     * @return the target list view
     */
    public final ListView<T> getTargetListView() {
        return targetListView;
    }

    /**
     * Creates the {@link ListView} instance used on the left-hand side as the
     * source list. This method can be overridden to provide a customized list
     * view control.
     * 
     * @return the source list view
     */
    protected ListView<T> createSourceListView() {
        return createListView();
    }

    /**
     * Creates the {@link ListView} instance used on the right-hand side as the
     * target list. This method can be overridden to provide a customized list
     * view control.
     * 
     * @return the target list view
     */
    protected ListView<T> createTargetListView() {
        return createListView();
    }

    private ListView<T> createListView() {
        ListView<T> view = new ListView<>();
        view.getSelectionModel().setSelectionMode(MULTIPLE);
        return view;
    }

    private Node createFooterControl(final ListView<T> view) {
        Button selectAll = new Button("Select All");
        selectAll.getStyleClass().add("select-all-button");
        selectAll.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                view.getSelectionModel().selectAll();
            }
        });

        Button deselectAll = new Button("Deselect All");
        deselectAll.getStyleClass().add("deselect-all-button");
        deselectAll.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                view.getSelectionModel().clearSelection();
            }
        });

        HBox box = new HBox(10);
        box.getChildren().addAll(selectAll, deselectAll);

        return box;
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

    /**
     * Moves the currently selected items from the source list to the target
     * list.
     */
    public final void moveToTarget() {
        move(sourceListView, targetListView);
        sourceListView.getSelectionModel().clearSelection();
    }

    /**
     * Moves all items from the source list to the target list.
     */
    public final void moveToTargetAll() {
        move(sourceListView, targetListView,
                new ArrayList<>(sourceListView.getItems()));
        sourceListView.getSelectionModel().clearSelection();
    }

    /**
     * Moves the currently selected items from the target list to the source
     * list.
     */
    public final void moveToSource() {
        move(targetListView, sourceListView);
        targetListView.getSelectionModel().clearSelection();
    }

    /**
     * Moves all items from the target list to the source list.
     */
    public final void moveToSourceAll() {
        move(targetListView, sourceListView,
                new ArrayList<>(targetListView.getItems()));
        targetListView.getSelectionModel().clearSelection();
    }

    private void move(ListView<T> viewA, ListView<T> viewB) {
        List<T> selectedItems = new ArrayList<T>(viewA.getSelectionModel()
                .getSelectedItems());
        move(viewA, viewB, selectedItems);
    }

    private void move(ListView<T> viewA, ListView<T> viewB, List<T> items) {
        for (T item : items) {
            viewA.getItems().remove(item);
            viewB.getItems().add(item);
        }
    }
}
