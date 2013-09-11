package org.controlsfx.iconfont;

import javafx.scene.control.Label;
import javafx.scene.paint.Color;

public class Icon extends Label {

    private final String fontFamily;
    private final Character character;
    private double size;
    private Color color;

    public Icon(String fontFamily, Character character, double size, Color color) {
        super(character.toString());

        this.fontFamily = fontFamily;
        this.character = character;
        this.size = size;
        this.color = color;

        getStyleClass().add("icon-font");
        updateStyle();
    }
    
    public void setSize(double size) {
        this.size = size;
        updateStyle();
    }
    
    public void setColor(Color color) {
        this.color = color;
        updateStyle();
    }
    
    private void updateStyle() {
        StringBuilder css = new StringBuilder("-fx-font-family: "+ fontFamily +"; -fx-font-size: " + size + ";");
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
}