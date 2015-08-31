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
package org.controlsfx.tools;

import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * Represents operating system with appropriate properties 
 *
 */
public enum Platform {
    
    WINDOWS("windows"), //$NON-NLS-1$
    OSX("mac"), //$NON-NLS-1$
    UNIX("unix"), //$NON-NLS-1$
    UNKNOWN(""); //$NON-NLS-1$
    
    private static Platform current = getCurrentPlatform();
    
    private String platformId;
    
    Platform( String platformId ) {
        this.platformId = platformId;
    }
    
    /**
     * Returns platform id. Usually used to specify platform dependent styles
     * @return platform id
     */
    public String getPlatformId() {
        return platformId;
    }
    
    /**
     * @return the current OS.
     */
    public static Platform getCurrent() {
        return current;
    }
    
    private static Platform getCurrentPlatform() {
        String osName = System.getProperty("os.name");
        if ( osName.startsWith("Windows") ) return WINDOWS;        
        if ( osName.startsWith("Mac") )     return OSX;
        if ( osName.startsWith("SunOS") )   return UNIX;
        if ( osName.startsWith("Linux") ) {
            String javafxPlatform = AccessController.doPrivileged(new PrivilegedAction<String>() {
                @Override
                public String run() {
                    return System.getProperty("javafx.platform");
                }
            });
            if (! ( "android".equals(javafxPlatform) || "Dalvik".equals(System.getProperty("java.vm.name")) ) ) // if not Android
                return UNIX;
        }
        return UNKNOWN;
    }
    
}
