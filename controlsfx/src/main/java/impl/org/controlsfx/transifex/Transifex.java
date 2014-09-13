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
package impl.org.controlsfx.transifex;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;
import java.util.List;
import java.util.Map;

public class Transifex {
    
    private static final String CHARSET = "ISO-8859-1";
	private static final boolean DEBUG = true;
    private static final boolean FILTER_INCOMPLETE_TRANSLATIONS = false;
    private static final String FILE_NAME = "controlsfx_%1s.properties";
    private static final String NEW_LINE = System.getProperty("line.separator");

    private static final String BASE_URI = "https://www.transifex.com/api/2/";
//    private static final String PROJECT_PATH = "project/controlsfx/resource/controlsfx-core"; // list project details
    private static final String LIST_TRANSLATIONS  = BASE_URI + "project/controlsfx/languages/"; // list all translations
    private static final String GET_TRANSLATION = BASE_URI + "project/controlsfx/resource/controlsfx-core/translation/%1s/"; // gets a translation for one language
    private static final String TRANSLATION_STATS = BASE_URI + "project/controlsfx/resource/controlsfx-core/stats/%1s/"; // gets a translation for one language
    
    private static final String USERNAME = System.getProperty("transifex.username");
    private static final String PASSWORD = System.getProperty("transifex.password");

    public static void main(String[] args) throws FileNotFoundException, UnsupportedEncodingException {
        new Transifex().doTransifexCheck();
    }
        
    private void doTransifexCheck() {
        System.out.println("=== Starting Transifex Check ===");
        
        if (USERNAME == null || PASSWORD == null) {
            System.out.println("transifex.username and transifex.password system properties must be specified");
        }
        
        // get a list of all translations
        // Once parsed, returns a List of Map, e.g.
        // [
        //  {language_code=ar, translators=[], coordinators=[Mazenization], reviewers=[]}, 
        //  {language_code=ca, translators=[], coordinators=[ManelSanchezRuiz], reviewers=[]}, 
        //  {language_code=zh_CN, translators=[], coordinators=[happyfeet], reviewers=[]}, 
        //  {language_code=nl_BE, translators=[], coordinators=[neo4010], reviewers=[]}
        // ]
        // we just care about the language code, so we extract these out into a list
        String response = transifexRequest(LIST_TRANSLATIONS);
        List<Map<String,String>> translations = JSON.parse(response);
        
        // main loop
        translations.stream()
                .map(map -> map.get("language_code"))
                .filter(this::filterOutIncompleteTranslations)
                .forEach(this::downloadTranslation);
        
        System.out.println("Transifex Check Complete");
    }
    
    private String transifexRequest(String request, Object... args) {
        request = String.format(request, args);
        
        URL url;
        HttpURLConnection connection = null;  
        try {
            url = new URL(request);
            connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("GET");
            connection.setUseCaches(false);
            connection.setDoInput(true);
            
            // pass in username / password
            String encoded = Base64.getEncoder().encodeToString((USERNAME+":"+PASSWORD).getBytes());
            connection.setRequestProperty("Authorization", "Basic "+encoded);
            connection.setRequestProperty("Accept-Charset", CHARSET); 
            
            InputStream is = connection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            String line;
            StringBuilder response = new StringBuilder(); 
            while((line = rd.readLine()) != null) {
                response.append(line);
                response.append(NEW_LINE);
            }
            rd.close();
            
            return response.toString();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(connection != null) {
                connection.disconnect(); 
            }
        }
        
        return "";
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
        String completed = map.getOrDefault("completed", "0%");
        String reviewed = map.getOrDefault("reviewed_percentage", "0%");
        boolean isAccepted = completed.equals("100%") && reviewed.equals("100%");
        
        if (DEBUG) {
            System.out.println("Reviewing translation '" + languageCode + "'" + 
                    "\tcompletion: " + completed + 
                    ",\treviewed: " + reviewed + 
                    "\t-> TRANSLATION" + (isAccepted ? " ACCEPTED" : " REJECTED"));
        }
        
        return isAccepted || !FILTER_INCOMPLETE_TRANSLATIONS;
    }
    
    private void downloadTranslation(String languageCode) {
        // Now we download the translations of the completed languages
        System.out.println("\tDownloading translation file...");
        
        try {
            Map<String,String> map = JSON.parse(transifexRequest(GET_TRANSLATION, languageCode));
            
            String content = new String(map.get("content").getBytes(), CHARSET);
            String outputFile = "build/resources/main/" + String.format(FILE_NAME, languageCode);
            PrintWriter pw = new PrintWriter(outputFile, CHARSET);
            
            pw.write(content.replace("\\n", "\n"));
            
//                String[] lines = content.split("\\\\n");
//                System.out.println("line count: " + lines.length);
//                for (String line : lines) {
//                    pw.println(line);
//                }
            pw.close();
        } catch (UnsupportedEncodingException | FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
