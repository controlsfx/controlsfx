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

import javafx.scene.control.MenuButton;

/**
 *
 * The interface for implementing Filters on {@link SpreadsheetColumn}. Simply
 * set a Filtered row on the {@link SpreadsheetView} by using {@link SpreadsheetView#setFilteredRow(java.lang.Integer)
 * }.
 * <br>
 * Then construct the Filters and add them to each {@link SpreadsheetColumn}.
 * <br>
 * A Filter is simply a customizable {@link MenuButton} that will be displayed
 * over the cell. It is primarily designed to filter some rows in the
 * SpreadsheetView. But the MenuButton can serve any purpose since it is
 * entirely customizable.
 * <br>
 * <center><img src="filterExample.PNG" alt="Some filters set on cells."></center>
 */
public interface Filter {

    /**
     * Return the MenuButton displayed into the bottom-right corner of the cell.
     * This method will be called whenever the user clicks on this Filter.
     *
     * @return the MenuButton displayed into the bottom-right corner of the
     * cell.
     */
    public MenuButton getMenuButton();
}
