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
package impl.org.controlsfx.transifex

import groovyx.net.http.RESTClient
import groovyx.net.http.HttpResponseDecorator
import groovyx.net.http.HttpResponseException
import groovyx.net.http.ContentType

import java.net.UnknownHostException
    
RESTClient client
HttpResponseDecorator resp

String username = System.properties['transifex.username']
String password = System.properties['transifex.password']

client = new RESTClient()

client.uri = "https://www.transifex.com/api/2/"

// Use preemptive authentication
client.headers['Authorization'] = "Basic "+(username+":"+password).bytes.encodeBase64()
client.auth.basic(username, password)

try {
    resp = client.get(
        path : "project/controlsfx/resource/controlsfx-core/",
        query : [details : ""]
    )

    resp.data.available_languages.code.each {
        String file = "controlsfx_${it}.properties"
        
        print "Downloading $file..."
        
        new File("build/resources/main/$file").withWriter("iso-8859-1") { writer ->
            writer << client.get(
                path : "project/controlsfx/resource/controlsfx-core/translation/$it/",
                query : [file : ""],
                contentType : ContentType.BINARY
            ).data.getText("iso-8859-1")
        }
         
        println "Done."
    }
} catch (UnknownHostException ex) {
    System.err.println "Unable to download translation resources."
    System.err.println ex
    System.err.println "Are you connected to the Internet ?"
} catch (HttpResponseException ex) {
    System.err.println "Unable to download translation resources."
    System.err.println ex
    System.err.println "Incorrect Username/Password !!"
}

