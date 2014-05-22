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

public class Version {
    private final int major;
    private final int minor;
    private final int increment;
    private int javaBuildMin;
    private final boolean snapshot;
    
    public Version(String version) {
        System.out.println("Version: " + version);
        snapshot = version.contains("SNAPSHOT");
        version = snapshot ? version.substring(0, version.indexOf("-SNAPSHOT")) : version;
        
        javaBuildMin = version.contains("_") ? Integer.valueOf(version.substring(version.indexOf("_") + 1)) : -1;
        version = javaBuildMin != -1 ? version.substring(0, version.indexOf("_")) : version;
        
        String[] splitVersion = version.split("\\.");
        if (splitVersion.length != 3) {
            throw new RuntimeException("Unknown version string: " + version);
        }
        major = Integer.valueOf(splitVersion[0]);
        minor = Integer.valueOf(splitVersion[1]);
        increment = Integer.valueOf(splitVersion[2]);
    }
    
    public final int getMajor() {
        return major;
    }
    
    public final int getMinor() {
        return minor;
    }
    
    public final int getIncrement() {
        return increment;
    }
    
    public final int getJavaBuildMin() {
        return javaBuildMin;
    }
    
    @Override public String toString() {
        return major + "." + minor + "." + increment + 
                (javaBuildMin == -1 ? "" : "_" + javaBuildMin) + 
                (snapshot ? "-SNAPSHOT" : "");
    }
}
