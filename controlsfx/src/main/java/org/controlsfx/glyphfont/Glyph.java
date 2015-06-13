/**
 * Copyright (c) 2013, 2015 ControlsFX
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
package org.controlsfx.glyphfont;

import java.util.Optional;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.paint.*;
import javafx.scene.text.Font;

import org.controlsfx.control.action.Action;
import org.controlsfx.tools.Duplicatable;

/**
 * Represents one glyph from the font.
 * The glyph is actually a label showing one character from the specified font. It can be used as 'graphic' on any UI
 * control or {@link Action}. It can also directly be used in FXML.
 *
 * Examples:
 *
 * <pre>{@code
 * new Button("", new Glyph("FontAwesome", "BEER"))
 * }</pre>
 *
 * <pre>{@code
 * new Button("", new Glyph("FontAwesome", FontAwesome.Glyph.BEER))
 * }</pre>
 *
 * Thy Glyph-Class also offers a fluent API to customize the look of the Glyph.
 * For example, you can set the color {@link #color(javafx.scene.paint.Color)} or
 * also add effects such as {@link #useHoverEffect()}
 *
 * <p>An ability to retrieve glyph node by combination of font name and glyph name
 * extends to the {@link org.controlsfx.control.action.ActionProxy} graphic attribute, where the "font&gt;"
 * prefix should be used. For more information see {@link org.controlsfx.control.action.ActionProxy}.
 *
 */
public class Glyph extends Label implements Duplicatable<Glyph> {

    /***************************************************************************
     *                                                                         *
     * Static creators                                                         *
     *                                                                         *
     **************************************************************************/

    /**
     * Retrieve glyph by font name and glyph name using one string
     * where font name an glyph name are separated by pipe.
     *
     * @param fontAndGlyph The font and glyph separated by a pipe. Example: "FontAwesome|STAR"
     * @return A instance of a Glyph node
     */
    public static Glyph create(String fontAndGlyph) {
        String[] args = fontAndGlyph.split("\\|"); //$NON-NLS-1$
        return new Glyph(args[0], args[1]);
    }


    /***************************************************************************
     *                                                                         *
     * Private fields                                                          *
     *                                                                         *
     **************************************************************************/

    public final static String DEFAULT_CSS_CLASS = "glyph-font";    //$NON-NLS-1$
    public final static String STYLE_GRADIENT = "gradient";         //$NON-NLS-1$
    public final static String STYLE_HOVER_EFFECT = "hover-effect"; //$NON-NLS-1$

    private final ObjectProperty<Object> icon = new SimpleObjectProperty<>();

    /***************************************************************************
     *                                                                         *
     * Constructors                                                            *
     *                                                                         *
     **************************************************************************/

    /**
     * Empty Constructor (used by FXML)
     */
    public Glyph(){
        getStyleClass().add(DEFAULT_CSS_CLASS);

        icon.addListener(x -> updateIcon());
        fontProperty().addListener(x -> updateIcon());
    }

    /**
     * Creates a new Glyph
     * @param fontFamily The family name of the font. Example: "FontAwesome"
     * @param unicode The Unicode character of the glyph
     */
    public Glyph(String fontFamily, char unicode) {
        this();
        setFontFamily(fontFamily);
        setTextUnicode(unicode);
    }

    /**
     * Creates a new Glyph
     * @param fontFamily The family name of the font. Example: "FontAwesome"
     * @param icon The icon - which can be the name (String) or Enum value.
     *             Example: FontAwesome.Glyph.BEER
     */
    public Glyph(String fontFamily, Object icon) {
        this();
        setFontFamily(fontFamily);
        setIcon(icon);
    }

    /***************************************************************************
     *                                                                         *
     * Public API                                                              *
     *                                                                         *
     **************************************************************************/

    /**
     * Sets the glyph icon font family
     * @param fontFamily A font family name
     * @return Returns this instance for fluent API
     */
    public Glyph fontFamily(String fontFamily){
        setFontFamily(fontFamily);
        return this;
    }

    /**
     * Sets the glyph color
     * @param color
     * @return Returns this instance for fluent API
     */
    public Glyph color(Color color){
        setColor(color);
        return this;
    }

    /**
     * Sets glyph size
     * @param size
     * @return Returns this instance for fluent API
     */
    public Glyph size(double size) {
        setFontSize(size);
        return this;
    }

