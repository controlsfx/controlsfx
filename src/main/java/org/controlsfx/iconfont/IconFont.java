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

import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.text.Font;

public class IconFont {

	private static double DEFAULT_ICON_SIZE = 16.0;
	
	private final String fontName;
	
	public IconFont( String fontName, java.io.InputStream in   ) {
		this.fontName = fontName;
		Font.loadFont(in, DEFAULT_ICON_SIZE);
	}
	
	public IconFont( String fontName, java.lang.String urlStr ) {
		this.fontName = fontName;
		Font.loadFont(urlStr, DEFAULT_ICON_SIZE);
	}
	
	public Node createNode(char c, double size) {
		return new Icon(c, size);
	}
	
	public Node createNode(char c) {
		return createNode(c,DEFAULT_ICON_SIZE);
	}
	
	public Image createImage(char c, double size) {
		return createNode(c, size).snapshot( new SnapshotParameters(), null);
	}
	
	public Image createImage(char c) {
		return createImage(c,DEFAULT_ICON_SIZE);
	}
	
	
	private class Icon extends Label {
		
		private final Character fontChar;
		private final double size;
		
		public Icon( Character fontChar, double size ) {
			super(fontChar.toString());
		    this.fontChar = fontChar;
		    this.size = size;
		    setFont(Font.font(fontName, size));
		}
		
		public Character getFontChar() {
			return fontChar;
		}
		
		public double getSize() {
			return size;
		}
		
		@Override public String toString() {
			return fontChar.toString();
		}
		
	}
	
}
