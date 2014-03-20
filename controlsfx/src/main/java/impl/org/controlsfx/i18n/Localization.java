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

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class Localization {

	private Localization() {
	}

	public static final String KEY_PREFIX = "@@"; //$NON-NLS-1$

	private static final String LOCALE_BUNDLE_NAME = "controlsfx"; //$NON-NLS-1$
	private static Locale locale = null;

	/**
	 * Returns the Locale object that is associated with ControlsFX.
	 * 
	 * @return the global ControlsFX locale
	 */
	public static final Locale getLocale() {
		// following allows us to have a "dynamic" locale based on OS/JDK
		return locale == null ? Locale.getDefault() : locale;
	}

	/**
	 * Sets locale which will be used as ControlsFX locale
	 * 
	 * @param newLocale
	 *            null is allowed and will be interpreted as default locale
	 */
	public static final void setLocale(final Locale newLocale) {
		locale = newLocale;
	}

	private static Locale resourceBundleLocale = null; // has to be null initially
	private static ResourceBundle resourceBundle = null;

	private static synchronized final ResourceBundle getLocaleBundle() {

		Locale currentLocale = getLocale();
		if (!currentLocale.equals(resourceBundleLocale)) {
			resourceBundleLocale = currentLocale;
			resourceBundle = ResourceBundle.getBundle(LOCALE_BUNDLE_NAME,
					resourceBundleLocale, Localization.class.getClassLoader());
		}
		return resourceBundle;

	}

	/**
	 * Returns a string localized using currently set locale
	 * 
	 * @param key resource bundle key
	 * @return localized text or formatted key if not found
	 */
	public static final String getString(final String key) {
		try {
			return getLocaleBundle().getString(key);
		} catch (MissingResourceException ex) {
			return String.format("<%s>", key); //$NON-NLS-1$
		}
	}

	/**
	 * Converts text to localization key,
	 * currently by prepending it with the KEY_PREFIX
	 * 
	 * @param text
	 * @return localization key
	 */
	public static final String asKey(String text) {
		return KEY_PREFIX + text;
	}

	/**
	 * Checks if the text is a localization key
	 * 
	 * @param text
	 * @return true if text is a localization key
	 */
	public static final boolean isKey(String text) {
		return text != null && text.startsWith(KEY_PREFIX);
	}

	/**
	 * Tries to localize the text. If the text is a localization key - and attempt will be made to 
	 * use it for localization, otherwise the text is returned as is
	 * 
	 * @param text
	 * @return
	 */
	public static String localize(String text) {
		return isKey(text) ? getString(text.substring(KEY_PREFIX.length())
				.trim()) : text;
	}

}
