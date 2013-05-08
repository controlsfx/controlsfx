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
package org.controlsfx.dialogs;

import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedExceptionAction;
import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;

/**
 *
 */
class DialogResources {

    // Localization strings.
    private static ResourceBundle rbFX;

    static {
        reset();
    }

    static void reset() {
        rbFX = ResourceBundle.getBundle("impl.org.controlsfx.dialogs.resources.oxygen.dialog-resources");
    }


    /**
     * Method to get an internationalized string from the deployment resource.
     */
    static String getMessage(String key) {
        try {
            return rbFX.getString(key);
        } catch (MissingResourceException ex) {
            // Do not trace this exception, because the key could be
            // an already translated string.
            System.out.println("Failed to get string for key '" + key + "'");
            return key;
        }
    }

    /**
     * Returns a string from the resources
     */
    static String getString(String key) {
        try {
            return rbFX.getString(key);
        } catch (MissingResourceException mre) {
            // Do not trace this exception, because the key could be
            // an already translated string.
            System.out.println("Failed to get string for key '" + key + "'");
            return key;
        }
    }

    /**
     * Returns a string from a resource, substituting argument 1
     */
    static String getString(String key, Object... args) {
        return MessageFormat.format(getString(key), args);
    }


    /**
     * Returns an <code>ImageView</code> given an image file name or resource name
     */
    static public Image getImage(final String key) {
        try {
            return AccessController.doPrivileged(
                    new PrivilegedExceptionAction<Image>()   {
                        @Override public Image run() {
                            return getImage_(key);
                        }
                    });
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    static public Image getImage_(String key) {
        String resourceName = getString(key);
        URL url = DialogResources.class.getResource(resourceName);
        if (url == null) {
            System.out.println("Can't create ImageView for key '" + key + 
                    "', which has resource name '" + resourceName + 
                    "' and URL 'null'");
            return null;
        }
        return getImage(url);
    }

    static public Image getImage(URL url) {
//        if (url.toString().endsWith(".svg")) {
//            WritableImage image = new WritableImage(48, 48);
//            SVGLoader.loadSVGImage(url, image);
//            return image;
//        }
        return new Image(url.toString());
    }
}