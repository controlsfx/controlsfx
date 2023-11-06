/**
 * Copyright (c) 2014, 2023, ControlsFX
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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Transifex {
    
    private static final String CHARSET             = "ISO-8859-1";
    private static final String FILE_NAME           = "controlsfx_%1s.utf8";
    private static final String NEW_LINE            = System.getProperty("line.separator");

    private static final String BASE_URI            = "https://rest.api.transifex.com/";
    private static final String CREATE_TRANSLATION  = BASE_URI + "resource_translations_async_downloads";
    private static final String GET_TRANSLATION     = BASE_URI + "resource_translations_async_downloads/%s";
    private static final String TRANSLATION_STATS   = BASE_URI + "resource_language_stats?filter[project]=o:controlsfx:p:controlsfx";
    private static final String TRANSIFEX_API       = System.getProperty("transifex.api"); //$NON-NLS-1$

    public static void main(String[] args) {
        new Transifex().doTransifexCheck();
    }
        
    @SuppressWarnings("unchecked")
    private void doTransifexCheck() {
        System.out.println("=== Starting Transifex Check ==="); //$NON-NLS-1$
        
        if (TRANSIFEX_API == null || TRANSIFEX_API.isEmpty()) {
            System.out.println("transifex.api system properties must be specified"); //$NON-NLS-1$
            return;
        }

        Map<String,Object> translationStats = JSON.parse(transifexRequest(TRANSLATION_STATS));
        List<Map<String, Object>> availableLanguages = (List<Map<String, Object>>) translationStats.get("data");

        Map<String, String> resourceIds = availableLanguages.parallelStream()
                .filter(this::filterOutIncompleteTranslations)
                .map(this::getLanguageCode)
                .collect(Collectors.toMap(languageCode -> languageCode, this::createDownloadTranslationFile));

        resourceIds.forEach(this::downloadTranslation);
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
            
            // pass in API details
            connection.setRequestProperty("Authorization", "Bearer " + TRANSIFEX_API);
            connection.setRequestProperty("Accept-Charset", CHARSET);
            
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
    
    private boolean filterOutIncompleteTranslations(Map<String, Object> languageMap) {
        // filter out any translation that does not have 100% completion and reviewed state.
        // Returns a Map, for example:
        // {
        //      "id": "o:controlsfx:p:controlsfx:r:controlsfx-core:l:ar",
        //      "type": "resource_language_stats",
        //      "attributes": {
        //        "untranslated_words": 0,
        //        "translated_words": 161,
        //        "reviewed_words": 0,
        //        "proofread_words": 0,
        //        "total_words": 161,
        //        "untranslated_strings": 0,
        //        "translated_strings": 93,
        //        "reviewed_strings": 0,
        //        "proofread_strings": 0,
        //        "total_strings": 93,
        //        "last_translation_update": "2020-08-20T17:16:40Z",
        //        "last_review_update": "2018-12-17T18:13:15Z",
        //        "last_proofread_update": null,
        //        "last_update": "2020-08-20T17:16:40Z"
        //      },
        //      "relationships": {
        //        "resource": {
        //          "links": {
        //            "related": "https://rest.api.transifex.com/resources/o:controlsfx:p:controlsfx:r:controlsfx-core"
        //          },
        //          "data": {
        //            "type": "resources",
        //            "id": "o:controlsfx:p:controlsfx:r:controlsfx-core"
        //          }
        //        },
        //        "language": {
        //          "links": {
        //            "related": "https://rest.api.transifex.com/languages/l:ar"
        //          },
        //          "data": {
        //            "type": "languages",
        //            "id": "l:ar"
        //          }
        //        }
        //      },
        //      "links": {
        //        "self": "https://rest.api.transifex.com/resource_language_stats/o:controlsfx:p:controlsfx:r:controlsfx-core:l:ar"
        //      }
        //    }
        Map<String, Object> attributes = (Map<String, Object>) languageMap.get("attributes");
        boolean isAccepted = (int) attributes.get("untranslated_words") == 0;
        System.out.println("\tReviewing translation '" +
                getLanguageCode(languageMap) + "'" +
                "\t-> TRANSLATION" + (isAccepted ? " ACCEPTED" : " REJECTED"));
        return isAccepted;
    }
    
    private void downloadTranslation(String languageCode, String resourceId) {
        if (resourceId.isEmpty()) return;
        // Now we download the translations of the completed languages
        System.out.println("\tDownloading translation file for... " + languageCode);
        Function<InputStream, Void> consumer = inputStream -> {
            final String outputFile = "build/resources/main/" + String.format(FILE_NAME, languageCode);
            try (BufferedWriter writer = new BufferedWriter(new PrintWriter(outputFile, CHARSET))) {
                writer.write(parseInputStream(inputStream));
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        };
        performTransifexTask(consumer, String.format(GET_TRANSLATION, resourceId));
    }

    private String getLanguageCode(Map<String, Object> languageMap) {
        String id = (String) languageMap.get("id");
        return id.substring(id.lastIndexOf(":") + 1);
    }

    private String createDownloadTranslationFile(String languageCode) {
        String requestData = String.format("{\"data\":{\"attributes\":{\"callback_url\":null,\"content_encoding\":\"text\",\"file_type\":\"default\",\"mode\":\"default\",\"pseudo\":false},\"relationships\":{\"language\":{\"data\":{\"type\":\"languages\",\"id\":\"l:%s\"}},\"resource\":{\"data\":{\"type\":\"resources\",\"id\":\"o:controlsfx:p:controlsfx:r:controlsfx-core\"}}},\"type\":\"resource_translations_async_downloads\"}}", languageCode);
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(CREATE_TRANSLATION))
                    .setHeader("Authorization", "Bearer " + TRANSIFEX_API)
                    .setHeader("content-type", "application/vnd.api+json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestData))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 202) {
                Map<String, Object> jsonResponse = JSON.parse(response.body());
                Map<String, Object> data = (Map<String, Object>) jsonResponse.get("data");
                // return resource id
                return (String) data.get("id");
            }
            return "";
        } catch (IOException | InterruptedException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
