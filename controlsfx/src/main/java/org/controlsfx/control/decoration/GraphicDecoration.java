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

import impl.org.controlsfx.ImplUtils;

import java.util.List;

import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;

public class GraphicDecoration implements Decoration {

    private final Node decorationNode;
    private final Pos pos;
    private final double xOffset;
    private final double yOffset;

    public GraphicDecoration(Node decoration) {
        this(decoration, Pos.TOP_LEFT);
    }
    
    public GraphicDecoration(Node decorationNode, Pos position) {
        this(decorationNode, position, 0, 0);
    }
    
    public GraphicDecoration(Node decorationNode, Pos position, double xOffset, double yOffset) {
        this.decorationNode = decorationNode;
        this.decorationNode.setManaged(false);
        this.pos = position;
        this.xOffset = xOffset;
        this.yOffset = yOffset;
    }
    
    @Override public Node run(Node targetNode, boolean add) {
        List<Node> targetNodeChildren = ImplUtils.getChildren((Parent)targetNode);
        
        if (add) {
            updateGraphicPosition(targetNode);
            if (!targetNodeChildren.contains(decorationNode)) {
                targetNodeChildren.add(decorationNode);
            }
            return null;
        } else {
            if (targetNodeChildren.contains(decorationNode)) {
                targetNodeChildren.remove(decorationNode);
            }
            return null;
        }
    }
    
    private void updateGraphicPosition(Node targetNode) {
        final double decorationNodeWidth = decorationNode.prefWidth(-1);
        final double decorationNodeHeight = decorationNode.prefHeight(-1);
        
        Bounds targetBounds = targetNode.getLayoutBounds();
        double x = targetBounds.getMinX();
        double y = targetBounds.getMinY();

        double targetWidth = targetBounds.getWidth();
        if (targetWidth <= 0) {
            targetWidth = targetNode.prefWidth(-1);
        }
        
        double targetHeight = targetBounds.getHeight();
        if (targetHeight <= 0) {
            targetHeight = targetNode.prefHeight(-1);
        }

        // x
        switch (pos.getHpos()) {
        	case CENTER: 
        		x += targetWidth/2 - decorationNodeWidth / 2.0;
        		break;
        	case LEFT: 
        		x -= decorationNodeWidth / 2.0;
        		break;
        	case RIGHT:
        		x += targetWidth - decorationNodeWidth / 2.0;
        		break;
        }
        
        // y
        switch (pos.getVpos()) {
        	case CENTER: 
        		y += targetHeight/2 - decorationNodeHeight / 2.0;
        		break;
        	case TOP: 
        		y -= decorationNodeHeight / 2.0;
        		break;
        	case BOTTOM:
        		y += targetHeight - decorationNodeWidth / 2.0;
        		break;
        	case BASELINE: 
        		y += targetNode.getBaselineOffset() - decorationNode.getBaselineOffset() - decorationNodeHeight / 2.0;
        		break;
        }
        
        decorationNode.setLayoutX(x + xOffset);
        decorationNode.setLayoutY(y + yOffset);
    }
}
