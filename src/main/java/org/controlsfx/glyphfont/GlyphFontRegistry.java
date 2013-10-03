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
package org.controlsfx.glyphfont;


import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

import javafx.scene.Node;

/**
 * The glyph font registry automatically registers available fonts using a 
 * {@link ServiceLoader} facility, however it is also possible to register 
 * glyph fonts manually using the provided 
 * {@link GlyphFontRegistry#register(GlyphFont)} method. 
 * 
 * <p>Once registered fonts and their glyphs can be requested by name using the
 * {@link GlyphFontRegistry#glyph(String)} and
 * {@link GlyphFontRegistry#glyph(String, String)} methods. For example:
 * 
 * <pre>{@code
 * import static org.controlsfx.glyphfont.GlyphFontRegistry.glyph; 
 * new Button("", glyph("FontAwesome|TRASH")) 
 * }</pre>
 * 
 * <p>An ability to retrieve glyph node by combination of font name and glyph name 
 * extends to the {@link ActionProxy} graphic attribute, where the "font>" 
 * prefix should be used. For more information see {@link ActionProxy}.  
 */
public final class GlyphFontRegistry {
	
	private static Map<String, GlyphFont> fontMap = new HashMap<>();
	
	private static boolean isInited = false;
	
	private GlyphFontRegistry() {
		// no-op
	}
	
	private static void init() {
	    if (isInited) return;
	    isInited = true;
	    
	    // find all classes that implement GlyphFont and register them now
	    ServiceLoader<GlyphFont> loader = ServiceLoader.load(GlyphFont.class);
        for (GlyphFont font : loader) {
        	GlyphFontRegistry.register(font);
        }
	}
	
	/**
	 * Registers specified font
	 * @param font
	 */
	public static void register( GlyphFont font ) {
	    init();
		if (font != null ) {
			fontMap.put( font.getName(), font );
		}
	}
	
	/**
	 * Retrieve font by font
	 * @param fontName font name
	 * @return font or null if not found
	 */
	public static GlyphFont font( String fontName ) {
	    init();
		return fontMap.get(fontName);
	}
	
	/**
	 * Retrieve one glyph by font name and glyph name
	 * @param fontName font name
	 * @param glyphName glyph name
	 * @return glyph as a Node
	 */
	public static Node glyph( String fontName, String glyphName ) {
	    init();
		GlyphFont font = font(fontName);
		return font.create(glyphName);
	}
	
	/**
	 * Retrieve glyph by font name and glyph name using one string where font name an glyph name are separated by pipe
	 * @param fontAndGlyph font and glyph
	 * @return glyph as Node
	 */
	public static Node glyph( String fontAndGlyph ) {
	    init();
		String[] args = fontAndGlyph.split("\\|");
		return glyph( args[0], args[1]);
	}
}
