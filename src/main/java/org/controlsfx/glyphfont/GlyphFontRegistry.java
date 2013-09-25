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
 * Glyph Font Registry. 
 * Automatically registers available font packs using {@link ServiceLoader} facility.   
 * Glyph font pack can also be registered manually using 'register' method.
 * <br/>
 * From than on fonts and their glyphs can be requested by name.
 */
public final class GlyphFontRegistry {
	
	private static Map<String, GlyphFontPack> packMap = new HashMap<>();
	
	private static boolean isInited = false;
	
	private GlyphFontRegistry() {
		// no-op
	}
	
	private static void init() {
	    if (isInited) return;
	    isInited = true;
	    
	    // find all classes that implement GlyphFontPack and register them now
	    ServiceLoader<GlyphFontPack> loader = ServiceLoader.load(GlyphFontPack.class);
        for (GlyphFontPack fontPack : loader) {
        	GlyphFontRegistry.register(fontPack);
        }
	}
	
	/**
	 * Registers specified font pack
	 * @param pack font pack
	 */
	public static void register( GlyphFontPack pack ) {
	    init();
		if (pack != null ) {
			packMap.put( pack.getFontName(), pack );
		}
	}
	
	/**
	 * Retrieve font pack by font name
	 * @param fontName font name
	 * @return font pack or null if not found
	 */
	public static GlyphFontPack pack( String fontName ) {
	    init();
		return packMap.get(fontName);
	}
	
	/**
	 * Retrieve one glyph by font name and glyph name
	 * @param fontName font name
	 * @param glyphName glyph name
	 * @return glyph as a Node
	 */
	public static Node glyph( String fontName, String glyphName ) {
	    init();
		GlyphFontPack pack = pack(fontName);
		return pack.getFont().create(pack.getGlyphs().get(glyphName));
	}
	
	/**
	 * Retrieve glyph by font name and glyph name using one string where font name an glyph name are separated by pipe
	 * @param fontAndGlyph font and glyph
	 * @return glyoh as Node
	 */
	public static Node glyph( String fontAndGlyph ) {
	    init();
		String[] args = fontAndGlyph.split("\\|");
		return glyph( args[0], args[1]);
	}
}
