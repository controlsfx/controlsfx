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
import impl.org.controlsfx.table.ColumnFilter.FilterValue;
import javafx.beans.Observable;
import javafx.beans.value.ObservableValue;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.util.Comparator;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;


public final class FilterPanel<T> extends VBox {
    
    private final ColumnFilter<T> columnFilter;

    private final FilteredList<CheckItem> filterList;
    private static final String promptText = "Search...";
    private final TextField searchBox = new TextField();
    private boolean searchMode = false;
    private boolean bumpedWidth = false;

    private static final Image filterIcon = new Image("/impl/org/controlsfx/table/filter.png");

    private static final Supplier<ImageView> filterImageView = () -> {
        ImageView imageView = new ImageView(filterIcon);
        imageView.setFitHeight(15);
        imageView.setPreserveRatio(true);
        return imageView;
    };

    FilterPanel(ColumnFilter<T> columnFilter) {
        this.columnFilter = columnFilter;


        //initialize search box
        this.setPadding(new Insets(3));
        
        searchBox.setPromptText(promptText);
        this.getChildren().add(searchBox);

        //initialize checklist view
        Function<ColumnFilter.FilterValue,CheckItem>  newCheckItemFx = v -> {
            CheckItem chkItem = new CheckItem(v);
            chkItem.checkBox.selectedProperty().setValue(true);
            return chkItem;
        };

        filterList = new FilteredList<>(new SortedList<>(new MappedList<>(new SortedList<>(this.columnFilter.getFilterValues(), new FilterValueComparator()), newCheckItemFx)), t -> true);
        ListView<CheckItem> checkListView = new ListView<>();
        checkListView.setItems(filterList);

        this.getChildren().add(checkListView);
        
        //initialize apply button
        HBox bttnBox = new HBox();
        Button applyBttn = new Button("APPLY");

        HBox.setHgrow(bttnBox, Priority.ALWAYS);
        applyBttn.setOnAction(e -> {
        	if (searchMode) { 
        		filterList.forEach(v -> v.filterValue.getSelectedProperty().setValue(true));
        		
        		columnFilter.getFilterValues().stream()
        			.filter(v -> !filterList.stream().filter(fl -> fl.filterValue.equals(v)).findAny().isPresent())
        			.forEach(v -> v.getSelectedProperty().setValue(false));
        		
        		resetSearchFilter();
        	}
            if (columnFilter.getFilterValues().stream().filter(v -> v.getSelectedProperty().get()).findAny().isPresent()) {
                columnFilter.applyFilter();
                columnFilter.getTableColumn().setGraphic(filterImageView.get());
                if (!bumpedWidth) {
                    columnFilter.getTableColumn().setPrefWidth(columnFilter.getTableColumn().getWidth() + 15);
                    bumpedWidth = true;
                }
            }
            else {
                resetSearchFilter();
            }
        });
        
        bttnBox.getChildren().add(applyBttn);
        

        //initialize unselect all button
        Button unselectAllButton = new Button("NONE");
        HBox.setHgrow(unselectAllButton, Priority.ALWAYS);
        unselectAllButton.setOnAction(e -> {
            columnFilter.getFilterValues().forEach(v -> v.getSelectedProperty().set(false));
        });
        bttnBox.getChildren().add(unselectAllButton);

        //initialize reset buttons
        Button clearButton = new Button("ALL");
        HBox.setHgrow(clearButton, Priority.ALWAYS);

        clearButton.setOnAction(e -> {
            columnFilter.resetAllFilters();
            filterList.setPredicate(v -> true);
        });

        bttnBox.getChildren().add(clearButton);

        Button clearAllButton = new Button("RESET ALL");
        HBox.setHgrow(clearAllButton, Priority.ALWAYS);

        clearAllButton.setOnAction(e -> {
            columnFilter.resetAllFilters();
            columnFilter.getTableFilter().getColumnFilters().stream().forEach(cf -> cf.getTableColumn().setGraphic(null));
        });
        bttnBox.getChildren().add(clearAllButton);
        bttnBox.setAlignment(Pos.BASELINE_CENTER);

        this.getChildren().add(bttnBox);
        //this.getChildren().add(vBox);
    }
    private static final class CheckItem extends HBox {
        private final CheckBox checkBox = new CheckBox();
        private final Label label = new Label();
        private final FilterValue filterValue;
        
        CheckItem(ColumnFilter.FilterValue filterValue) {
        	this.filterValue = filterValue;
            label.setText(Optional.ofNullable(filterValue.getValueProperty()).map(ObservableValue::getValue).map(Object::toString).orElse(null));
            
            filterValue.getInScopeProperty().addListener((Observable v) -> label.textFillProperty().set(filterValue.getInScopeProperty().get() ? Color.BLACK : Color.LIGHTGRAY));
            checkBox.selectedProperty().bindBidirectional(filterValue.getSelectedProperty());
            this.getChildren().addAll(checkBox, label);
        }
    }
    private static final class FilterValueComparator implements Comparator<FilterValue> {

		@Override
		public int compare(FilterValue first, FilterValue second) {
			if (first.getInScopeProperty().get() && !second.getInScopeProperty().get())
				return 1;

			int compare = Optional.ofNullable(first.getValueProperty().getValue()).map(Object::toString).orElse("")
                    .compareTo(Optional.ofNullable(second.getValueProperty().getValue()).map(Object::toString).orElse(""));

			if (compare > 0) 
				return 1;
			if (compare < 0) 
				return -1;
			return 0;
		}
    	
    }
    public void resetSearchFilter() {
        this.filterList.setPredicate(t -> true);
        searchBox.clear();
    }
    public static <T> CustomMenuItem getInMenuItem(ColumnFilter<T> columnFilter) { 
        
        FilterPanel<T> filterPanel = new FilterPanel<>(columnFilter);

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
        menuItem.getStyleClass().add("filter-menu-item");
        return menuItem;
    }
    private void initializeListeners() { 
        searchBox.textProperty().addListener(l -> {
        	searchMode = !searchBox.getText().isEmpty();
        	filterList.setPredicate(val -> searchBox.getText().isEmpty() || columnFilter.getSearchStrategy().test(searchBox.getText(), val.filterValue.toString()));
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
