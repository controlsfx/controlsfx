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
package impl.org.controlsfx.skin;

import javafx.beans.InvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.SkinBase;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.controlsfx.control.ListSelectionView;
import org.controlsfx.glyphfont.FontAwesome;

import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.requireNonNull;
import static javafx.scene.control.SelectionMode.MULTIPLE;
import static javafx.scene.input.MouseEvent.MOUSE_CLICKED;

public class ListSelectionViewSkin<T> extends SkinBase<ListSelectionView<T>> {

    private GridPane gridPane;
    private final HBox horizontalButtonBox;
    private final VBox verticalButtonBox;
    private Button moveToTarget;
    private Button moveToTargetAll;
    private Button moveToSourceAll;
    private Button moveToSource;
    private ListView<T> sourceListView;
    private ListView<T> targetListView;

    public ListSelectionViewSkin(ListSelectionView<T> view) {
        super(view);

        sourceListView = requireNonNull(createSourceListView(),
                "source list view can not be null");
        sourceListView.setId("source-list-view");
        sourceListView.setItems(view.getSourceItems());

        targetListView = requireNonNull(createTargetListView(),
                "target list view can not be null");
        targetListView.setId("target-list-view");
        targetListView.setItems(view.getTargetItems());

        sourceListView.cellFactoryProperty().bind(view.cellFactoryProperty());
        targetListView.cellFactoryProperty().bind(view.cellFactoryProperty());

        gridPane = createGridPane();
        horizontalButtonBox = createHorizontalButtonBox();
        verticalButtonBox = createVerticalButtonBox();

        getChildren().add(gridPane);

        InvalidationListener updateListener = o -> updateView();

        view.sourceHeaderProperty().addListener(updateListener);
        view.sourceFooterProperty().addListener(updateListener);
        view.targetHeaderProperty().addListener(updateListener);
        view.targetFooterProperty().addListener(updateListener);

        updateView();

        getSourceListView().addEventHandler(MOUSE_CLICKED, event -> {
            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                moveToTarget();
            }
        });

