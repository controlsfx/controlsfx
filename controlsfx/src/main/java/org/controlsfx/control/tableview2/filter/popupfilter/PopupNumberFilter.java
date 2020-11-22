/**
 * Copyright (c) 2018 ControlsFX
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
package org.controlsfx.control.tableview2.filter.popupfilter;

import impl.org.controlsfx.tableview2.filter.parser.number.NumberParser;
import org.controlsfx.control.tableview2.FilteredTableColumn;
import org.controlsfx.control.tableview2.filter.parser.Parser;

import java.util.List;

/**
 * PopupFilter to be used on {@link javafx.scene.control.TableColumn} containing cells of Number type.
 *
 * @param <S> Type of the objects contained within the 
 *      {@link org.controlsfx.control.tableview2.FilteredTableView} items list.
 * @param <T> Type of the content to be filtered,
 *           which is similar to the type of cells contained in the 
 *      {@link FilteredTableColumn}. Should
 *       extend {@link Number}.
 */
public class PopupNumberFilter<S, T extends Number> extends PopupFilter<S, T> {

    private final NumberParser<T> numberParser;

    /**
     * Creates a new instance of PopupNumberFilter.
     * @param tableColumn TableColumn associated with PopupFilter
     */
    public PopupNumberFilter(FilteredTableColumn<S, T> tableColumn) {
        super(tableColumn);
        numberParser = new NumberParser<>();
        
        text.addListener((obs, ov, nv) -> {
            if (nv == null || nv.isEmpty()) {
                tableColumn.setPredicate(null);
            } else {
                tableColumn.setPredicate(getParser().parse(nv));
            }
        });
    }

    /**
     *{@inheritDoc}
     */
    @Override
    public List<String> getOperations() {
        return numberParser.operators();
    }

    /**
     *{@inheritDoc}
     */
    @Override
    public Parser<T> getParser() {
        return numberParser;
    }
}
