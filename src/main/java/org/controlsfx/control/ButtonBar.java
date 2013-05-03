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
package org.controlsfx.control;

import impl.org.controlsfx.skin.ButtonBarSkin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sun.javafx.Utils;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

public final class ButtonBar extends Control {
    
    /**************************************************************************
     * 
     * Static fields
     * 
     **************************************************************************/

    /**
     * The default button ordering on Windows.
     */
    public static final String BUTTON_ORDER_WINDOWS = "L_E+U+FBI_YNOCAH_R";
    
    /**
     * The default button ordering on Mac OS.
     */
    public static final String BUTTON_ORDER_MAC_OS  = "L_HE+U+FBI_NYCOA_R";
    
    /**
     * The default button ordering on Linux (specifically, GNOME).
     */
    public static final String BUTTON_ORDER_LINUX   = "L_HE+UNYACBXIO_R";
    
    
    
    /**************************************************************************
     * 
     * Static enumerations
     * 
     **************************************************************************/
    
    /**
     * An enumeration of all available button types. By designating ever button
     * in a {@link ButtonBar} as one of these types, the buttons will be 
     * appropriately positioned relative to all other buttons in the ButtonBar.
     */
    public static enum ButtonType {
        /**
         * Buttons with this style tag will statically end up on the left end of the bar.
         */
        LEFT("L"),
        
        /**
         * Buttons with this style tag will statically end up on the right end of the bar.
         */
        RIGHT("R"),
        
        /**
         * A tag for the "help" button that normally is supposed to be on the right.
         */
        HELP("H"),
        
        /**
         * A tag for the "help2" button that normally is supposed to be on the left.
         */
        HELP_2("E"),
        
        /**
         * A tag for the "yes" button.
         */
        YES("Y"),
        
        /**
         * A tag for the "no" button.
         */
        NO("N"),
        
        /**
         * A tag for the "next >" or "forward >" button.
         */
        NEXT_FORWARD("X"),
        
        /**
         * A tag for the "< back>" or "< previous" button.
         */
        BACK_PREVIOUS("B"),
        
        /**
         * A tag for the "finish".
         */
        FINISH("I"),
        
        /**
         * A tag for the "apply" button.
         */
        APPLY("A"),
        
        /**
         * A tag for the "cancel" or "close" button.
         */
        CANCEL_CLOSE("C"),
        
        /**
         * A tag for the "ok" or "done" button.
         */
        OK_DONE("O"),
        
        /**
         * All Uncategorized, Other, or "Unknown" buttons. Tag will be "other".
         */
        OTHER("U"),
        
        /**
         * A glue push gap that will take as much space as it can and at least 
         * an "unrelated" gap. (Platform dependant)
         */
        BIG_GAP("+"),
        
        /**
         * An "unrelated" gap. (Platform dependant)
         */
        SMALL_GAP("_");
        
        private final String typeCode;
        private ButtonType(String type) {
            this.typeCode = type;
        }
        
        public String getTypeCode() {
            return typeCode;
        }
    }
    
    /**
     * Sets the given ButtonType on the given button. If this button is
     * subsequently placed in a {@link ButtonBar} it will be placed in the 
     * correct position relative to all other buttons in the bar.
     * 
     * @param button The button to tag with the given type.
     * @param type The type to designate the button as.
     */
    public static void setType(ButtonBase button, ButtonType type) {
        button.getProperties().put(ButtonBarSkin.BUTTON_TYPE_PROPERTY, type);
    }
    
    
    
    /**************************************************************************
     * 
     * Private fields
     * 
     **************************************************************************/
    
    private ObservableList<ButtonBase> buttons = FXCollections.<ButtonBase>observableArrayList();
    
    
    
    /**************************************************************************
     * 
     * Constructors
     * 
     **************************************************************************/
    
    public ButtonBar() {
        // set the default button order 
        if (Utils.isMac()) {
            setButtonOrder(BUTTON_ORDER_MAC_OS);
        } else if (Utils.isUnix()) {
            setButtonOrder(BUTTON_ORDER_LINUX);
        } else {
            // windows by default
            setButtonOrder(BUTTON_ORDER_WINDOWS);
        }
    }
    
    
    
    /**************************************************************************
     * 
     * Public API
     * 
     **************************************************************************/
    
    /**
     * {@inheritDoc}
     */
    @Override protected Skin<?> createDefaultSkin() {
        return new ButtonBarSkin(this);
    }

    /**
     * Placing buttons inside this ObservableList will instruct the ButtonBar
     * to position them relative to each other based on their specified
     * {@link ButtonType}. To set the ButtonType for a button, simply call
     * {@link ButtonBar#setType(ButtonBase, ButtonType)}, passing in the 
     * relevant ButtonType.
     *  
     * @return A list containing all buttons currently in the button bar, and 
     *      allowing for further buttons to be added or removed.
     */
    public ObservableList<ButtonBase> getButtons() {
        return buttons;
    }
    
    
    
    /**************************************************************************
     * 
     * Properties
     * 
     **************************************************************************/
    
    /**
     * The order for the typical buttons in a standard button bar. It is 
     * one letter per button type, and the applicable options are part of 
     * {@link ButtonType}. Default button orders for operating systems are also 
     * available: {@link #BUTTON_ORDER_WINDOWS}, {@link #BUTTON_ORDER_MAC_OS},
     * {@link #BUTTON_ORDER_LINUX}.
     */
    private final StringProperty buttonOrderProperty = 
            new SimpleStringProperty(this, "buttonOrder", BUTTON_ORDER_WINDOWS);
    public final StringProperty buttonOrderProperty() {
        return buttonOrderProperty;
    }
    public final void setButtonOrder(String buttonOrder) {
        buttonOrderProperty.set(buttonOrder);
    }
    public final String getButtonOrder() {
        return buttonOrderProperty.get();
    }
    
    
    
    /**************************************************************************
     * 
     * Implementation
     * 
     **************************************************************************/
    

    
    
    /**************************************************************************
     * 
     * Support classes / enums
     * 
     **************************************************************************/
    
}
