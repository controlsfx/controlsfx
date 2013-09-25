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

import javafx.scene.paint.Color;
import javafx.scene.text.Font;

/**
 *  Represents icon font, which can be loaded locally or from specified URL.
 *  {@link Icon}s can be created easily using specified character 
 *
 */
public class IconFont {
    
    static {
        StyleManager.getInstance().addUserAgentStylesheet(
                IconFont.class.getResource("iconfont.css").toExternalForm());
    }

	private final String fontName;
	
	private final double defaultSize;
	private double size;
	private Color color;
	

	/**
	 * Loads icon font from specified {@link InputStream}
	 * @param fontName icon font name
	 * @param defaultSize default font size
	 * @param in input stream to load the font from
	 */
	public IconFont( String fontName, int defaultSize, InputStream in   ) {
		this.fontName = fontName;
		this.defaultSize = defaultSize;
		this.size = defaultSize;
		Font.loadFont(in, -1);
	}
	
	/**
	 * Load icon font from specified {@link URL} 
	 * @param fontName icon font name
	 * @param defaultSize default font size
	 * @param urlStr {@link URL} to load the font from
	 */
	public IconFont( String fontName, int defaultSize, String urlStr ) {
		this.fontName = fontName;
		this.defaultSize = defaultSize;
		this.size = defaultSize;
		Font.loadFont(urlStr, -1);
	}
	
	/**
	 * Returns the default font size
	 * @return default font size
	 */
	public double getDefaultSize() {
		return defaultSize;
	}
	
	/**
	 * Sets font size
	 * @param size
	 * @return font instance so the calls can be chained
	 */
	public IconFont fontSize(double size) {
	    this.size = size;
	    return this;
	}
	
	/**
	 * Sets font color
	 * @param color
	 * @return font instance so the calls can be chained
	 */
	public IconFont fontColor(Color color) {
	    this.color = color;
	    return this;
	}
	
	/**
	 * Creates and instance of {@link Icon} using specified font character
	 * @param character font character
	 * @return instance of {@link Icon}
	 */
	public Icon create(char character) {
	    return new Icon(fontName, character, size, color);
	}
}
