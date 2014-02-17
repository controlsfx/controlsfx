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
package org.controlsfx.dialog;

import java.util.Iterator;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.stage.Stage;
import javafx.stage.Window;

class DialogFactory {
    
    static FXDialog createDialog(String title) {
        return createDialog(false, title);
    }
    
    static FXDialog createDialog(boolean useLightweight, String title) {
        return createDialog(false, title, null, false);
    }

    static FXDialog createDialog(boolean useLightweight, String title, Object owner, boolean modal) {
        return createDialog(useLightweight, title, owner, modal, false);
    }
    
    static FXDialog createDialog(boolean useLightweight, String title, Object owner, boolean modal, boolean nativeChrome) {
        if (useLightweight) {
            return new LightweightDialog(title, owner);
        } else {
        	
        	Window window = null;
            
            // we need to determine the type of the owner, so that we can appropriately
            // show the dialog
            if (owner == null) {
                // lets just get the focused stage and show the dialog in there
                @SuppressWarnings("deprecation")
				Iterator<Window> windows = Window.impl_getWindows();
                while (windows.hasNext()) {
                    window = windows.next();
                    if (window.isFocused()) {
                        break;
                    }
                }
            } else if (owner instanceof Window) {
                window = (Window) owner;
            } else if (owner instanceof Node) {
            	window = ((Node)owner).getScene().getWindow();
            } else {
                throw new IllegalArgumentException("Unknown owner: " + owner.getClass());
            }
        	
            return new HeavyweightDialog(title, window, modal, nativeChrome);
        }
    }
    
}
