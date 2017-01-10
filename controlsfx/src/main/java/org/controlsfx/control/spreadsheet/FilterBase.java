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
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.control.CheckBox;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuButton;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.util.Callback;

public class FilterBase implements Filter {

    private final SpreadsheetView spv;
    private final int column;
    private MenuButton menuButton;
    private BitSet hiddenRows;

    /**
     * Default constructor, the default "picker-label" styleClass is applied.
     *
     * @param spv
     * @param row
     */
    public FilterBase(SpreadsheetView spv, int column) {
        this.spv = spv;
        this.column = column;
    }

    private Comparator ascendingComp = new Comparator<ObservableList<SpreadsheetCell>>() {
        @Override
        public int compare(ObservableList<SpreadsheetCell> o1, ObservableList<SpreadsheetCell> o2) {
            //If we are 
            if (o1.get(column).getRow() <= spv.getFilteredRow()) {
                return Integer.compare(o1.get(column).getRow(), o2.get(column).getRow());
            } else if (o2.get(column).getRow() <= spv.getFilteredRow()) {
                return Integer.compare(o1.get(column).getRow(), o2.get(column).getRow());
            } else {
                return o1.get(column).getText().compareToIgnoreCase(o2.get(column).getText());
            }
        }
    };

    private Comparator descendingComp = new Comparator<ObservableList<SpreadsheetCell>>() {
        @Override
        public int compare(ObservableList<SpreadsheetCell> o1, ObservableList<SpreadsheetCell> o2) {
            //If we are 
            if (o1.get(column).getRow() <= spv.getFilteredRow()) {
                return Integer.compare(o1.get(column).getRow(), o2.get(column).getRow());
            } else if (o2.get(column).getRow() <= spv.getFilteredRow()) {
                return Integer.compare(o1.get(column).getRow(), o2.get(column).getRow());
            } else {
                return o2.get(column).getText().compareToIgnoreCase(o1.get(column).getText());
            }
        }
    };

    /**
     * This method will be called whenever the user clicks on this picker.
     *
     * @return
     */
    public MenuButton getMenuButton() {
        if (menuButton == null) {
            menuButton = new MenuButton();
            menuButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    if (event.getButton() == MouseButton.SECONDARY) {
                        if (spv.getComparator() == ascendingComp) {
                            spv.setComparator(descendingComp, 0);
                        } else if (spv.getComparator() == descendingComp) {
                            spv.setComparator(null, 0);
                        } else {
                            spv.setComparator(ascendingComp, 0);
                        }
                    }
                }
            });

            menuButton.showingProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                    if (newValue) {
                        addMenuItems();
                        hiddenRows = new BitSet(spv.getHiddenRows().size());
                        hiddenRows.or(spv.getHiddenRows());
                    } else {
                        spv.setHiddenRows(hiddenRows);
                    }
                }
            });
        }
        return menuButton;
    }

    private void addMenuItems() {
        if (menuButton.getItems().isEmpty()) {
            ListView<SpreadsheetCell> listView = new ListView<>();
            listView.setCellFactory(new Callback<ListView<SpreadsheetCell>, ListCell<SpreadsheetCell>>() {
                @Override
                public ListCell<SpreadsheetCell> call(ListView<SpreadsheetCell> param) {
                    return new ListCell<SpreadsheetCell>() {
                        @Override
                        public void updateItem(SpreadsheetCell item, boolean empty) {
                            super.updateItem(item, empty);
                            setText(item == null ? "" : item.getText());
                            if (item != null) {
                                CheckBox checkBox = new CheckBox();
                                checkBox.setSelected(!hiddenRows.get(item.getRow()));
                                checkBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
                                    @Override
                                    public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                                        hiddenRows.set(item.getRow(), !newValue);
                                    }
                                });
                                setGraphic(checkBox);
                            }
                        }
                    };
                }
            });

            for (int i = spv.getFilteredRow(); i < spv.getGrid().getRowCount(); ++i) {
                listView.getItems().add(spv.getGrid().getRows().get(i).get(column));
            }
            CustomMenuItem customMenuItem = new CustomMenuItem(listView);
            customMenuItem.setHideOnClick(false);
            menuButton.getItems().add(customMenuItem);
        }
    }
}
