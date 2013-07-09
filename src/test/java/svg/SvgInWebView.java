package svg;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

/**
 * @created 7/04/2013 
 */
public class SvgInWebView extends Application {

    public static void main(String[] args) {
        launch(args);
    }
    
    @Override public void start(Stage stage) throws Exception {
        final WebView view = new WebView();
        final WebEngine eng = view.getEngine();
        
        final double prefWidth = 175;
        final double prefHeight = 175;
        
        final String[] images = new String[] {
              "emblem-important.svg",
              "error.svg",
              "help-browser.svg"
        };
        
        String imageHTML = "";
        for (String svgImage : images) {
            final String src = getClass().getResource(svgImage).toExternalForm();
            imageHTML += "<img width=\"" + prefWidth + "\" height=\"" + prefHeight + "\" src=\"" + src + "\" />";
            imageHTML += "<br/>";
        }
        
        String content =
        "<html>" +
            "<body>" +
                imageHTML +
            "</body>" +
        "</head>";
                        
        eng.loadContent(content);
        
        final Scene scene = new Scene(view);
        stage.setWidth(200);
        stage.setScene(scene);
        stage.show();
    }

}
