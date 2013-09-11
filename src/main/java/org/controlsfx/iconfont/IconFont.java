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

import java.io.InputStream;

import com.sun.javafx.css.StyleManager;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class IconFont {
    
    static {
        StyleManager.getInstance().addUserAgentStylesheet(
                IconFont.class.getResource("iconfont.css").toExternalForm());
    }

	private static double DEFAULT_ICON_SIZE = 16.0;
	
	private final String fontName;
	
	private Character character;
	private double size = DEFAULT_ICON_SIZE;
	private Color color;
	
	public IconFont( String fontName, InputStream in   ) {
		this.fontName = fontName;
		Font.loadFont(in, DEFAULT_ICON_SIZE);
	}
	
	public IconFont( String fontName, String urlStr ) {
		this.fontName = fontName;
		Font.loadFont(urlStr, DEFAULT_ICON_SIZE);
	}
	
	public IconFont create(char c) {
	    this.character = c;
	    
	    // reset other properties to default
	    size = DEFAULT_ICON_SIZE;
	    color = null;
	    
	    return this;
	}
	
	public IconFont size(double size) {
	    this.size = size;
	    return this;
	}
	
	public IconFont color(Color color) {
	    this.color = color;
	    return this;
	}
	
	public Node build() {
	    return new Icon(character, size, color);
	}
	
	
	private class Icon extends Label {
		
		private final Character _character;
		private final double _size;
		private final Color _color;
		
		public Icon(Character character, double size, Color color) {
			super(character.toString());
			
			this._character = character;
			this._size = size;
			this._color = color;
			
			StringBuilder css = new StringBuilder("-fx-font-family: "+ fontName +"; -fx-font-size: " + _size + ";");
			if (color == null) {
			    css.append("-icons-color: -fx-text-background-color;");
			} else {
			    css.append("-icons-color: rgb(");
			    css.append((int)(_color.getRed()*255));
			    css.append(",");
			    css.append((int)(_color.getGreen()*255));
			    css.append(",");
			    css.append((int)(_color.getBlue())*255);
			    css.append(");");
			}
		    setStyle(css.toString());
		    getStyleClass().add("icon-font");
		}
	}
}
