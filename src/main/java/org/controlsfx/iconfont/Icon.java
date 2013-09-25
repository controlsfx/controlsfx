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
package org.controlsfx.iconfont;

import org.controlsfx.tools.Duplicatable;

import javafx.scene.control.Label;
import javafx.scene.paint.Color;

/**
 * Represents one icon from the icon font.
 * The icon is actually a label showing one character from the specified icon font. It can be used as 'graphic' on any UI
 * control or {@link Action}  
 *
 */
public class Icon extends Label implements Duplicatable<Icon>{

    private final String fontFamily;
    private final Character character;
    private double size;
    private Color color;

    /**
     * Creates the icon
     * @param fontFamily icon font the icon should be based on
     * @param character character representing the icon in the icon font
     * @param size icon size in pixels
     * @param color icon color
     */
    public Icon(String fontFamily, Character character, double size, Color color) {
        super(character.toString());

        this.fontFamily = fontFamily;
        this.character = character;
        this.size = size;
        this.color = color;

        getStyleClass().add("icon-font");
        updateStyle();
    }
    
    /**
     * Sets icon size in pixels
     * @param size
     */
    public void setSize(double size) {
        this.size = size;
        updateStyle();
    }
    
    /**
     * Sets icon color
     * @param color
     */
    public void setColor(Color color) {
        this.color = color;
        updateStyle();
    }
    
    //TODO: Need to be able to use external styles
    private void updateStyle() {
        StringBuilder css = new StringBuilder( String.format("-fx-font-family: %s; -fx-font-size: %fpx;", fontFamily, size));
        if (color == null) {
            css.append("-icons-color: -fx-text-background-color;");
        } else {
            css.append("-icons-color: rgb(");
            css.append((int)(color.getRed()*255));
            css.append(",");
            css.append((int)(color.getGreen()*255));
            css.append(",");
            css.append((int)(color.getBlue())*255);
            css.append(");");
        }
        setStyle(css.toString());
    }

    /**
     * Allows icon duplication. Since in the JavaFX scenegraph it is not possible to insert the same 
     * {@link Node} in multiple locations at the same time, this method allows for icon reuse in several places  
     */
	@Override public Icon duplicate() {
		return new Icon(fontFamily, character, size, color);
	}
}