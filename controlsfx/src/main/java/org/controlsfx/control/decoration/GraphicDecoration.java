/**
 * Copyright (c) 2014, 2017 ControlsFX
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
import java.util.Optional;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.image.ImageView;

/**
 * GraphicDecoration is a {@link Decoration} designed to show a graphic (be it
 * an image loaded via an {@link ImageView} or an arbitrarily complex 
 * scenegraph in its own right) on top of a given node. GraphicDecoration is
 * applied as part of the ControlsFX {@link Decorator} API - refer to the 
 * {@link Decorator} javadoc for more details.
 * 
 * @see Decoration
 * @see Decorator
 */
public class GraphicDecoration extends Decoration {

    private final Node decorationNode;
    private final Pos pos;
    private final double xOffset;
    private final double yOffset;
    /**
     * Because {@link #decorationNode} is unmanaged, we need this listener
     * to detect when its Parent lays out so that we can lay out {@code decorationNode}.
     */
    private final ChangeListener<Boolean> targetNeedsLayoutListener;

    /**
     * Constructs a new GraphicDecoration with the given decoration node to be 
     * applied to any node that has this decoration applied to it. By default
     * the decoration node will be applied in the top-left corner of the node.
     * 
     * @param decorationNode The decoration node to apply to any node that has this 
     *      decoration applied to it 
     */
    public GraphicDecoration(Node decorationNode) {
        this(decorationNode, Pos.TOP_LEFT);
    }
    
    /**
     * Constructs a new GraphicDecoration with the given decoration node to be 
     * applied to any node that has this decoration applied to it, in the location
     * provided by the {@link Pos position} argument.
     * 
     * @param decorationNode The decoration node to apply to any node that has this 
     *      decoration applied to it 
     * @param position The location to position the decoration node relative to the 
     *      node that is being decorated.
     */
    public GraphicDecoration(Node decorationNode, Pos position) {
        this(decorationNode, position, 0, 0);
    }
    
    /**
     * Constructs a new GraphicDecoration with the given decoration node to be 
     * applied to any node that has this decoration applied to it, in the location
     * provided by the {@link Pos position} argument, with the given xOffset and
     * yOffset values used to adjust the position.
     * 
     * @param decorationNode The decoration node to apply to any node that has this 
     *      decoration applied to it 
     * @param position The location to position the decoration node relative to the 
     *      node that is being decorated.
     * @param xOffset The amount of movement to apply to the decoration node in the
     *      x direction (i.e. left and right).
     * @param yOffset The amount of movement to apply to the decoration node in the
     *      y direction (i.e. up and down). 
     */
    public GraphicDecoration(Node decorationNode, Pos position, double xOffset, double yOffset) {
        this.decorationNode = decorationNode;
        this.decorationNode.setManaged(false);
        this.pos = position;
        this.xOffset = xOffset;
        this.yOffset = yOffset;
        targetNeedsLayoutListener = ( __, ___, ____ ) ->
                Optional.ofNullable( decorationNode.getParent() ).ifPresent( this::layoutGraphic );
    }
    
    /** {@inheritDoc} */
    @Override public Node applyDecoration(Node targetNode) {
        List<Node> targetNodeChildren = ImplUtils.getChildren((Parent)targetNode, true);
        layoutGraphic(targetNode);
        if (!targetNodeChildren.contains(decorationNode)) {
            targetNodeChildren.add(decorationNode);
        }
        ((Parent) targetNode).needsLayoutProperty().removeListener(targetNeedsLayoutListener); // In case already added.
        ((Parent) targetNode).needsLayoutProperty().addListener(targetNeedsLayoutListener);
        return null;
    }
    
    /** {@inheritDoc} */
    @Override public void removeDecoration(Node targetNode) {
        List<Node> targetNodeChildren = ImplUtils.getChildren((Parent)targetNode, true);
        
        if (targetNodeChildren.contains(decorationNode)) {
            targetNodeChildren.remove(decorationNode);
        }
        ((Parent) targetNode).needsLayoutProperty().removeListener(targetNeedsLayoutListener);
    }

    private void layoutGraphic(Node targetNode) {
        // Because we made decorationNode unmanaged, we are responsible for sizing it:
        decorationNode.autosize();
        // Now get decorationNode's layout Bounds and use for its position computations: 
        final Bounds decorationNodeLayoutBounds = decorationNode.getLayoutBounds();
        final double decorationNodeWidth = decorationNodeLayoutBounds.getWidth();
        final double decorationNodeHeight = decorationNodeLayoutBounds.getHeight();

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
