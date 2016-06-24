/**
 * Copyright (c) 2015, 2016, ControlsFX
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
package impl.org.controlsfx.table;

import com.sun.javafx.scene.control.skin.NestedTableColumnHeader;
import com.sun.javafx.scene.control.skin.TableColumnHeader;
import com.sun.javafx.scene.control.skin.TableViewSkin;
import javafx.beans.Observable;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.Optional;
import java.util.function.Supplier;


public final class FilterPanel<T,R> extends VBox {

    private final ColumnFilter<T,R> columnFilter;

    private final FilteredList<FilterValue> filterList;
    private static final String promptText = "Search...";
    private final TextField searchBox = new TextField();
    private boolean searchMode = false;
    private boolean bumpedWidth = false;

    private final ListView<FilterValue> checkListView;

    private static final Image filterIcon = new Image("/impl/org/controlsfx/table/filter.png");

    private static final Supplier<ImageView> filterImageView = () -> {
        ImageView imageView = new ImageView(filterIcon);
        imageView.setFitHeight(15);
        imageView.setPreserveRatio(true);
        return imageView;
    };

    void selectAllValues() {
        checkListView.getItems().stream()
                .forEach(item -> item.selectedProperty().set(true));
    }
    void unSelectAllValues() {
        checkListView.getItems().stream()
                .forEach(item -> item.selectedProperty().set(false));
    }
    void selectValue(Object value) {
        checkListView.getItems().stream().filter(item -> item.getValue().equals(value))
                .forEach(item -> item.selectedProperty().set(true));
    }
    void unSelectValue(Object value) {
        checkListView.getItems().stream().filter(item -> item.getValue() == value)
                .forEach(item -> item.selectedProperty().set(false));
    }

    FilterPanel(ColumnFilter<T,R> columnFilter, ContextMenu contextMenu) {
        columnFilter.setFilterPanel(this);
        this.columnFilter = columnFilter;
        getStyleClass().add("filter-panel");

        //initialize search box
        setPadding(new Insets(3));

        searchBox.setPromptText(promptText);
        getChildren().add(searchBox);

        //initialize checklist view

        filterList = new FilteredList<>(new SortedList<>(columnFilter.getFilterValues()), t -> true);
        checkListView = new ListView<>();
        checkListView.setItems(new SortedList<>(filterList, FilterValue::compareTo));

        getChildren().add(checkListView);

        //initialize apply button
        HBox buttonBox = new HBox();

        Button applyBttn = new Button("APPLY");
        HBox.setHgrow(applyBttn, Priority.ALWAYS);

        applyBttn.setOnAction(e -> {
                    if (searchMode) {
                        filterList.forEach(v -> v.selectedProperty().setValue(true));

                        columnFilter.getFilterValues().stream()
                                .filter(v -> !filterList.stream().filter(fl -> fl.equals(v)).findAny().isPresent())
                                .forEach(v -> v.selectedProperty().setValue(false));

                        resetSearchFilter();
                    }
                    if (columnFilter.getTableFilter().isDirty()) {
                        columnFilter.applyFilter();
                        columnFilter.getTableFilter().getColumnFilters().stream().map(ColumnFilter::getFilterPanel)
                                .forEach(fp -> {
                                    if (!fp.columnFilter.hasUnselections()) {
                                        fp.columnFilter.getTableColumn().setGraphic(null);
                                    } else {
                                        fp.columnFilter.getTableColumn().setGraphic(filterImageView.get());
                                        if (!bumpedWidth) {
                                            fp.columnFilter.getTableColumn().setPrefWidth(columnFilter.getTableColumn().getWidth() + 20);
                                            bumpedWidth = true;
                                        }
                                    }
                                });
                    }
                contextMenu.hide();
                });

        buttonBox.getChildren().add(applyBttn);

        //initialize unselect all button
        Button unselectAllButton = new Button("NONE");
        HBox.setHgrow(unselectAllButton, Priority.ALWAYS);

        unselectAllButton.setOnAction(e -> columnFilter.getFilterValues().forEach(v -> v.selectedProperty().set(false)));
        buttonBox.getChildren().add(unselectAllButton);

        //initialize reset buttons
        Button selectAllButton = new Button("ALL");
        HBox.setHgrow(selectAllButton, Priority.ALWAYS);

        selectAllButton.setOnAction(e -> {
            columnFilter.getFilterValues().forEach(v -> v.selectedProperty().set(true));
        });

        buttonBox.getChildren().add(selectAllButton);

        Button clearAllButton = new Button("RESET ALL");
        HBox.setHgrow(clearAllButton, Priority.ALWAYS);

        clearAllButton.setOnAction(e -> {
            columnFilter.resetAllFilters();
            columnFilter.getTableFilter().getColumnFilters().stream().forEach(cf -> cf.getTableColumn().setGraphic(null));
            contextMenu.hide();
        });
        buttonBox.getChildren().add(clearAllButton);

        buttonBox.setAlignment(Pos.BASELINE_CENTER);


        getChildren().add(buttonBox);
    }

    public void resetSearchFilter() {
        this.filterList.setPredicate(t -> true);
        searchBox.clear();
    }
    public static <T,R> CustomMenuItem getInMenuItem(ColumnFilter<T,R> columnFilter, ContextMenu contextMenu) {

        FilterPanel<T,R> filterPanel = new FilterPanel<>(columnFilter, contextMenu);

        CustomMenuItem menuItem = new CustomMenuItem();

        filterPanel.initializeListeners();

        menuItem.contentProperty().set(filterPanel);

        columnFilter.getTableFilter().getTableView().skinProperty().addListener((w, o, n) -> {
            if (n instanceof TableViewSkin) {
                TableViewSkin<?> skin = (TableViewSkin<?>) n;
                checkChangeContextMenu(skin, columnFilter.getTableColumn());
            }
        });
        menuItem.setHideOnClick(false);
        return menuItem;
    }
    private void initializeListeners() {
        searchBox.textProperty().addListener(l -> {
            searchMode = !searchBox.getText().isEmpty();
            filterList.setPredicate(val -> searchBox.getText().isEmpty() ||
                    columnFilter.getSearchStrategy().test(searchBox.getText(), Optional.ofNullable(val.getValue()).map(Object::toString).orElse("")));
        });
    }

    /* Methods below helps will anchor the context menu under the column */
    private static void checkChangeContextMenu(TableViewSkin<?> skin, TableColumn<?, ?> column) {
        NestedTableColumnHeader header = skin.getTableHeaderRow()
                .getRootHeader();
        header.getColumnHeaders().addListener((Observable obs) -> changeContextMenu(header,column));
        changeContextMenu(header, column);
    }

    private static void changeContextMenu(NestedTableColumnHeader header, TableColumn<?, ?> column) {
        TableColumnHeader headerSkin = scan(column, header);
        if (headerSkin != null) {
            headerSkin.setOnContextMenuRequested(ev -> {
                ContextMenu cMenu = column.getContextMenu();
                if (cMenu != null) {
                    cMenu.show(headerSkin, Side.BOTTOM, 5, 5);
                }
                ev.consume();
            });
        }
    }

    private static TableColumnHeader scan(TableColumn<?, ?> search,
                                          TableColumnHeader header) {
        // firstly test that the parent isn't what we are looking for
        if (search.equals(header.getTableColumn())) {
            return header;
        }

        if (header instanceof NestedTableColumnHeader) {
            NestedTableColumnHeader parent = (NestedTableColumnHeader) header;
            for (int i = 0; i < parent.getColumnHeaders().size(); i++) {
                TableColumnHeader result = scan(search, parent
                        .getColumnHeaders().get(i));
                if (result != null) {
                    return result;
                }
            }
        }

        return null;
    }
}
