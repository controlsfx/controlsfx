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

import impl.org.controlsfx.skin.DecorationPaneSkin;

import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Parent;

import org.controlsfx.control.DecorationPane;

public class DecorationUtils {

    public final static String DECORATIONS_PROPERTY_KEY = "$org.controlsfx.decorations$";

    private DecorationUtils() {
        // no op
    }

    public static final void registerDecoration(Node target, Decoration decoration) {
        getAllDecorations(target, true).add(decoration);
        updateDecorationsOnNode(target, FXCollections.observableArrayList(decoration), null);
    }

    public static final void unregisterDecoration(Node target, Decoration decoration) {
        getAllDecorations(target, true).remove(decoration);
        updateDecorationsOnNode(target, null, FXCollections.observableArrayList(decoration));
    }
    
    public static final void unregisterAllDecorations(Node target) {
        List<Decoration> decorations = getAllDecorations(target, true);
        List<Decoration> removed = FXCollections.observableArrayList(decorations);
        
        target.getProperties().remove(DECORATIONS_PROPERTY_KEY);
        
        updateDecorationsOnNode(target, null, removed);
    }
    
    public static final ObservableList<Decoration> getAllDecorations(Node target) {
        return getAllDecorations(target, false);
    }

    private static final ObservableList<Decoration> getAllDecorations(Node target, boolean createIfAbsent) {
        @SuppressWarnings("unchecked")
        ObservableList<Decoration> decorations = (ObservableList<Decoration>) target.getProperties().get(DECORATIONS_PROPERTY_KEY);
        if (decorations == null && createIfAbsent) {
            decorations = FXCollections.observableArrayList();
            target.getProperties().put(DECORATIONS_PROPERTY_KEY, decorations);
        }
        return decorations;
    }
    
    private static void updateDecorationsOnNode(Node target, List<Decoration> added, List<Decoration> removed) {
        // find a DecorationPane parent and notify it that a node has updated
        // decorations
        DecorationPane p = getDecorationPane(target);
        if (p == null) return;
        
        DecorationPaneSkin skin = (DecorationPaneSkin) p.getSkin();
        skin.updateDecorationsOnNode(target, added, removed);
    }
    
    private static DecorationPane getDecorationPane(Node target) {
        // find a DecorationPane parent and notify it that a node has updated
        // decorations
        Parent p = target.getParent();
        while (p != null) {
            if (p instanceof DecorationPane) {
                break;
            }
            p = p.getParent();
        }
        
        return (DecorationPane)p;
    }
}