    /**
     * Sets glyph size using size factor based on default font size
     * @param factor
     * @return Returns this instance for fluent API
     */
    public Glyph sizeFactor(int factor) {
    	Optional.ofNullable(GlyphFontRegistry.font(getFont().getFamily())).ifPresent( glyphFont ->{
    		setFontSize(glyphFont.getDefaultSize()* (factor < 1? 1: factor));
    	});
    	return this;
    }

    

    /**
     * Adds the hover effect style
     * @return Returns this instance for fluent API
     */
    public Glyph useHoverEffect(){
        this.getStyleClass().add(Glyph.STYLE_HOVER_EFFECT);
        return this;
    }

    /**
     * Adds the gradient effect style
     * @return Returns this instance for fluent API
     */
    public Glyph useGradientEffect(){

        if(getTextFill() instanceof Color){
            Color currentColor = (Color)getTextFill();

            /*
             TODO
             Do this in code:
            -fx-text-fill: linear-gradient(to bottom, derive(-fx-text-fill,20%) 10%, derive(-fx-text-fill,-40%) 80%);
             */
            Stop[] stops = new Stop[] { new Stop(0, Color.BLACK), new Stop(1, currentColor)};
            LinearGradient lg1 = new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE, stops);
            setTextFill(lg1);
        }

        this.getStyleClass().add(Glyph.STYLE_GRADIENT);
        return this;
    }


    /**
     * Allows glyph duplication. Since in the JavaFX scenegraph it is not possible to insert the same
     * {@link Node} in multiple locations at the same time, this method allows for glyph reuse in several places
     */
    @Override public Glyph duplicate() {
        Paint color = getTextFill();
        Object icon = getIcon();
        ObservableList<String> classes = getStyleClass();
        return new Glyph(){{
            setIcon(icon);
            setTextFill(color);
            getStyleClass().addAll(classes);
        }}
                .fontFamily(getFontFamily())
                .size(getFontSize());
    }

    /***************************************************************************
     *                                                                         *
     * Properties                                                              *
     *                                                                         *
     **************************************************************************/

    /**
     * Sets the font family of this glyph
     * Font size is reset to default glyph font size
     */
    public void setFontFamily(String family){
        if( !getFont().getFamily().equals(family)){
        	Optional.ofNullable(GlyphFontRegistry.font(family)).ifPresent( glyphFont -> {
        		glyphFont.ensureFontIsLoaded(); // Make sure font is loaded 
        		Font newFont = Font.font(family, glyphFont.getDefaultSize()); // Reset to default font size
                setFont(newFont);
        	});
        }
    }

    /**
     * Gets the font family of this glyph
     */
    public String getFontFamily(){
        return getFont().getFamily();
    }

    /**
     * Sets the font size of this glyph
     */
    public void setFontSize(double size){
        Font newFont = Font.font(getFont().getFamily(), size);
        setFont(newFont);
    }

    /**
     * Gets the font size of this glyph
     */
    public double getFontSize(){
        return getFont().getSize();
    }

    /**
     * Set the Color of this Glyph
     */
    public void setColor(Color color){
        setTextFill(color);
    }

    /**
     * The icon name property.
     *
     * This must either be a Glyph-Name (either string or enum value) known by the GlyphFontRegistry.
     * Alternatively, you can directly submit a unicode character here.
     */
    public ObjectProperty<Object> iconProperty(){
        return icon;
    }

    /**
     * Set the icon to display.
     * @param iconValue This can either be the Glyph-Name, Glyph-Enum Value or a unicode character representing the sign.
     */
    public void setIcon(Object iconValue){
        icon.set(iconValue);
    }

    public Object getIcon(){
        return icon.get();
    }

    /***************************************************************************
     *                                                                         *
     * Private methods                                                         *
     *                                                                         *
     **************************************************************************/


    /**
     * This updates the text with the correct unicode value
     * so that the desired icon is displayed.
     */
    private void updateIcon(){

        Object iconValue = getIcon();

        if(iconValue != null) {
            if(iconValue instanceof Character){
                setTextUnicode((Character)iconValue);
            }else {
                GlyphFont glyphFont = GlyphFontRegistry.font(getFontFamily());
                if (glyphFont != null) {
                    String name = iconValue.toString();
                    Character unicode = glyphFont.getCharacter(name);
                    if (unicode != null) {
                        setTextUnicode(unicode);
                    } else {
                        // Could not find a icon with this name
                        setText(name);
                    }
                }
            }
        }
    }

    /**
     * Sets the given char as text
     * @param unicode
     */
    private void setTextUnicode(char unicode){
        setText(String.valueOf(unicode));
    }
}