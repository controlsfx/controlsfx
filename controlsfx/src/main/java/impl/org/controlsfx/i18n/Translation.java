/**
 * Copyright (c) 2014, ControlsFX
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
package impl.org.controlsfx.i18n;

import java.nio.file.Path;
import java.util.Locale;

public class Translation implements Comparable<Translation> {

    private final String localeString;
    private final Locale locale;
    private final Path path;
    
    public Translation(String locale, Path path) {
        this.localeString = locale;
        this.path = path;
        
        String[] split = localeString.split("_"); //$NON-NLS-1$
        if (split.length == 1) {
            this.locale = new Locale(localeString);
        } else if (split.length == 2) {
            this.locale = new Locale(split[0], split[1]);
        } else if (split.length == 3) {
            this.locale = new Locale(split[0], split[1], split[2]);
        } else {
            throw new IllegalArgumentException("Unknown locale string '" + locale + "'"); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }
    
    public final String getLocaleString() {
        return localeString;
    }
    
    public final Locale getLocale() {
        return locale;
    }
    
    public final Path getPath() {
        return path;
    }
    
    @Override public String toString() {
        return localeString;
    }
    
    @Override public int compareTo(Translation o) {
        if (o == null) return 1;
        return localeString.compareTo(o.localeString);
    }
}
