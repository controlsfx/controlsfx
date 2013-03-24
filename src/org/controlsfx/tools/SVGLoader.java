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

public class SVGLoader {
    
    private SVGLoader() {
        // no-op
    }

    public static void loadSVGImage(URL svgImage, final double prefWidth, final double prefHeight, final Callback<ImageView, Void> callback) {
        loadSVGImage(svgImage, prefWidth, prefHeight, callback, null);
    }
    
    public static void loadSVGImage(URL svgImage, final WritableImage outputImage) {
        final double w = outputImage == null ? -1 : outputImage.getWidth();
        final double h = outputImage == null ? -1 : outputImage.getHeight();
        loadSVGImage(svgImage, w, h, null, outputImage);
    }
    
    public static void loadSVGImage(URL svgImage, final double prefWidth, final double prefHeight, final Callback<ImageView, Void> callback, final WritableImage outputImage) {
        final WebView view = new WebView();
        final WebEngine eng = view.getEngine();
        
        // using non-public API to ensure background transparency
        WebPage webPage = Accessor.getPageFor(eng);
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

        String content = 
//                "<body bgcolor=\"yellow\">" +
//                "<body style=\"background: rgba(0, 0, 0, 1.0);\">" +
                "<body style=\"margin-top: 0px; margin-bottom: 0px; margin-left: 0px; margin-right: 0px; padding: 0;\">" +
                "<img width=\"" + prefWidth + "\" height=\"" + prefHeight + "\" src=\"" + svgImage.toExternalForm() + "\" />";
        eng.loadContent(content);

        eng.getLoadWorker().stateProperty().addListener(new ChangeListener<State>() {
            @Override public void changed(javafx.beans.value.ObservableValue<? extends State> o, State oldValue, State newValue) {
                if (newValue == State.SUCCEEDED) {
                    SnapshotParameters params = new SnapshotParameters();
                    params.setFill(Color.TRANSPARENT);
                    params.setViewport(new Rectangle2D(0, 0, prefWidth, prefWidth));
                    
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
}
