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
package org.controlsfx.control.decoration;

import java.util.HashMap;
import java.util.Map;

import javafx.scene.Node;

/**
 * Decoration is an abstract class used by the ControlsFX {@link Decorator} class
 * for adding and removing decorations on a node. ControlsFX
 * ships with pre-built decorations, including {@link GraphicDecoration} and
 * {@link StyleClassDecoration}.
 * 
 * <p>To better understand how to use the ControlsFX decoration API in your 
 * application, refer to the code samples and explanations in {@link Decorator}.
 * 
 * @see Decorator
 * @see GraphicDecoration
 * @see StyleClassDecoration
 */
public abstract class Decoration {
    
    private volatile Map<String,Object> properties;
    
    /**
     * Instantiates a default Decoration instance (obviously only callable by
     * subclasses).
     */
    protected Decoration() {
        // no-op
    }
    
	/**
     * This method decorates the given 
     * target node with the relevant decorations, returning any 'decoration node' 
     * that needs to be added to the scenegraph (although this can be null). When
     * the returned Node is null, this indicates that the decoration will be 
     * handled internally by the decoration (which is preferred, as the default
     * implementation is not ideal in most circumstances).
     * 
     * <p>When the boolean parameter is false, this method removes the decoration 
     * from the given target node, always returning null.
     * 
     * @param targetNode The node to decorate.
     * @return The decoration, but null is a valid return value.
     */
    public abstract Node applyDecoration(Node targetNode);
    
    /**
     * This method removes the decoration from the given target node.
     * 
     * @param targetNode The node to undecorate.
     */
    public abstract void removeDecoration(Node targetNode);
    
    /**
     * Custom decoration properties
     * @return decoration properties
     */
    public synchronized final Map<String,Object> getProperties() {
        if (properties == null) {
            properties = new HashMap<>();
        }
    	return properties;
    }
}
