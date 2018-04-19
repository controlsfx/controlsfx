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
package impl.build.transifex;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class Transifex {
    
    private static final String CHARSET             = "ISO-8859-1"; //$NON-NLS-1$
    private static final String FILE_NAME           = "controlsfx_%1s.utf8"; //$NON-NLS-1$
    private static final String NEW_LINE            = System.getProperty("line.separator"); //$NON-NLS-1$

    private static final String BASE_URI            = "https://www.transifex.com/api/2/"; //$NON-NLS-1$
    private static final String PROJECT_PATH        = BASE_URI + "project/controlsfx/resource/controlsfx-core"; // list simple project details //$NON-NLS-1$
    private static final String PROJECT_DETAILS     = BASE_URI + "project/controlsfx/resource/controlsfx-core?details"; // list all project details //$NON-NLS-1$
    private static final String LIST_TRANSLATIONS   = BASE_URI + "project/controlsfx/languages/"; // list all translations //$NON-NLS-1$
    private static final String GET_TRANSLATION     = BASE_URI + "project/controlsfx/resource/controlsfx-core/translation/%1s?file"; // gets a translation for one language //$NON-NLS-1$
    private static final String TRANSLATION_STATS   = BASE_URI + "project/controlsfx/resource/controlsfx-core/stats/%1s/"; // gets a translation for one language //$NON-NLS-1$

    private static final String USERNAME            = System.getProperty("$transifex_username"); //$NON-NLS-1$
    private static final String PASSWORD            = System.getProperty("$transifex_password"); //$NON-NLS-1$
    private static final boolean FILTER_INCOMPLETE_TRANSLATIONS = Boolean.parseBoolean(System.getProperty("transifex.filterIncompleteTranslations", "false"));

    public static void main(String[] args) throws FileNotFoundException, UnsupportedEncodingException {
        new Transifex().doTransifexCheck();
    }
        
    @SuppressWarnings("unchecked")
    private void doTransifexCheck() {
        System.out.println("=== Starting Transifex Check ==="); //$NON-NLS-1$
        
        if (USERNAME == null || PASSWORD == null || USERNAME.isEmpty() || PASSWORD.isEmpty()) {
            System.out.println("  transifex.username and transifex.password system properties must be specified"); //$NON-NLS-1$
            return;
        }
        
        System.out.println("  Filtering out incomplete translations: " + FILTER_INCOMPLETE_TRANSLATIONS);
        
        Map<String,Object> projectDetails = JSON.parse(transifexRequest(PROJECT_DETAILS));
        List<Map<String, String>> availableLanguages = (List<Map<String, String>>) projectDetails.get("available_languages");
        
        // main loop
        availableLanguages.parallelStream()
                .map(map -> map.get("code")) //$NON-NLS-1$
                .filter(this::filterOutIncompleteTranslations)
                .forEach(this::downloadTranslation);
        
        System.out.println("Transifex Check Complete"); //$NON-NLS-1$
    }
    
    private String transifexRequest(String request, Object... args) {
        return performTransifexTask(this::parseInputStream, request, args);
    }

    private String parseInputStream(InputStream inputStream) {
        StringBuilder response = new StringBuilder();
        try(BufferedReader rd = new BufferedReader(new InputStreamReader(inputStream, CHARSET)) ) {
            String line;
            while((line = rd.readLine()) != null) {
                response.append(line);
                response.append(NEW_LINE);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return response.toString();
    }
    
    private static <T> T performTransifexTask(Function<InputStream, T> consumer, String request, Object... args) {
        request = String.format(request, args);
        
        URL url;
        HttpURLConnection connection = null;  
        try {
            url = new URL(request);
            connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("GET"); //$NON-NLS-1$
            connection.setUseCaches(false);
            connection.setDoInput(true);
            
            // pass in username / password
            String encoded = Base64.getEncoder().encodeToString((USERNAME+":"+PASSWORD).getBytes()); //$NON-NLS-1$
            connection.setRequestProperty("Authorization", "Basic "+encoded); //$NON-NLS-1$ //$NON-NLS-2$
            connection.setRequestProperty("Accept-Charset", CHARSET);  //$NON-NLS-1$
            
            return consumer.apply(connection.getInputStream());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        
        return null;
    }
    
    private boolean filterOutIncompleteTranslations(String languageCode) {
        // filter out any translation that does not have 100% completion and reviewed state.
        // Returns a Map, for example:
        // { 
        //     untranslated_entities=8, 
        //     last_commiter=eryzhikov, 
        //     translated_entities=34, 
        //     untranslated_words=16, 
        //     translated_words=57, 
        //     last_update=2014-09-12 08:44:33, 
        //     reviewed_percentage=69%, 
        //     reviewed=29, 
        //     completed=80%
        // }
        Map<String, String> map = JSON.parse(transifexRequest(TRANSLATION_STATS, languageCode));
        String completed = map.getOrDefault("completed", "0%"); //$NON-NLS-1$ //$NON-NLS-2$
        String reviewed = map.getOrDefault("reviewed_percentage", "0%"); //$NON-NLS-1$ //$NON-NLS-2$
        boolean isAccepted = completed.equals("100%") && reviewed.equals("100%"); //$NON-NLS-1$ //$NON-NLS-2$
        
        System.out.println("  Reviewing translation '" + languageCode + "'" +  //$NON-NLS-1$ //$NON-NLS-2$
                "\tcompletion: " + completed +  //$NON-NLS-1$
                ",\treviewed: " + reviewed +  //$NON-NLS-1$
                "\t-> TRANSLATION" + (isAccepted ? " ACCEPTED" : " REJECTED")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        
        return isAccepted || !FILTER_INCOMPLETE_TRANSLATIONS;
    }
    
    private void downloadTranslation(String languageCode) {
        // Now we download the translations of the completed languages
        System.out.println("\tDownloading translation file..."); //$NON-NLS-1$

        Function<InputStream, Void> consumer = inputStream -> {
            final String outputFile = "build/resources/main/" + String.format(FILE_NAME, languageCode); //$NON-NLS-1$
            try (BufferedWriter writer = new BufferedWriter(new PrintWriter(outputFile, CHARSET))) {
                writer.write(parseInputStream(inputStream));
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        };
        performTransifexTask(consumer, GET_TRANSLATION, languageCode);
    }
}
