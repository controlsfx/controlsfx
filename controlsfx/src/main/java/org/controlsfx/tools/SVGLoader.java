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
package org.controlsfx.tools;

import java.net.URL;

import javafx.beans.value.ChangeListener;
import javafx.concurrent.Worker.State;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.SnapshotResult;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.util.Callback;

import com.sun.javafx.webkit.Accessor;
import com.sun.webkit.WebPage;

/**
 * Convenience class that will attempt to load a given URL as an .svg file.
 */
class SVGLoader {
    
    private SVGLoader() {
        // no-op
    }

    /**
     * This method will attempt to load the given svgImage URL into an ImageView
     * node that will be provided asynchronously via the provided 
     * {@link Callback}, and it will be sized to the given prefWidth / prefHeight.
     * 
     * <p>Note that it is valid to pass in -1 to prefWidth and / or prefHeight as
     * an indicator to the SVG loader. If both values are -1, the default width
     * of the SVG will be used. If one of the values is -1, then the SVG will
     * be sized to ensure that it remains proportional.
     * 
     * @param svgImage The image to load.
     * @param prefWidth The preferred width of the image when loaded, or -1 if 
     *      there is no preferred width.
     * @param prefHeight The preferred height of the image when loaded, or -1 if 
     *      there is no preferred height.
     * @param callback The {@link Callback} that will be called when the SVG 
     *      image is loaded, where the {@link ImageView} containing the rendered
     *      image will be available.
     */
    public static void loadSVGImage(final URL svgImage, 
                                    final double prefWidth, 
                                    final double prefHeight, 
                                    final Callback<ImageView, Void> callback) {
        loadSVGImage(svgImage, prefWidth, prefHeight, callback, null);
    }
    
    /**
     * This method will attempt to load the given svgImage URL into the provided
     * {@link WritableImage}, with the SVG scaled to fit the size of the
     * WritableImage.
     * 
     * @param svgImage The image to load.
     * @param outputImage The location to write the loaded image once it has 
     *      been rendered (it will not happen synchronously).
     * @throws NullPointerException The outputImage argument must be non-null.
     */
    public static void loadSVGImage(final URL svgImage, 
                                    final WritableImage outputImage) {
        if (outputImage == null) {
            throw new NullPointerException("outputImage can not be null"); //$NON-NLS-1$
        }
        final double w = outputImage.getWidth();
        final double h = outputImage.getHeight();
        loadSVGImage(svgImage, w, h, null, outputImage);
    }
    
    public static void loadSVGImage(final URL svgImage, 
                                    final double prefWidth, 
                                    final double prefHeight, 
                                    final Callback<ImageView, Void> callback, 
                                    final WritableImage outputImage) {
        final WebView view = new WebView();
        final WebEngine eng = view.getEngine();
        
        // using non-public API to ensure background transparency
        final WebPage webPage = Accessor.getPageFor(eng);
        webPage.setBackgroundColor(webPage.getMainFrame(), 0xffffff00);
        webPage.setOpaque(webPage.getMainFrame(), false); 
        // end of non-public API

        // temporary scene / stage
        final Scene scene = new Scene(view);
        final Stage stage = new Stage();
        stage.setScene(scene);
        stage.setWidth(0);
        stage.setHeight(0);
        stage.setOpacity(0);
        stage.show();
        
//        String svgString = readFile(svgImage);

        String content =
        "<html>" + //$NON-NLS-1$
            "<body style=\"margin-top: 0px; margin-bottom: 30px; margin-left: 0px; margin-right: 0px; padding: 0;\">" + //$NON-NLS-1$
//                "<div style=\"width: " + prefWidth + "; height: " + prefHeight + ";\">" +
                    "<img id=\"svgImage\" style=\"display: block;float: top;\" width=\"" + prefWidth + "\" height=\"" + prefHeight + "\" src=\"" + svgImage.toExternalForm() + "\" />" + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
//                    svgString +
//                "</div>" +
            "</body>" + //$NON-NLS-1$
        "</head>"; //$NON-NLS-1$
                
        
        
        eng.loadContent(content);
        
        eng.getLoadWorker().stateProperty().addListener(new ChangeListener<State>() {
            @Override public void changed(javafx.beans.value.ObservableValue<? extends State> o, State oldValue, State newValue) {
                if (newValue == State.SUCCEEDED) {
                    
//                    HTMLImageElement svgImageElement = (HTMLImageElement) getSvgDom(eng);
//                    System.out.println(svgImageElement.getAttributes());
                    
                    final double svgWidth = prefWidth >= 0 ? prefWidth : getSvgWidth(eng);
                    final double svgHeight = prefHeight >= 0 ? prefWidth : getSvgHeight(eng);
                    
                    SnapshotParameters params = new SnapshotParameters();
                    params.setFill(Color.TRANSPARENT);
                    params.setViewport(new Rectangle2D(0, 0, svgWidth, svgHeight));
                    
                    view.snapshot(new Callback<SnapshotResult, Void>() {
                        @Override public Void call(SnapshotResult param) {
                            WritableImage snapshot = param.getImage();
                            ImageView image = new ImageView(snapshot);
                            
                            if (callback != null) {
                                callback.call(image);
                            }
                            
                            stage.hide();
                            return null;
                        }
                    }, params, outputImage);
                }
            }
        });
    }
    
//    private static String readFile(URL url) {
//        try {
//            FileInputStream stream = new FileInputStream(new File(url.toURI()));
//            try {
//                FileChannel fc = stream.getChannel();
//                MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
//                return Charset.defaultCharset().decode(bb).toString();
//            }
//            finally {
//                stream.close();
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return null;
//    }
    
    private static double getSvgWidth(WebEngine webEngine) {
        Object result = getSvgDomProperty(webEngine, "offsetWidth"); //$NON-NLS-1$
        if (result instanceof Integer) {
            return (Integer) result;
        }
        return -1;
    }
    
    private static double getSvgHeight(WebEngine webEngine) {
        Object result = getSvgDomProperty(webEngine, "offsetHeight"); //$NON-NLS-1$
        if (result instanceof Integer) {
            return (Integer) result;
        }
        return -1;
    }
    
    private static Object getSvgDomProperty(final WebEngine webEngine, final String property) {
        return webEngine.executeScript("document.getElementById('svgImage')." + property); //$NON-NLS-1$
    }

//    private static HTMLImageElement getSvgDom(WebEngine webEngine) {
//        return (HTMLImageElement) webEngine.executeScript("document.getElementById('svgImage')");
//    }
//    
//    private static void printDocument(Document doc, OutputStream out) throws IOException, TransformerException {
//        TransformerFactory tf = TransformerFactory.newInstance();
//        Transformer transformer = tf.newTransformer();
//        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
//        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
//        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
//        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
//        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
//
//        transformer.transform(new DOMSource(doc), 
//             new StreamResult(new OutputStreamWriter(out, "UTF-8")));
//    }
}
