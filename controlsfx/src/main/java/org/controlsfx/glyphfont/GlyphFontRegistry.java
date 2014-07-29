/**
 * Copyright (c) 2013,2014 ControlsFX
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


import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * The glyph font registry automatically registers available fonts using a
 * {@link ServiceLoader} facility, however it is also possible to register
 * glyph fonts manually using the provided
 * {@link GlyphFontRegistry#register(GlyphFont)} method.
 *
 * <p>Once registered, fonts can be requested by name using the
 * {@link GlyphFontRegistry#font(String)} method.
 *
 * Please refer to the {@link GlyphFont} documentation
 * to learn how to use a font.
 *
 */
public final class GlyphFontRegistry {

    /***************************************************************************
     *                                                                         *
     * Private fields                                                          *
     *                                                                         *
     **************************************************************************/

    private static Map<String, GlyphFont> fontMap = new HashMap<>();

    /***************************************************************************
     *                                                                         *
     * Constructors                                                            *
     *                                                                         *
     **************************************************************************/

    static {
        // find all classes that implement GlyphFont and register them now
        ServiceLoader<GlyphFont> loader = ServiceLoader.load(GlyphFont.class);
        for (GlyphFont font : loader) {
            GlyphFontRegistry.register(font);
        }
    }

    /**
     * Private constructor since static class
     */
    private GlyphFontRegistry() {
        // no-op
    }

    /***************************************************************************
     *                                                                         *
     * Public API                                                              *
     *                                                                         *
     **************************************************************************/

    /**
     * Registers the specified font as default GlyphFont
     * @param familyName The name of this font.
     * @param uri The location where it can be loaded from.
     * @param defaultSize The default font size
     */
    public static void register(String familyName, String uri, int defaultSize){
        register(new GlyphFont(familyName, defaultSize, uri));
    }

    /**
     * Registers the specified font as default GlyphFont
     * @param familyName The name of this font.
     * @param in Inputstream of the font data
     * @param defaultSize The default font size
     */
    public static void register(String familyName, InputStream in, int defaultSize){
        register(new GlyphFont(familyName, defaultSize, in));
    }

    /**
     * Registers the specified font
     * @param font
     */
    public static void register( GlyphFont font ) {
        if (font != null ) {
            fontMap.put( font.getName(), font );
        }
    }

    /**
     * Retrieve font by its family name
     * @param familyName family name of the font
     * @return font or null if not found
     */
    public static GlyphFont font( String familyName ) {
        GlyphFont font = fontMap.get(familyName);
        if(font != null) {
            font.ensureFontIsLoaded();
        }
        return font;
    }
}
