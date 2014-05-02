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

import java.util.HashMap;
import java.util.Map;

import javafx.scene.Node;

/**
 * Decoration is a functional interface used by the ControlsFX {@link Decorator} class
 * for adding and removing decorations on a node. ControlsFX
 * ships with pre-built decorations, including {@link GraphicDecoration} and
 * {@link StyleClassDecoration}.
 * 
 * @see Decorator
 * @see GraphicDecoration
 * @see StyleClassDecoration
 */
@FunctionalInterface
public interface Decoration {
    
	/**
     * Depending on the boolean 'add' parameter, this method is responsible for
     * decorating or undecorating the given target node. 
     * 
     * <p>When the boolean parameter is true, this method decorates the given 
     * target node with the relevant decorations, returning any 'decoration node' 
     * that needs to be added to the scenegraph (although this can be null). When
     * the returned Node is null, this indicates that the decoration will be 
     * handled internally by the decorator (which is preferred, as the default
     * implementation is not ideal in most circumstances).
     * 
     * <p>When the boolean parameter is false, this method removes the decoration 
     * from the given target node, always returning null.
     * 
     * @param targetNode The node to decorate or undecorate.
     * @return If the add parameter is true, the decoration, but null is a 
     *         valid return value. If the add parameter is false, always null.
     */
    public Node run(Node targetNode, boolean add);
    
    //TODO: Move to abstract class - too public :)
    final Map<String,Object> properties = new HashMap<>(); 
    
    /**
     * Custom decoration properties
     * @return decoration properties
     */
    default Map<String,Object> getProperties() {
    	return properties;
    }
}
