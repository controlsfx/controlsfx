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
public class SVGLoader {
    
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
    public static void loadSVGImage(URL svgImage, final double prefWidth, final double prefHeight, final Callback<ImageView, Void> callback) {
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
    public static void loadSVGImage(URL svgImage, final WritableImage outputImage) {
        if (outputImage == null) {
            throw new NullPointerException("outputImage can not be null");
        }
        final double w = outputImage.getWidth();
        final double h = outputImage.getHeight();
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
