package org.controlsfx.tools;

import com.sun.javafx.Utils;
/**
 * Represents operating system with appropriate properties 
 *
 */
public enum Platform {
    
    WINDOWS("windows"),
    OSX("mac"),
    UNIX("unix"),
    UNKNOWN("");
    
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
     * Returns current OS 
     * @return
     */
    public static Platform getCurrent() {
        return current;
    }
    
    private static Platform getCurrentPlatform() {
        if ( Utils.isWindows() ) return WINDOWS;
        if ( Utils.isMac() )     return OSX;
        if ( Utils.isUnix() )    return UNIX;
        return UNKNOWN;
    }
    
}
