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
		    setStyle("-fx-font-family: "+ fontName +"; -fx-font-size: " + size + ";");
		}
		
		public Character getFontChar() {
			return fontChar;
		}
		
		public double getSize() {
			return size;
		}
		
		@Override
		public String toString() {
			return fontChar.toString();
		}
		
	}
	
}
