/**
 * Copyright (c) 2013, 2018 ControlsFX
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
package org.controlsfx.control.tableview2;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.control.TableColumn;

/**
 * An extension of {@link TableColumn} that allows adding a South Header Node.
 * 
 * @param <S> The type of the objects contained within the TableView items list.
 * @param <T> The type of the content in all cells in this TableColumn
 */
public class TableColumn2<S, T> extends TableColumn<S, T> {
    
    /***************************************************************************
     * * Constructor * *
     **************************************************************************/

    /**
     * Creates a TableColumn2 control.
     */
    public TableColumn2() {
        super();
    }
    
    public TableColumn2(String text) {
        this();
        setText(text);
    }
    
    /***************************************************************************
     * * Properties * *
     **************************************************************************/
    
    /**
     * This property allows the developer to set a node to the south of the header
     * of this column, where UI can be displayed.
     */
    private final ObjectProperty<Node> southNode = new SimpleObjectProperty<>(this, "southNode", null);
    public final void setSouthNode(Node value) { southNode.set(value); }
    public final Node getSouthNode() { return southNode.get(); }
    public final ObjectProperty<Node> southNodeProperty() { return southNode; }
    
}
