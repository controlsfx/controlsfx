/**
 * Copyright (c) 2013, 2014, ControlsFX
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
package org.controlsfx.control.decoration;

import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;

public class GraphicDecoration implements Decoration {

    private final Node decorationNode;
    private final Pos pos;

    public GraphicDecoration(Node decoration) {
        this(decoration, Pos.TOP_LEFT);
    }
    
    public GraphicDecoration(Node decorationNode, Pos position) {
        this.decorationNode = decorationNode;
        this.pos = position;
    }
    
    @Override public Node run(Node targetNode, boolean add) {
        if (add) {
            Bounds targetBounds = targetNode.getBoundsInParent();
            Bounds dbounds = decorationNode.getBoundsInLocal();
            
            double top = targetBounds.getMinY() - dbounds.getHeight() / 2 + getVInset(targetBounds);
            double left = targetBounds.getMinX() - dbounds.getWidth() / 2 + getHInset(targetBounds);
            Insets margin = new Insets(top, 0, 0, left);
            StackPane.setMargin(decorationNode, margin);
            
            return decorationNode;
        }
        
        return null;
    }
    
    private double getHInset(Bounds targetBounds) {
        switch (pos.getHpos()) {
            case CENTER:
                return targetBounds.getWidth() / 2;
            case RIGHT:
                return targetBounds.getWidth();
            default:
                return 0;
        }
    }

    private double getVInset(Bounds targetBounds) {
        switch (pos.getVpos()) {
            case CENTER:
                return targetBounds.getHeight() / 2;
            case BOTTOM:
                return targetBounds.getHeight();
            default:
                return 0;
        }
    }
}
