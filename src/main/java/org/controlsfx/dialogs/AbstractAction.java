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
package org.controlsfx.dialogs;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.Tooltip;

/**
 * A convenience class that implements the {@link Action} interface and provides
 * a simpler API. It is highly recommended to use this class rather than 
 * implement the {@link Action} interface directly.
 * 
 * @see Action
 */
public abstract class AbstractAction implements Action {
    private final StringProperty textProperty = 
            new SimpleStringProperty(this, "text");
    private final BooleanProperty disabledProperty = 
            new SimpleBooleanProperty(this, "disabled");
    private final ObjectProperty<Tooltip> tooltipProperty =
            new SimpleObjectProperty<Tooltip>(this, "tooltip");
    private final ObjectProperty<Node> graphicProperty =
            new SimpleObjectProperty<Node>(this, "graphic");
    
    public AbstractAction(String text) {
        setText(text);
    }
    
    
    // --- text
    @Override public StringProperty textProperty() {
        return textProperty;
    }
    public final String getText() {
        return textProperty.get();
    }
    public final void setText(String value) {
        textProperty.set(value);
    }
    
    
    // --- disabled
    @Override public BooleanProperty disabledProperty() {
        return disabledProperty;
    }
    public final boolean isDisabled() {
        return disabledProperty.get();
    }
    public final void setDisabled(boolean value) {
        disabledProperty.set(value);
    }

    
    // --- tooltip
    @Override public ObjectProperty<Tooltip> tooltipProperty() {
        return tooltipProperty;
    }
    public final Tooltip getTooltip() {
        return tooltipProperty.get();
    }
    public final void setTooltip(Tooltip value) {
        tooltipProperty.set(value);
    }
    
    
    // --- graphic
    @Override public ObjectProperty<Node> graphicProperty() {
        return graphicProperty;
    }
    public final Node getGraphic() {
        return graphicProperty.get();
    }
    public final void setGraphic(Node value) {
        graphicProperty.set(value);
    }

    /**
     * {@inheritDoc}
     */
    @Override public abstract void execute(ActionEvent ae);
}