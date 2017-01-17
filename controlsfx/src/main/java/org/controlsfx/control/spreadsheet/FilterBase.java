/**
 * Copyright (c) 2016 ControlsFX
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
package org.controlsfx.control.spreadsheet;

import java.util.BitSet;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.CheckBox;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.util.Callback;

/**
 * A simple implementation of the {@link Filter}. It will show all rows
 * available below it, and offer the possibility to sort and filter some row.
 */
public class FilterBase implements Filter {

    private final SpreadsheetView spv;
    private final int column;
    private MenuButton menuButton;
    private BitSet hiddenRows;
    private Set<String> stringSet = new HashSet<>();
    private Set<String> copySet = new HashSet<>();

    /**
     * Constructor for the Filter indicating on which column it's applied on.
     *
     * @param spv
     * @param column
     */
    public FilterBase(SpreadsheetView spv, int column) {
        this.spv = spv;
        this.column = column;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MenuButton getMenuButton() {
        if (menuButton == null) {
            menuButton = new MenuButton();
            menuButton.getStyleClass().add("filter-menu-button");

            menuButton.showingProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                    if (newValue) {
                        addMenuItems();
                        hiddenRows = new BitSet(spv.getHiddenRows().size());
                        hiddenRows.or(spv.getHiddenRows());
                    } else {
                        for (int i = spv.getFilteredRow() + 1; i < spv.getGrid().getRowCount(); ++i) {
                            hiddenRows.set(i, !copySet.contains(spv.getGrid().getRows().get(i).get(column).getText()));
                        }
                        spv.setHiddenRows(hiddenRows);
                    }
                }
            });
        }
        return menuButton;
    }

    private void addMenuItems() {
        if (menuButton.getItems().isEmpty()) {
            MenuItem sortItem = new MenuItem("Sort ascending");
            sortItem.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    if (spv.getComparator() == ascendingComp) {
                        spv.setComparator(descendingComp);
                        sortItem.setText("Remove sort");
                    } else if (spv.getComparator() == descendingComp) {
                        spv.setComparator(null);
                        sortItem.setText("Sort ascending");
                    } else {
                        spv.setComparator(ascendingComp);
                        sortItem.setText("Sort descending");
                    }
                }
            });

            ListView<String> listView = new ListView<>();
            listView.setCellFactory(new Callback<ListView<String>, ListCell<String>>() {
                @Override
                public ListCell<String> call(ListView<String> param) {
                    return new ListCell<String>() {
                        @Override
                        public void updateItem(String item, boolean empty) {
                            super.updateItem(item, empty);
                            setText(item);
                            if (item != null) {
                                CheckBox checkBox = new CheckBox();
                                checkBox.setSelected(copySet.contains(item));
                                checkBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
                                    @Override
                                    public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                                        if (newValue) {
                                            copySet.add(item);
                                        } else {
                                            copySet.remove(item);
                                        }
                                    }
                                });
                                setGraphic(checkBox);
                            }
                        }
                    };
                }
            });

            for (int i = spv.getFilteredRow() + 1; i < spv.getGrid().getRowCount(); ++i) {
                stringSet.add(spv.getGrid().getRows().get(i).get(column).getText());
            }
            listView.setItems(FXCollections.observableArrayList(stringSet));

            CustomMenuItem customMenuItem = new CustomMenuItem(listView);
            customMenuItem.setHideOnClick(false);
            menuButton.getItems().addAll(sortItem, customMenuItem);
        }

        copySet.clear();
        for (int i = spv.getFilteredRow() + 1; i < spv.getGrid().getRowCount(); ++i) {
            if (!spv.getHiddenRows().get(i)) {
                copySet.add(spv.getGrid().getRows().get(i).get(column).getText());
            }
        }
    }

    private final Comparator ascendingComp = new Comparator<ObservableList<SpreadsheetCell>>() {
        @Override
        public int compare(ObservableList<SpreadsheetCell> o1, ObservableList<SpreadsheetCell> o2) {
            SpreadsheetCell cell1 = o1.get(column);
            SpreadsheetCell cell2 = o2.get(column);
            if (cell1.getRow() <= spv.getFilteredRow()) {
                return Integer.compare(cell1.getRow(), cell2.getRow());
            } else if (cell2.getRow() <= spv.getFilteredRow()) {
                return Integer.compare(cell1.getRow(), cell2.getRow());
            } else if (cell1.getCellType() == SpreadsheetCellType.INTEGER && cell2.getCellType() == SpreadsheetCellType.INTEGER) {
                return Integer.compare((Integer) cell1.getItem(), (Integer) cell2.getItem());
            } else if (cell1.getCellType() == SpreadsheetCellType.DOUBLE && cell2.getCellType() == SpreadsheetCellType.DOUBLE) {
                return Double.compare((Double) cell1.getItem(), (Double) cell2.getItem());
            } else {
                return cell1.getText().compareToIgnoreCase(cell2.getText());
            }
        }
    };

    private final Comparator descendingComp = new Comparator<ObservableList<SpreadsheetCell>>() {
        @Override
        public int compare(ObservableList<SpreadsheetCell> o1, ObservableList<SpreadsheetCell> o2) {
            SpreadsheetCell cell1 = o1.get(column);
            SpreadsheetCell cell2 = o2.get(column);
            if (cell1.getRow() <= spv.getFilteredRow()) {
                return Integer.compare(cell1.getRow(), cell2.getRow());
            } else if (cell2.getRow() <= spv.getFilteredRow()) {
                return Integer.compare(cell1.getRow(), cell2.getRow());
            } else if (cell1.getCellType() == SpreadsheetCellType.INTEGER && cell2.getCellType() == SpreadsheetCellType.INTEGER) {
                return Integer.compare((Integer) cell2.getItem(), (Integer) cell1.getItem());
            } else if (cell1.getCellType() == SpreadsheetCellType.DOUBLE && cell2.getCellType() == SpreadsheetCellType.DOUBLE) {
                return Double.compare((Double) cell2.getItem(), (Double) cell1.getItem());
            } else {
                return cell2.getText().compareToIgnoreCase(cell1.getText());
            }
        }
    };
}
