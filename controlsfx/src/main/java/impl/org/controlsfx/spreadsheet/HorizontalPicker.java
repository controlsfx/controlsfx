/**
 * Copyright (c) 2014 ControlsFX
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

import com.sun.javafx.scene.control.skin.TableColumnHeader;
import java.util.Stack;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.event.EventHandler;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import org.controlsfx.control.spreadsheet.SpreadsheetView;

/**
 *
 * This class will display all the available pickers. It is a StackPane clipped which
 * contain a inner Region that display the picker. In that way, we don't need to re-layout
 * every time but just "slide" the inner Region inside this class so that the pickers
 * are sliding along with the TableColumnHeaders.
 */
public class HorizontalPicker extends StackPane {

    private static final String PICKER_INDEX = "PickerIndex"; //$NON-NLS-1$

    private final HorizontalHeader horizontalHeader;

    private final SpreadsheetView spv;
    private final Stack<Label> pickerPile;
    private final Stack<Label> pickerUsed;

    private final InnerHorizontalPicker innerPicker = new InnerHorizontalPicker();

    public HorizontalPicker(HorizontalHeader horizontalHeader, SpreadsheetView spv) {
        this.horizontalHeader = horizontalHeader;
        this.spv = spv;

        pickerPile = new Stack<>();
        pickerUsed = new Stack<>();

        //Clip this StackPane just like the TableHeaderRow.
        Rectangle clip = new Rectangle();
        clip.setSmooth(true);
        clip.setHeight(VerticalHeader.PICKER_SIZE);
        clip.widthProperty().bind(horizontalHeader.widthProperty());
        setClip(clip);

        getChildren().add(innerPicker);
        
        horizontalHeader.getRootHeader().getColumnHeaders().addListener(new InvalidationListener() {

            @Override
            public void invalidated(Observable o) {
                innerPicker.requestLayout();
            }
        });
    }

    @Override
    protected void layoutChildren() {
        //Just relocate the inner for sliding.
        innerPicker.relocate(horizontalHeader.getRootHeader().getLayoutX(), snappedTopInset());
    }

    /**
     * Method called by the HorizontalHeader in order to slide the pickers.
     */
    public void updateScrollX() {
        requestLayout();
    }

    private Label getPicker(int columnNumber) {
        Label picker;
        if (pickerPile.isEmpty()) {
            picker = new Label();
            picker.getStyleClass().add("picker-label"); //$NON-NLS-1$
            picker.setOnMouseClicked(pickerMouseEvent);
        } else {
            picker = pickerPile.pop();
        }
        pickerUsed.push(picker);
        picker.getProperties().put(PICKER_INDEX, columnNumber);
        return picker;
    }

    private final EventHandler<MouseEvent> pickerMouseEvent = new EventHandler<MouseEvent>() {

        @Override
        public void handle(MouseEvent mouseEvent) {
            Label picker = (Label) mouseEvent.getSource();

           spv.getColumnPickerCallback().call((Integer) picker.getProperties().get(PICKER_INDEX));
        }
    };

    /**
     * Inner class that will lay out all the pickers.
     */
    private class InnerHorizontalPicker extends Region {

        @Override
        protected void layoutChildren() {
            pickerPile.addAll(pickerUsed.subList(0, pickerUsed.size()));
            pickerUsed.clear();

            getChildren().clear();
            double x = 0;
            for (int index = 0; index < horizontalHeader.getRootHeader().getColumnHeaders().size(); ++index) {
                TableColumnHeader column = horizontalHeader.getRootHeader().getColumnHeaders().get(index);
                if (spv.getColumnPickers().contains(index)) {
                    Label label = getPicker(index);
                    label.resize(column.getWidth(), VerticalHeader.PICKER_SIZE);
                    label.setLayoutX(x);
                    getChildren().add(label);
                }
                x += column.getWidth();
            }
        }
    }
}
