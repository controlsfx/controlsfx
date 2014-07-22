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
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import org.controlsfx.control.ListSelectionView;
import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.GlyphFont;
import org.controlsfx.glyphfont.GlyphFontRegistry;

public class ListSelectionViewSkin<T> extends SkinBase<ListSelectionView<T>> {
    private GridPane gridPane;
    private VBox buttonBox;
    private Button moveToTarget;
    private Button moveToTargetAll;
    private Button moveToSourceAll;
    private Button moveToSource;
    private GlyphFont fontAwesome;

    public ListSelectionViewSkin(ListSelectionView<T> view) {
        super(view);

        this.fontAwesome = GlyphFontRegistry.font("FontAwesome");

        gridPane = createGridPane();
        buttonBox = createButtonBox();

        getChildren().add(gridPane);

        InvalidationListener updateListener = new InvalidationListener() {

            @Override
            public void invalidated(Observable observable) {
                updateView();
            }
        };

        view.sourceHeaderProperty().addListener(updateListener);
        view.sourceFooterProperty().addListener(updateListener);
        view.targetHeaderProperty().addListener(updateListener);
        view.targetFooterProperty().addListener(updateListener);

        updateView();
    }

    private GridPane createGridPane() {
        GridPane gridPane = new GridPane();
        gridPane.getStyleClass().add("grid-pane");

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

        return gridPane;
    }

    private VBox createButtonBox() {
        VBox box = new VBox(5);
        box.setFillWidth(true);

        moveToTarget = new Button("", FontAwesome.Glyph.ANGLE_RIGHT.create());
        moveToTargetAll = new Button("",
                FontAwesome.Glyph.DOUBLE_ANGLE_RIGHT.create());

        moveToSource = new Button("", FontAwesome.Glyph.ANGLE_LEFT.create());
        moveToSourceAll = new Button("",
                FontAwesome.Glyph.DOUBLE_ANGLE_LEFT.create());

        moveToTarget.getStyleClass().add("move-to-target-button");
        moveToTargetAll.getStyleClass().add("move-to-target-all-button");
        moveToSource.getStyleClass().add("move-to-source-button");
        moveToSourceAll.getStyleClass().add("move-to-source-all-button");

        moveToTarget.setMaxWidth(Double.MAX_VALUE);
        moveToTargetAll.setMaxWidth(Double.MAX_VALUE);
        moveToSource.setMaxWidth(Double.MAX_VALUE);
        moveToSourceAll.setMaxWidth(Double.MAX_VALUE);

        getSkinnable().getSourceListView().itemsProperty()
                .addListener(new InvalidationListener() {

                    @Override
                    public void invalidated(Observable observable) {
                        bindMoveAllButtonsToDataModel();
                    }
                });

        getSkinnable().getTargetListView().itemsProperty()
                .addListener(new InvalidationListener() {

                    @Override
                    public void invalidated(Observable observable) {
                        bindMoveAllButtonsToDataModel();
                    }
                });

        getSkinnable().getSourceListView().selectionModelProperty()
                .addListener(new InvalidationListener() {

                    @Override
                    public void invalidated(Observable observable) {
                        bindMoveButtonsToSelectionModel();
                    }
                });

        getSkinnable().getTargetListView().selectionModelProperty()
                .addListener(new InvalidationListener() {

                    @Override
                    public void invalidated(Observable observable) {
                        bindMoveButtonsToSelectionModel();
                    }
                });

        bindMoveButtonsToSelectionModel();
        bindMoveAllButtonsToDataModel();

        moveToTarget.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                getSkinnable().moveToTarget();
            }
        });

        moveToTargetAll.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                getSkinnable().moveToTargetAll();
            }
        });

        moveToSource.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                getSkinnable().moveToSource();
            }
        });

        moveToSourceAll.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                getSkinnable().moveToSourceAll();
            }
        });

        box.getChildren().addAll(moveToTarget, moveToTargetAll, moveToSource,
                moveToSourceAll);

        return box;
    }

    private void bindMoveAllButtonsToDataModel() {
        moveToTargetAll.disableProperty()
                .bind(Bindings.isEmpty(getSkinnable().getSourceListView()
                        .getItems()));

        moveToSourceAll.disableProperty()
                .bind(Bindings.isEmpty(getSkinnable().getTargetListView()
                        .getItems()));
    }

    private void bindMoveButtonsToSelectionModel() {
        moveToTarget.disableProperty().bind(
                Bindings.isEmpty(getSkinnable().getSourceListView()
                        .getSelectionModel().getSelectedItems()));

        moveToSource.disableProperty().bind(
                Bindings.isEmpty(getSkinnable().getTargetListView()
                        .getSelectionModel().getSelectedItems()));
    }

    private void updateView() {
        gridPane.getChildren().clear();

        Node sourceHeader = getSkinnable().getSourceHeader();
        Node targetHeader = getSkinnable().getTargetHeader();
        Node sourceFooter = getSkinnable().getSourceFooter();
        Node targetFooter = getSkinnable().getTargetFooter();

        ListView<T> sourceList = getSkinnable().getSourceListView();
        ListView<T> targetList = getSkinnable().getTargetListView();

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

        StackPane stackPane = new StackPane();
        stackPane.setAlignment(Pos.CENTER);
        stackPane.getChildren().add(buttonBox);

        gridPane.add(stackPane, 1, 1);
    }
}
