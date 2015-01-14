/**
 * Copyright (c) 2014, ControlsFX
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
package impl.org.controlsfx.skin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import javafx.beans.property.BooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;

import org.controlsfx.control.decoration.Decoration;
import org.controlsfx.control.decoration.Decorator;

public class DecorationPane extends StackPane {
    
    // maps from a node to a list of its decoration nodes
    private final Map<Node, List<Node>> nodeDecorationMap = new WeakHashMap<>();
    
    ChangeListener<Boolean> visibilityListener = new ChangeListener<Boolean>() {
        @Override public void changed(ObservableValue<? extends Boolean> o, Boolean wasVisible, Boolean isVisible) {
            BooleanProperty p = (BooleanProperty)o;
            Node n = (Node) p.getBean();
            
            removeAllDecorationsOnNode(n, Decorator.getDecorations(n));
            Decorator.removeAllDecorations(n);
        }
    };

    public DecorationPane() {
        // Make DecorationPane transparent
        setBackground(null);
    }
        
    public void setRoot(Node root) {
        getChildren().setAll(root);
    }
    
    public void updateDecorationsOnNode(Node targetNode, List<Decoration> added, List<Decoration> removed) {
        removeAllDecorationsOnNode(targetNode, removed);
        addAllDecorationsOnNode(targetNode, added);
    }

    private void showDecoration(Node targetNode, Decoration decoration) {
        Node decorationNode = decoration.applyDecoration(targetNode);
        if (decorationNode != null) {
            List<Node> decorationNodes = nodeDecorationMap.get(targetNode);
            if (decorationNodes == null) {
                decorationNodes = new ArrayList<>();
                nodeDecorationMap.put(targetNode, decorationNodes);
            }
            decorationNodes.add(decorationNode);
            
            if (!getChildren().contains(decorationNode)) {
                getChildren().add(decorationNode);
                StackPane.setAlignment(decorationNode, Pos.TOP_LEFT); // TODO support for all positions.
            }
        }
        
        targetNode.visibleProperty().addListener(visibilityListener);
    }

    private void removeAllDecorationsOnNode(Node targetNode, List<Decoration> decorations) {
        if (decorations == null || targetNode == null) return;
        
        // We need to do two things: 
        // 1) Remove the decoration node (if it exists) from the nodeDecorationMap
        //    for the targetNode, if it exists.
        List<Node> decorationNodes = nodeDecorationMap.remove(targetNode);
        if (decorationNodes != null) {
            for (Node decorationNode : decorationNodes) {
                boolean success = getChildren().remove(decorationNode);
                if (! success) {
                    throw new IllegalStateException("Could not remove decoration " +  //$NON-NLS-1$
                            decorationNode + " from decoration pane children list: " +  //$NON-NLS-1$
                            getChildren());
                }
            }
        }
        
        // 2) Tell the decoration to remove itself from the target node (if necessary)
        for (Decoration decoration : decorations) {
            decoration.removeDecoration(targetNode);
        }
    }
    
    private void addAllDecorationsOnNode(Node targetNode, List<Decoration> decorations) {
        if (decorations == null) return;
        for (Decoration decoration : decorations) {
            showDecoration(targetNode, decoration);
        }
    }
}
