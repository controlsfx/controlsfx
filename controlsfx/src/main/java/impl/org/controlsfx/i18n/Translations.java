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

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

public class Translations {

    private static List<Translation> translations = new ArrayList<>();

    static {
        // firstly try to read from the controlsfx jar
        File file = new File(Translations.class.getProtectionDomain().getCodeSource().getLocation().getPath());
        if (file.getName().endsWith(".jar")) { //$NON-NLS-1$
            Path jarFile = file.toPath();
            try (FileSystem fs = FileSystems.newFileSystem(jarFile, null)) {
                fs.getRootDirectories().forEach(path -> loadFrom(path));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        // look in src directory
        if (translations.isEmpty()) {
            // try to read the files from the local filesystem (good for when ControlsFX
            // is being run from within a developers IDE)
            Path srcDir = new File("src/main/resources").toPath(); //$NON-NLS-1$
            loadFrom(srcDir);
        }
        
        // look in bin directory
        if (translations.isEmpty()) {
            Path binDir = new File("bin").toPath(); //$NON-NLS-1$
            loadFrom(binDir);
        }
        
        // look in bin directory an alternative way (good for when running 
        // controlsfx-samples)
        if (translations.isEmpty()) {
            if (file.getAbsolutePath().endsWith("controlsfx" + File.separator + "bin")) { //$NON-NLS-1$ //$NON-NLS-2$
                loadFrom(file.toPath());
            }
        }
        
        Collections.sort(translations);
    }
    
    private static void loadFrom(Path rootPath) {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(rootPath)) {
            for (Path path : stream) {
                String filename = path.getFileName().toString();

                if (! filename.startsWith("controlsfx") && ! filename.endsWith(".properties")) { //$NON-NLS-1$ //$NON-NLS-2$
                    continue;
                }

                if ("controlsfx.properties".equals(filename)) { //$NON-NLS-1$
                    translations.add(new Translation("en", path)); //$NON-NLS-1$
                } else if (filename.contains("_")) { //$NON-NLS-1$
                    String locale = filename.substring(11, filename.indexOf(".properties")); //$NON-NLS-1$
                    translations.add(new Translation(locale, path));
                } else {
                    throw new IllegalStateException("Unknown translation file '" + path + "'."); //$NON-NLS-1$ //$NON-NLS-2$
                }

            }
        } catch (IOException | DirectoryIteratorException x) {
            // no-op
        }
    }

    private Translations() {
        // no-op
    }
    
    public static Optional<Translation> getTranslation(String localeString) {
        for (Translation t : translations) {
            if (localeString.equals(t.getLocaleString())) {
                return Optional.of(t);
            }
        }
        return Optional.empty();
    }

    public static List<Translation> getAllTranslations() {
        return translations;
    }

    public static List<Locale> getAllTranslationLocales() {
        return translations.stream().map((Translation t) -> t.getLocale()).collect(Collectors.toList());
    }
}
