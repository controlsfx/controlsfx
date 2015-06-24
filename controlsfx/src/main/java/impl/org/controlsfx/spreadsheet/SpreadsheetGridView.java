/**
 * Copyright (c) 2013, 2014, 2015 ControlsFX
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

import javafx.collections.ObservableList;
import javafx.scene.control.TableView;
import org.controlsfx.control.spreadsheet.SpreadsheetCell;
import org.controlsfx.control.spreadsheet.SpreadsheetView;

public class SpreadsheetGridView extends TableView<ObservableList<SpreadsheetCell>> {
    private final SpreadsheetHandle handle;

    /*
     * cache the stylesheet as lookup takes time and the getUserAgentStylesheet is called repeatedly
     */
    private String stylesheet;

    /**
     * We don't want to show the current value in the TextField when we are
     * editing by typing a key. We want directly to take those typed letters
     * and put them into the textfield.
     */
    public SpreadsheetGridView(SpreadsheetHandle handle) {
        this.handle = handle;
    }

    @Override
    public String getUserAgentStylesheet() {
        /*
         * For more information please see RT-40658
         */
        if (stylesheet == null) {
            stylesheet = SpreadsheetView.class.getResource("spreadsheet.css") //$NON-NLS-1$
                    .toExternalForm();
        }

        return stylesheet;
    }

    @Override
    protected javafx.scene.control.Skin<?> createDefaultSkin() {
        return new GridViewSkin(handle);
    }

    public GridViewSkin getGridViewSkin() {
        return handle.getCellsViewSkin();
    }
};