        getTargetListView().addEventHandler(MOUSE_CLICKED, event -> {
            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                moveToSource();
            }
        });

        view.orientationProperty().addListener(observable -> updateView());
    }

    private GridPane createGridPane() {
        GridPane gridPane = new GridPane();
        gridPane.getStyleClass().add("grid-pane");
        return gridPane;
    }

    // Constraints used when view's orientation is HORIZONTAL
    private void setHorizontalViewConstraints() {
        gridPane.getColumnConstraints().clear();
        gridPane.getRowConstraints().clear();

        ColumnConstraints col1 = new ColumnConstraints();

        col1.setFillWidth(true);
        col1.setHgrow(Priority.ALWAYS);
        col1.setMaxWidth(Double.MAX_VALUE);
        col1.setPrefWidth(200);

        ColumnConstraints col2 = new ColumnConstraints();
        col2.setFillWidth(true);
        col2.setHgrow(Priority.NEVER);

        ColumnConstraints col3 = new ColumnConstraints();
        col3.setFillWidth(true);
        col3.setHgrow(Priority.ALWAYS);
        col3.setMaxWidth(Double.MAX_VALUE);
        col3.setPrefWidth(200);

        gridPane.getColumnConstraints().addAll(col1, col2, col3);

        RowConstraints row1 = new RowConstraints();
        row1.setFillHeight(true);
        row1.setVgrow(Priority.NEVER);

        RowConstraints row2 = new RowConstraints();
        row2.setMaxHeight(Double.MAX_VALUE);
        row2.setPrefHeight(200);
        row2.setVgrow(Priority.ALWAYS);

        RowConstraints row3 = new RowConstraints();
        row3.setFillHeight(true);
        row3.setVgrow(Priority.NEVER);

        gridPane.getRowConstraints().addAll(row1, row2, row3);
    }

    // Constraints used when view's orientation is VERTICAL
    private void setVerticalViewConstraints() {
        gridPane.getColumnConstraints().clear();
        gridPane.getRowConstraints().clear();

        ColumnConstraints col1 = new ColumnConstraints();

        col1.setFillWidth(true);
        col1.setHgrow(Priority.ALWAYS);
        col1.setMaxWidth(Double.MAX_VALUE);
        col1.setPrefWidth(200);

        gridPane.getColumnConstraints().addAll(col1);

        RowConstraints row1 = new RowConstraints();
        row1.setFillHeight(true);
        row1.setVgrow(Priority.NEVER);

        RowConstraints row2 = new RowConstraints();
        row2.setMaxHeight(Double.MAX_VALUE);
        row2.setPrefHeight(200);
        row2.setVgrow(Priority.ALWAYS);

        RowConstraints row3 = new RowConstraints();
        row3.setFillHeight(true);
        row3.setVgrow(Priority.NEVER);

        RowConstraints row4 = new RowConstraints();
        row4.setFillHeight(true);
        row4.setVgrow(Priority.NEVER);

        RowConstraints row5 = new RowConstraints();
        row5.setFillHeight(true);
        row5.setVgrow(Priority.NEVER);

        RowConstraints row6 = new RowConstraints();
        row6.setMaxHeight(Double.MAX_VALUE);
        row6.setPrefHeight(200);
        row6.setVgrow(Priority.ALWAYS);

        RowConstraints row7 = new RowConstraints();
        row7.setFillHeight(true);
        row7.setVgrow(Priority.NEVER);


        gridPane.getRowConstraints().addAll(row1, row2, row3, row4, row5, row6, row7);
    }

    // Used when view's orientation is HORIZONTAL
    private VBox createVerticalButtonBox() {
        VBox box = new VBox(5);
        box.setFillWidth(true);

        FontAwesome fontAwesome = new FontAwesome();
        moveToTarget = new Button("",
                fontAwesome.create(FontAwesome.Glyph.ANGLE_RIGHT));
        moveToTargetAll = new Button("",
                fontAwesome.create(FontAwesome.Glyph.ANGLE_DOUBLE_RIGHT));

        moveToSource = new Button("",
                fontAwesome.create(FontAwesome.Glyph.ANGLE_LEFT));
        moveToSourceAll = new Button("",
                fontAwesome.create(FontAwesome.Glyph.ANGLE_DOUBLE_LEFT));

        updateButtons();
        box.getChildren().addAll(moveToTarget, moveToTargetAll, moveToSource, moveToSourceAll);
        return box;
    }

    // Used when view's orientation is VERTICAL
    private HBox createHorizontalButtonBox() {
        HBox box = new HBox(5);
        box.setFillHeight(true);

        FontAwesome fontAwesome = new FontAwesome();
        moveToTarget = new Button("",
                fontAwesome.create(FontAwesome.Glyph.ANGLE_DOWN));
        moveToTargetAll = new Button("",
                fontAwesome.create(FontAwesome.Glyph.ANGLE_DOUBLE_DOWN));

        moveToSource = new Button("",
                fontAwesome.create(FontAwesome.Glyph.ANGLE_UP));
        moveToSourceAll = new Button("",
                fontAwesome.create(FontAwesome.Glyph.ANGLE_DOUBLE_UP));

        updateButtons();
        box.getChildren().addAll(moveToTarget, moveToTargetAll, moveToSource, moveToSourceAll);
        return box;
    }

    private void updateButtons() {

        moveToTarget.getStyleClass().add("move-to-target-button");
        moveToTargetAll.getStyleClass().add("move-to-target-all-button");
        moveToSource.getStyleClass().add("move-to-source-button");
        moveToSourceAll.getStyleClass().add("move-to-source-all-button");

        moveToTarget.setMaxWidth(Double.MAX_VALUE);
        moveToTargetAll.setMaxWidth(Double.MAX_VALUE);
        moveToSource.setMaxWidth(Double.MAX_VALUE);
        moveToSourceAll.setMaxWidth(Double.MAX_VALUE);

        getSourceListView().itemsProperty().addListener(
                it -> bindMoveAllButtonsToDataModel());

        getTargetListView().itemsProperty().addListener(
                it -> bindMoveAllButtonsToDataModel());

        getSourceListView().selectionModelProperty().addListener(
                it -> bindMoveButtonsToSelectionModel());

        getTargetListView().selectionModelProperty().addListener(
                it -> bindMoveButtonsToSelectionModel());

        bindMoveButtonsToSelectionModel();
        bindMoveAllButtonsToDataModel();

        moveToTarget.setOnAction(evt -> moveToTarget());
        moveToTargetAll.setOnAction(evt -> moveToTargetAll());
        moveToSource.setOnAction(evt -> moveToSource());
        moveToSourceAll.setOnAction(evt -> moveToSourceAll());
    }

    private void bindMoveAllButtonsToDataModel() {
        moveToTargetAll.disableProperty().bind(
                Bindings.isEmpty(getSourceListView().getItems()));

        moveToSourceAll.disableProperty().bind(
                Bindings.isEmpty(getTargetListView().getItems()));
    }

    private void bindMoveButtonsToSelectionModel() {
        moveToTarget.disableProperty().bind(
                Bindings.isEmpty(getSourceListView().getSelectionModel()
                        .getSelectedItems()));

        moveToSource.disableProperty().bind(
                Bindings.isEmpty(getTargetListView().getSelectionModel()
                        .getSelectedItems()));
    }

    private void updateView() {
        gridPane.getChildren().clear();

        Node sourceHeader = getSkinnable().getSourceHeader();
        Node targetHeader = getSkinnable().getTargetHeader();
        Node sourceFooter = getSkinnable().getSourceFooter();
        Node targetFooter = getSkinnable().getTargetFooter();

        ListView<T> sourceList = getSourceListView();
        ListView<T> targetList = getTargetListView();

        StackPane stackPane = new StackPane();
        stackPane.setAlignment(Pos.CENTER);

        Orientation orientation = getSkinnable().getOrientation();

        if (orientation == Orientation.HORIZONTAL) {
            setHorizontalViewConstraints();

            if (sourceHeader != null) {
                gridPane.add(sourceHeader, 0, 0);
            }

            if (targetHeader != null) {
                gridPane.add(targetHeader, 2, 0);
            }

            if (sourceList != null) {
                gridPane.add(sourceList, 0, 1);
            }

            if (targetList != null) {
                gridPane.add(targetList, 2, 1);
            }

            if (sourceFooter != null) {
                gridPane.add(sourceFooter, 0, 2);
            }

            if (targetFooter != null) {
                gridPane.add(targetFooter, 2, 2);
            }

            stackPane.getChildren().add(verticalButtonBox);
            gridPane.add(stackPane, 1, 1);
        } else {
            setVerticalViewConstraints();

            if (sourceHeader != null) {
                gridPane.add(sourceHeader, 0, 0);
            }

            if (targetHeader != null) {
                gridPane.add(targetHeader, 0, 4);
            }

            if (sourceList != null) {
                gridPane.add(sourceList, 0, 1);
            }

            if (targetList != null) {
                gridPane.add(targetList, 0, 5);
            }

            if (sourceFooter != null) {
                gridPane.add(sourceFooter, 0, 2);
            }

            if (targetFooter != null) {
                gridPane.add(targetFooter, 0, 6);
            }

            stackPane.getChildren().add(horizontalButtonBox);
            gridPane.add(stackPane, 0, 3);
        }
    }

    private void moveToTarget() {
        move(getSourceListView(), getTargetListView());
        getSourceListView().getSelectionModel().clearSelection();
    }

    private void moveToTargetAll() {
        move(getSourceListView(), getTargetListView(), new ArrayList<>(
                getSourceListView().getItems()));
        getSourceListView().getSelectionModel().clearSelection();
    }

    private void moveToSource() {
        move(getTargetListView(), getSourceListView());
        getTargetListView().getSelectionModel().clearSelection();
    }

    private void moveToSourceAll() {
        move(getTargetListView(), getSourceListView(), new ArrayList<>(
                getTargetListView().getItems()));
        getTargetListView().getSelectionModel().clearSelection();
    }

    private void move(ListView<T> viewA, ListView<T> viewB) {
        List<T> selectedItems = new ArrayList<>(viewA.getSelectionModel()
                .getSelectedItems());
        move(viewA, viewB, selectedItems);
    }

    private void move(ListView<T> viewA, ListView<T> viewB, List<T> items) {
        viewA.getItems().removeAll(items);
        viewB.getItems().addAll(items);
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
}
