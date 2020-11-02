/**
 * Copyright (c) 2019 ControlsFX
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

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.Background;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;

/**
 *
 * If anyone wants to display a specific Graphic in a SpreadsheetCell, a
 * solution is to provide a Node (for example a WebView) that will be
 * displayed in the cell.
 *
 * Because a Node can consume a lot of memory, we need this
 * {@code CellGraphicFactory} that will recycle the Nodes to only provide them
 * for visible cells.
 */
public interface CellGraphicFactory<T extends Node> {

    /**
     * Returns a {@code Node} to display in the cell graphic. This is called
     * internally by the SpreadsheetView when a cell is being visible and needs
     * to display a {@code Node}.
     *
     * @param cell the considered SpreadsheetCell
     * @return a {@code Node} to display in the cell graphic
     */
    public Node getNode(SpreadsheetCell cell);

    /**
     * When a {@code Node} is reused (transfered from one cell to another for
     * example), we ask the Node to reload. Beware, only reload when necessary!
     * This method can be called several times with the same {@code Node} and
     * itemValue.
     *
     * @param node the considered {@code Node}
     * @param cell the considered SpreadsheetCell
     */
    public void load(T node, SpreadsheetCell cell);

    /**
     * Once a {@code SpreadsheetCell} has been effectively loaded in the grid,
     * this method is called if the {@code Node} wants to access the cell's
     * graphic details.
     *
     * @param node the considered {@code Node}, may be {@code null} for empty
     * cell.
     * @param cell the considered SpreadsheetCell
     * @param font the cell {@code Font}
     * @param textFill the text's color
     * @param alignment the cell's vertical and horizontal alignment
     * @param background the cell's background
     */
    public void loadStyle(T node, SpreadsheetCell cell, Font font, Paint textFill, Pos alignment, Background background);

    /**
     * Once a {@code Node} is no longer used in a cell, it is given back.
     *
     * @param node the {@code Node}
     */
    public void setUnusedNode(T node);

    /**
     * Returns the exact class used in this CellGraphicFactory. It is used to
     * determine if the cell's graphic is handled by the cell of by this
     * interface.
     *
     * @return the exact class used in this CellGraphicFactory
     */
    public Class getType();

}
