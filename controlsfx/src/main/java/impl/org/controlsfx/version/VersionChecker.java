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
package impl.org.controlsfx.version;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.sun.javafx.runtime.VersionInfo;

public class VersionChecker {
    
    private static final boolean isJavaFX8;
    private static final boolean isJavaFX8u20;
    
    private static final Version javaFXVersion;
    private static final Version controlsFXVersion;
    
    static {
        javaFXVersion = new Version(VersionInfo.getVersion());
        controlsFXVersion = new Version(getControlsFXVersionString());

        final String version = VersionInfo.getVersion();
        isJavaFX8 = "8.0.0".equals(version);
        isJavaFX8u20 = "8.0.0_20".equals(version);
    }

    private VersionChecker() {
        // no-op
    }
    
    public static void doVersionCheck() {
        // We keep ControlsFX bleeding edge, and we try to let our version numbers
        // do the talking. However, we can't always ensure people do the right 
        // thing, so here we will check the ControlsFX and JavaFX version numbers,
        // to ensure they match.
        // Fortunately, our system is simple at present: we use the
        // 'javaBuildMin' value from JavaFX to represent what we require. In other
        // words, ControlsFX 8.0.0 has no javaBuildMin, so it will work on 
        // JavaFX 8.0.0. Conversely, ControlsFX 8.0.6_20 has a javaBuildMin of
        // 20, which means that ControlsFX will only work on JavaFX 8u20.
        // We of course also expect the major versions to line up!
        
        if (javaFXVersion.getMajor() < controlsFXVersion.getMajor()) {
            throw new RuntimeException("ControlsFX Error: ControlsFX " +
                    controlsFXVersion + " requires at least Java " + controlsFXVersion.getMajor());
        }
        
        if (javaFXVersion.getJavaBuildMin() < controlsFXVersion.getJavaBuildMin()) {
            throw new RuntimeException("ControlsFX Error: ControlsFX " +
                    controlsFXVersion + " requires at least Java " + 
                    controlsFXVersion.getMajor() + "u" + controlsFXVersion.getJavaBuildMin());
        }
    }

    public static boolean isJavaFX8() {
        return isJavaFX8;
    }

    public static boolean isJavaFX8u20() {
        return isJavaFX8u20;
    }
    
    public static Version getJavaFXVersion() {
        return javaFXVersion;
    }
    
    public static Version getControlsFXVersion() {
        return controlsFXVersion;
    }
    
    private static String getControlsFXVersionString() {
        // firstly try to read from manifest
        try {
            Properties prop = new Properties();
            InputStream is = VersionChecker.class.getResourceAsStream("/META-INF/MANIFEST.MF");
            if (is != null) {
                prop.load(is);
                String version = prop.getProperty("Implementation-Version");
                if (version != null && !version.isEmpty()) {
                    return version;
                }
            }
        } catch (IOException e) {
            // no-op
        }
        
        // try to read it from the controlsfx-build.properties if running
        // from within an IDE
        try {
            Properties prop = new Properties();
            File file = new File("../controlsfx-build.properties");
            if (file.exists()) {
                prop.load(new FileReader(file));
                String version = prop.getProperty("controlsfx_version");
                if (version != null && !version.isEmpty()) {
                    return version;
                }
            }
        } catch (IOException e) {
            // no-op
        }
        
        return null;
    }
}
