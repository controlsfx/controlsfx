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
import java.util.Properties;

import com.sun.javafx.runtime.VersionInfo;

public class VersionChecker {
    
    private static final String javaFXVersion;
    private static final String controlsFXSpecTitle;
    private static final String controlsFXSpecVersion;
    private static final String controlsFXImpVersion;
    
    private static final Package controlsFX;
    
    private static Properties props;
    
    static {
        controlsFX = VersionChecker.class.getPackage();
        
        javaFXVersion         = VersionInfo.getVersion();
        controlsFXSpecTitle   = getControlsFXSpecificationTitle();
        controlsFXSpecVersion = getControlsFXSpecificationVersion();
        controlsFXImpVersion  = getControlsFXImplementationVersion();
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
        // 'controlsFXSpec' value to represent what we require. In other
        // words, ControlsFX 8.0.0 has controlsFXSpecVersion of 8.0.0, so it will work on 
        // JavaFX 8.0.0 and later versions. Conversely, ControlsFX 8.0.6_20 has a controlsFXSpecVersion of
        // 8.0.20 (controlsFXSpecTitle of Java 8u20), which means that ControlsFX will only work on JavaFX 8u20
        // and later versions.
        
        if (controlsFXSpecVersion == null) {
            // FIXME temporary fix to allow ControlsFX to work when run inside
            // an IDE (i.e. for developers of ControlsFX).
            return;
        }
        
        Comparable[] splitSpecVersion = toComparable(controlsFXSpecVersion.split("\\.")); //$NON-NLS-1$
        
        // javaFXVersion may contain '-' like 8.0.20-ea so replace them with '.' before splitting.
        Comparable[] splitJavaVersion = toComparable(javaFXVersion.replace('-', '.').split("\\.")); //$NON-NLS-1$

        boolean notSupportedVersion = false;

        // Check Major Version
        if (splitSpecVersion[0].compareTo(splitJavaVersion[0]) > 0) {
            notSupportedVersion = true;
        } else if (splitSpecVersion[0].compareTo(splitJavaVersion[0]) == 0) {
            // Check Minor Version
            if (splitSpecVersion[1].compareTo(splitJavaVersion[2])>0) {
                notSupportedVersion = true;
            }
        }

        if (notSupportedVersion) {
            throw new RuntimeException("ControlsFX Error: ControlsFX " + //$NON-NLS-1$
                controlsFXImpVersion + " requires at least " + controlsFXSpecTitle); //$NON-NLS-1$
        }
    }

    private static Comparable<Comparable>[] toComparable(String[] tokens) {
        Comparable[] ret= new Comparable[tokens.length];
        for (int i = 0; i<tokens.length; i++) {
            String token = tokens[i];
            try {
                ret[i] = new Integer(token);
            }
            catch (NumberFormatException e) {
                ret[i] = token;
            }
        }
        return ret;
    }

    private static String getControlsFXSpecificationTitle() {
        // firstly try to read from manifest
        try {
            return controlsFX.getSpecificationTitle();
        } catch (NullPointerException e) {
            // no-op
        }
        
        // try to read it from the controlsfx-build.properties if running
        // from within an IDE
        return getPropertyValue("controlsfx_specification_title"); //$NON-NLS-1$
        
        
//        try {
//            Properties prop = new Properties();
//            File file = new File("../controlsfx-build.properties");
//            if (file.exists()) {
//                prop.load(new FileReader(file));
//                String version = prop.getProperty("controlsfx_specification_title");
//                if (version != null && !version.isEmpty()) {
//                    return version;
//                }
//            }
//        } catch (IOException e) {
//            // no-op
//        }
//        
//        return null;
    }
    
    private static String getControlsFXSpecificationVersion() {
        
        // firstly try to read from manifest
        try {
            return controlsFX.getSpecificationVersion();
        } catch (NullPointerException e) {
            // no-op
        }
        
        // try to read it from the controlsfx-build.properties if running
        // from within an IDE
        return getPropertyValue("controlsfx_specification_title"); //$NON-NLS-1$
        
//        try {
//            Properties prop = new Properties();
//            File file = new File("../controlsfx-build.properties");
//            if (file.exists()) {
//                prop.load(new FileReader(file));
//                String version = prop.getProperty("controlsfx_specification_version");
//                if (version != null && !version.isEmpty()) {
//                    return version;
//                }
//            }
//        } catch (IOException e) {
//            // no-op
//        }
//        
//        return null;
    }
    
    private static String getControlsFXImplementationVersion() {
        
        // firstly try to read from manifest
        try {
            return controlsFX.getImplementationVersion();
        } catch (NullPointerException e) {
            // no-op
        }
        
        // try to read it from the controlsfx-build.properties if running
        // from within an IDE
        
        return getPropertyValue("controlsfx_specification_title") + //$NON-NLS-1$
        	   getPropertyValue("artifact_suffix"); //$NON-NLS-1$
        
        
//        try {
//            Properties prop = new Properties();
//            File file = new File("../controlsfx-build.properties");
//            if (file.exists()) {
//                prop.load(new FileReader(file));
//                String version = prop.getProperty("controlsfx_version");
//                if (version != null && !version.isEmpty()) {
//                    return version;
//                }
//            }
//        } catch (IOException e) {
//            // no-op
//        }
//        
//        return null;
    }
    
    private static synchronized String getPropertyValue(String key) {
    	
    	if ( props == null ) {
        	try {
                File file = new File("../controlsfx-build.properties"); //$NON-NLS-1$
                if (file.exists()) {
                    props.load(new FileReader(file));
                }
            } catch (IOException e) {
                // no-op
            }
    	}
    	return props.getProperty(key);
    }
}
