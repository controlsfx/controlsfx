/**
 * Copyright (c) 2013, 2020 ControlsFX
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

import javafx.scene.text.Font;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 *  Represents a glyph font, which can be loaded locally or from a specified URL.
 *  {@link Glyph}s can be created easily using specified character defined in the
 *  font. For example, &#92;uf013 in FontAwesome is used to represent
 *  a gear icon.
 *
 *  <p>To simplify glyph customization, methods can be chained, for example:
 *
 *  <pre>
 *  Glyph glyph = fontAwesome.create('&#92;uf013').size(28).color(Color.RED); //GEAR
 *  </pre>
 *
 *  <p>Here's a screenshot of two font packs being used to render images into
 *  JavaFX Button controls:
 *
 * <br>
 * <center><img src="glyphFont.png" alt="Screenshot of GlyphFont"></center>
 */
public class GlyphFont {

    /***************************************************************************
     *                                                                         *
     * Private fields                                                          *
     *                                                                         *
     **************************************************************************/

    private final Map<String, Character> namedGlyphs = new HashMap<>();
    private final Runnable fontLoader;
    private final String fontName;
    private final double defaultSize;

    private boolean  fontLoaded = false;


    /***************************************************************************
     *                                                                         *
     * Constructors                                                            *
     *                                                                         *
     **************************************************************************/

    /**
     * Loads glyph font from specified {@link InputStream}
     * @param fontName glyph font name
     * @param defaultSize default font size
     * @param in input stream to load the font from
     */
    public GlyphFont( String fontName, int defaultSize, final InputStream in) {
        this(fontName, defaultSize, in, false);
    }

    /**
     * Load glyph font from specified URL.
     * Example for a local file:
     * "file:///C:/Users/Bob/Fonts/icomoon.ttf"
     * "file:///Users/Bob/Fonts/icomoon.ttf"
     *
     * @param fontName glyph font name
     * @param defaultSize default font size
     * @param urlStr A URL to load the font from
     */
    public GlyphFont( String fontName, int defaultSize, final String urlStr) {
        this(fontName, defaultSize, urlStr, false);
    }

    /**
     * Loads glyph font from specified {@link InputStream}
     * @param fontName glyph font name
     * @param defaultSize default font size
     * @param in input stream to load the font from
     * @param lazyLoad If true, the font will only be loaded when accessed
     */
    public GlyphFont( String fontName, int defaultSize, final InputStream in, boolean lazyLoad) {
        this(fontName, defaultSize, () -> {
            Font.loadFont(in, -1);
        }, lazyLoad);
    }

    /**
     * Load glyph font from specified URL.
     * Example for a local file:
     * "file:///C:/Users/Bob/Fonts/icomoon.ttf"
     * "file:///Users/Bob/Fonts/icomoon.ttf"
     *
     * @param fontName glyph font name
     * @param defaultSize default font size
     * @param urlStr A URL to load the font from
     * @param lazyLoad If true, the font will only be loaded when accessed
     */
    public GlyphFont( String fontName, int defaultSize, final String urlStr, boolean lazyLoad) {
        this(fontName, defaultSize, () -> {
            Font.loadFont(urlStr, -1);
        }, lazyLoad);
    }

    /**
     * Creates a GlyphFont
     * @param fontName
     * @param defaultSize
     * @param fontLoader
     * @param lazyLoad
     */
    private GlyphFont(String fontName, int defaultSize, Runnable fontLoader, boolean lazyLoad){
        this.fontName = fontName;
        this.defaultSize = defaultSize;
        this.fontLoader = fontLoader;

        if(!lazyLoad){
            ensureFontIsLoaded();
        }
    }

    /***************************************************************************
     *                                                                         *
     * Public API                                                              *
     *                                                                         *
     **************************************************************************/

    /**
     * Returns font name
     * @return font name
     */
    public String getName() {
        return fontName;
    }

    /**
     * Returns the default font size
     * @return default font size
     */
    public double getDefaultSize() {
        return defaultSize;
    }


    /**
     * Creates an instance of {@link Glyph} using specified font character
     * @param character font character
     * @return instance of {@link Glyph}
     */
    public Glyph create(char character) {
        return new Glyph(fontName, character);
    }

    /**
     * Creates an instance of {@link Glyph} using glyph name
     * @param glyphName glyph name
     * @return glyph by its name or null if name is not found
     */
    public Glyph create(String glyphName) {
        return new Glyph(fontName, glyphName);
    }

    /**
     *  Creates an instance of {@link Glyph} using a known Glyph enum value
     * @param glyph
     */
    public Glyph create(Enum<?> glyph) {
        return new Glyph(fontName, glyph);
    }

    /**
     * Returns the character code which is mapped to this Name.
     * If no match is found, NULL is returned.
     * @param glyphName
     */
    public Character getCharacter(String glyphName){
        return namedGlyphs.get(glyphName.toUpperCase());
    }


    /**
     * Registers all given characters with their name.
     * @param namedCharacters
     */
    public void registerAll(Iterable<? extends INamedCharacter> namedCharacters){
        for (INamedCharacter e:  namedCharacters) {
            register(e.name(), e.getChar());
        }
    }

    /**
     * Registers the given name-character mapping
     * @param name
     * @param character
     */
    public void register(String name, Character character){
        namedGlyphs.put(name.toUpperCase(), character);
    }

    /***************************************************************************
     *                                                                         *
     * Internal methods                                                        *
     *                                                                         *
     **************************************************************************/

    /**
     * Ensures that the font is loaded
     */
    synchronized void ensureFontIsLoaded(){
        if ( !fontLoaded ) {
            fontLoader.run();
            fontLoaded = true;
        }
    }
}
