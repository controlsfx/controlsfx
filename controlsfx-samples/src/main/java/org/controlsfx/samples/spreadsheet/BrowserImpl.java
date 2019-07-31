/**
 * Copyright (c) 2019 ControlsFX
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
package org.controlsfx.samples.spreadsheet;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.web.WebView;
import org.controlsfx.control.spreadsheet.BrowserInterface;
import org.controlsfx.control.spreadsheet.SpreadsheetCell;

/**
 *
 * Example of implementation of the BrowserInterface in order to provide WebView
 * inside of cells.
 */
public class BrowserImpl implements BrowserInterface<WebView> {

    private final LinkedList<WebView> browserList = new LinkedList<>();
    private final AtomicInteger browserCounter = new AtomicInteger();
    private final Map<WebView, String> loadedUrl = new HashMap<>();

    @Override
    public Node getBrowser(SpreadsheetCell cell) {
        //Limit the number of browser displayed.
        if (browserCounter.get() < 256) {
            if (browserList.isEmpty()) {
                browserCounter.incrementAndGet();
                WebView webView = new WebView();
                webView.getEngine().loadContent(cell.getItem().toString());
                loadedUrl.put(webView, cell.getItem().toString());
                return webView;
            } else {
                WebView webView = browserList.pop();
                webView.getEngine().loadContent(cell.getItem().toString());
                return webView;
            }
        } else {
            return new Label("Too many browser");
        }
    }

    @Override
    public void load(WebView browser, SpreadsheetCell cell) {
        if (!loadedUrl.containsKey(browser) || !loadedUrl.get(browser).equals(cell.getItem())) {
            browser.getEngine().loadContent(cell.getItem().toString());
            loadedUrl.put(browser, cell.getItem().toString());
        }
    }

    public void loadStyle(WebView browser, SpreadsheetCell cell, Font font, Paint textFill, Pos alignment, Background background) {
        //no-op
    }

    @Override
    public void setUnusedBrowser(WebView browser) {
        browserList.add(browser);
    }

    public Class getType() {
        return WebView.class;
    }
}
