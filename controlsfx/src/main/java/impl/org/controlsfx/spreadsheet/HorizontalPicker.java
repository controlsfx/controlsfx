/**
 * Copyright (c) 2014, 2021 ControlsFX
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Stack;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.event.EventHandler;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.skin.TableColumnHeader;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import org.controlsfx.control.spreadsheet.Picker;
import org.controlsfx.control.spreadsheet.SpreadsheetView;

/**
 *
 * This class will display all the available pickers. It is a StackPane clipped
 * which contain a inner Region that display the picker. In that way, we don't
 * need to re-layout every time but just "slide" the inner Region inside this
 * class so that the pickers are sliding along with the TableColumnHeaders.
 */
public class HorizontalPicker extends StackPane {

    private static final String PICKER_INDEX = "PickerIndex"; //$NON-NLS-1$

    private final HorizontalHeader horizontalHeader;
    private final SpreadsheetView spv;
    // Labels representing pickers that are ready to be re-used
    private final Stack<Label> pickerPile;
    // Labels reprenting pickers that are currently used (key is the column index)
    private final Map<Integer, Label> pickerUsed;
    private final InnerHorizontalPicker innerPicker = new InnerHorizontalPicker();

    public HorizontalPicker(HorizontalHeader horizontalHeader, SpreadsheetView spv) {
        this.horizontalHeader = horizontalHeader;
        this.spv = spv;

        pickerPile = new Stack<>();
        pickerUsed = new HashMap<>();

        //Clip this StackPane just like the TableHeaderRow.
        Rectangle clip = new Rectangle();
        clip.setSmooth(true);
        clip.setHeight(VerticalHeader.PICKER_SIZE);
        clip.widthProperty().bind(horizontalHeader.widthProperty());
        setClip(clip);

        getChildren().add(innerPicker);

        horizontalHeader.getRootHeader().getColumnHeaders().addListener(layoutListener);
        spv.getColumnPickers().addListener(layoutListener);
    }

    @Override
    protected void layoutChildren() {
        //Just relocate the inner for sliding.
        innerPicker.relocate(horizontalHeader.getRootHeader().getLayoutX(), snappedTopInset());
        //We must turn off pickers that are behind fixed columns
        for (Label label : pickerUsed.values()) {
            label.setVisible(label.getLayoutX() + innerPicker.getLayoutX() + label.getWidth() > horizontalHeader.gridViewSkin.fixedColumnWidth);
        }
    }

    /**
     * Method called by the HorizontalHeader in order to slide the pickers.
     */
    public void updateScrollX() {
        requestLayout();
    }

    /**
     * Inner class that will lay out all the pickers.
     */
    private class InnerHorizontalPicker extends Region {

        @Override
        protected void layoutChildren() {
            int columnSize = horizontalHeader.getRootHeader().getColumnHeaders().size();
            List<Integer> list = new ArrayList<>();
            // Clean all pickers
            for (Entry<Integer, Label> entry : pickerUsed.entrySet()) {
                Label label = entry.getValue();

                // Add picker that are out of bounds to the pile
                if (entry.getKey() >= columnSize) {
                    list.add(entry.getKey());
                    pickerPile.push(label);
                    getChildren().remove(label);
                }

                label.getStyleClass().removeListener(layoutListener);
                label.getStyleClass().clear();
                label.getProperties().remove(PICKER_INDEX);
            }
            pickerUsed.keySet().removeAll(list);

            int index = 0;
            int modelColumn;
            for (TableColumnHeader column : horizontalHeader.getRootHeader().getColumnHeaders()) {
                modelColumn = spv.getModelColumn(index);
                Picker picker = spv.getColumnPickers().get(modelColumn);
                if (picker != null) {
                    Label label = getPicker(picker, index);
                    label.resize(column.getWidth(), VerticalHeader.PICKER_SIZE);
                    label.layoutXProperty().bind(column.layoutXProperty());
                } else {
                    // Picker in place no longer used, add it to the pile
                    Label pickerLabel = pickerUsed.remove(index);
                    if (pickerLabel != null) {
                        pickerLabel.layoutXProperty().unbind();
                        pickerPile.add(pickerLabel);
                        getChildren().remove(pickerLabel);
                    }
                }
                ++index;
            }
        }

        /**
         * Return a Label representing the given Picker for the given column
         * index.
         *
         * @param picker the picker as defined in the Grid
         * @param column the column index
         * @return a label ready to be displayed
         */
        private Label getPicker(Picker picker, int column) {
            // Try to re-use a picker that are already placed for the column
            Label pickerLabel = pickerUsed.get(column);
            if (pickerLabel == null) {
                if (pickerPile.isEmpty()) {
                    pickerLabel = new Label();
                    pickerLabel.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                    pickerLabel.setOnMouseClicked(pickerMouseEvent);
                } else {
                    pickerLabel = pickerPile.pop();
                }
                pickerUsed.put(column, pickerLabel);
                getChildren().add(0, pickerLabel);
            }

            pickerLabel.getStyleClass().addAll(picker.getStyleClass());
            pickerLabel.getStyleClass().addListener(layoutListener);
            pickerLabel.getProperties().put(PICKER_INDEX, picker);
            return pickerLabel;
        }

        private final EventHandler<MouseEvent> pickerMouseEvent = (MouseEvent mouseEvent) -> {
            Label picker = (Label) mouseEvent.getSource();
            ((Picker) picker.getProperties().get(PICKER_INDEX)).onClick();
        };
    }

    private final InvalidationListener layoutListener = (Observable arg0) -> {
        innerPicker.requestLayout();
    };
}
