/**
 * Copyright (c) 2013, ControlsFX
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
package impl.org.controlsfx.spreadsheet;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.scene.control.TableCell;
import javafx.scene.layout.Region;

import org.controlsfx.control.spreadsheet.SpreadsheetCell;

import com.sun.javafx.scene.control.skin.TableCellSkin;

/**
 * 
 * This is the skin for the {@link CellView}.
 * 
 * Its main goal is to draw an object (a triangle) on cells which have their
 * {@link SpreadsheetCell#commentedProperty()} set to true.
 * 
 */
public class CellViewSkin extends TableCellSkin<ObservableList<SpreadsheetCell>, SpreadsheetCell> {

    /**
     * The size of the edge of the triangle FIXME Handling of static variable
     * will be changed.
     */
    private static final int TRIANGLE_SIZE = 8;
    /**
     * The region we will add on the cell when necessary.
     */
    private Region commentTriangle = null;

    public CellViewSkin(TableCell<ObservableList<SpreadsheetCell>, SpreadsheetCell> tableCell) {
        super(tableCell);
        tableCell.itemProperty().addListener(new ChangeListener<SpreadsheetCell>() {
            @Override
            public void changed(ObservableValue<? extends SpreadsheetCell> arg0, SpreadsheetCell oldCell,
                    SpreadsheetCell newCell) {
                if (oldCell != null) {
                    oldCell.commentedProperty().removeListener(triangleListener);
                }
                if (newCell != null) {
                    newCell.commentedProperty().addListener(triangleListener);
                }
            }
        });

        if (tableCell.getItem() != null) {
            tableCell.getItem().commentedProperty().addListener(triangleListener);
        }
    }

    @Override
    protected void layoutChildren(double x, final double y, final double w, final double h) {
        super.layoutChildren(x, y, w, h);
        if (getSkinnable().getItem() != null) {
            layoutTriangle(getSkinnable().getItem().isCommented());
        }
    }

    private void layoutTriangle(boolean isCommented) {
        if (isCommented) {
            if (commentTriangle == null) {
                commentTriangle = new Region();
            }
            if (!getChildren().contains(commentTriangle)) {
                getChildren().add(commentTriangle);
            }
            commentTriangle.resize(TRIANGLE_SIZE, TRIANGLE_SIZE);
            // Setting the style class is important for the shape.
            commentTriangle.getStyleClass().add("comment");
            commentTriangle.relocate(getSkinnable().getWidth() - TRIANGLE_SIZE, snappedTopInset() - 1);
        } else if (commentTriangle != null) {
            getChildren().remove(commentTriangle);
            commentTriangle = null;
        }
        getSkinnable().requestLayout();
    }

    private ChangeListener<Boolean> triangleListener = new ChangeListener<Boolean>() {
        @Override
        public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldValue, Boolean newValue) {
            getSkinnable().requestLayout();
        }
    };
}
