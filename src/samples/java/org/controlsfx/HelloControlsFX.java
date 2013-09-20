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
package org.controlsfx;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.util.Callback;

import org.controlsfx.control.HyperlinkLabel;

public class HelloControlsFX extends Application {

    private static final String SAMPLES_ROOT_PACKAGE = "org.controlsfx.samples";

    private final Map<String, TreeItem<Sample>> packageTreeItemMap = new HashMap<>();

    private GridPane grid;

    private TabPane tabPane;
    private Tab welcomeTab;
    private Tab sampleTab;
    private Tab webViewTab;

    private WebView webview;


    public static void main(String[] args) {
        launch(args);
    }

    @Override public void start(final Stage primaryStage) throws Exception {
        setUserAgentStylesheet(STYLESHEET_MODENA);

        primaryStage.getIcons().add(new Image("/org/controlsfx/samples/controlsfx-logo.png"));

        final TreeItem<Sample> root = new TreeItem<Sample>(new EmptySample("ControlsFX"));
        root.setExpanded(true);

        Class<?>[] sampleClasses = getClasses(SAMPLES_ROOT_PACKAGE);
        for (Class<?> sampleClass : sampleClasses) {
            if (! Sample.class.isAssignableFrom(sampleClass)) continue;

            final Sample sample = (Sample)sampleClass.newInstance();
            if (! sample.includeInSamples()) continue;

            final String packageName = sampleClass.getPackage().getName();
            String displayName = packageName.substring(packageName.lastIndexOf(".") + 1);
            displayName = displayName.substring(0, 1).toUpperCase() + displayName.substring(1);
            displayName = displayName.replace("_", " ");

            if (SAMPLES_ROOT_PACKAGE.equals(packageName)) {
                root.getChildren().add(new TreeItem<Sample>(sample));
                continue;
            }

            TreeItem<Sample> packageTreeItem;
            if (packageTreeItemMap.containsKey(displayName)) {
                packageTreeItem = packageTreeItemMap.get(displayName);
            } else {
                packageTreeItem = new TreeItem<Sample>(new EmptySample(displayName));
                packageTreeItemMap.put(displayName, packageTreeItem);
            }

            // now that we have the package TreeItem, we create a child TreeItem
            // for the actual sample
            TreeItem<Sample> sampleTreeItem = new TreeItem<Sample>(sample);
            packageTreeItem.getChildren().add(sampleTreeItem);
            packageTreeItem.setExpanded(true);
        }

        root.getChildren().addAll(packageTreeItemMap.values());

        Collections.sort(root.getChildren(), new Comparator<TreeItem<Sample>>() {
            @Override public int compare(TreeItem<Sample> o1, TreeItem<Sample> o2) {
                return o1.getValue().getSampleName().compareTo(o2.getValue().getSampleName());
            }
        });

        // simple layout: ListView on left, sample area on right
        grid = new GridPane();
        grid.setPadding(new Insets(5, 10, 10, 10));
        grid.setHgap(10);
        grid.setVgap(10);

        // --- left hand side
        TreeView<Sample> samplesTreeView = new TreeView<>(root);
        samplesTreeView.setMinWidth(200);
        samplesTreeView.setMaxWidth(200);
        samplesTreeView.setCellFactory(new Callback<TreeView<Sample>, TreeCell<Sample>>() {
            @Override public TreeCell<Sample> call(TreeView<Sample> param) {
                return new TreeCell<Sample>() {
                    @Override protected void updateItem(Sample item, boolean empty) {
                        super.updateItem(item, empty);

                        if (empty) {
                            setText("");
                        } else {
                            setText(item.getSampleName());
                        }
                    }
                };
            }
        });
        samplesTreeView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<TreeItem<Sample>>() {
            @Override public void changed(ObservableValue<? extends TreeItem<Sample>> observable, TreeItem<Sample> oldValue, TreeItem<Sample> newSample) {
                if (newSample == root) {
                    changeToWelcomeTab();
                    return;
                } else if (newSample.getValue() instanceof EmptySample) {
                    return;
                }
                changeSample(newSample.getValue(), primaryStage);
            }
        });
        GridPane.setVgrow(samplesTreeView, Priority.ALWAYS);
        GridPane.setMargin(samplesTreeView, new Insets(5, 0, 0, 0));
        grid.add(samplesTreeView, 0, 0);

        // right hand side
        tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);
        tabPane.getStyleClass().add(TabPane.STYLE_CLASS_FLOATING);
        GridPane.setHgrow(tabPane, Priority.ALWAYS);
        GridPane.setVgrow(tabPane, Priority.ALWAYS);
        grid.add(tabPane, 1, 0, 1, 1);

        sampleTab = new Tab("Sample");
        webViewTab = new Tab("JavaDoc");
        webview = new WebView();
        webViewTab.setContent(webview);

        // by default we'll have a welcome message in the right-hand side
        changeToWelcomeTab();

        // put it all together
        Scene scene = new Scene(grid);
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(1000);
        primaryStage.setMinHeight(600);
        primaryStage.setWidth(1000);
        primaryStage.setHeight(600);
        primaryStage.setTitle("ControlsFX!");
        primaryStage.show();

        samplesTreeView.requestFocus();
    }

    private void changeSample(Sample newSample, final Stage stage) {
        if (newSample == null) {
            return;
        }

        if (tabPane.getTabs().contains(welcomeTab)) {
            tabPane.getTabs().setAll(sampleTab, webViewTab);
        }

        // update the sample tab
        sampleTab.setContent(newSample.getPanel(stage));

        // update the javadoc tab
        webview.getEngine().load(newSample.getJavaDocURL());
    }

    private void changeToWelcomeTab() {
        // line 1
        Label welcomeLabel1 = new Label("Welcome to ControlsFX!");
        welcomeLabel1.setStyle("-fx-font-size: 2em; -fx-padding: 0 0 0 5;");

        // line 2
        HyperlinkLabel welcomeLabel2 = new HyperlinkLabel(
                "Explore the available UI controls by clicking on the options to the left.\n\n" +
                        "There have been many contributors to this project, including:\n" +
                        "   Jonathan Giles\n" +
                        "   Eugene Ryzhikov\n" +
                        "   Hendrik Ebbers\n" +
                        "   Danno Ferrin\n" +
                        "   Paru Somashekar\n\n" +
                        "If you ever meet any of these wonderful contributors, tell them how great they are! :-)\n\n" +
                "To keep up to date with the ControlsFX project, visit the website at [http://www.controlsfx.org]");
        welcomeLabel2.setStyle("-fx-font-size: 1.25em; -fx-padding: 0 0 0 5;");
        welcomeLabel2.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent event) {
                webViewTab.setText("ControlsFX Website");

                webview.getEngine().load("http://www.controlsfx.org");
                tabPane.getTabs().add(webViewTab);
                tabPane.getSelectionModel().select(webViewTab);
            }
        });

        VBox initialVBox = new VBox(5, welcomeLabel1, welcomeLabel2);

        welcomeTab = new Tab("Welcome to ControlsFX!");
        welcomeTab.setContent(initialVBox);

        tabPane.getTabs().setAll(welcomeTab);
    }





    private static class EmptySample implements Sample {
        private final String name;

        public EmptySample(String name) {
            this.name = name;
        }

        @Override public String getSampleName() {
            return name;
        }

        @Override public Node getPanel(Stage stage) {
            return null;
        }

        @Override public String getJavaDocURL() {
            return null;
        }

        @Override public boolean includeInSamples() {
            return true;
        }
    }




    // --- Following code taken from http://dzone.com/snippets/get-all-classes-within-package
    /**
     * Scans all classes accessible from the context class loader which belong to the given package and subpackages.
     *
     * @param packageName The base package
     * @return The classes
     * @throws ClassNotFoundException
     * @throws IOException
     */
    private static Class<?>[] getClasses(String packageName) throws ClassNotFoundException, IOException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        String path = packageName.replace('.', '/');
        
        Enumeration<URL> resources = classLoader.getResources(path);
        List<File> dirs = new ArrayList<File>();
        while (resources.hasMoreElements()) {
            URL next = resources.nextElement();
            
            // Only "file" and "jar" URLs are recognized, other schemes will be ignored.
            String protocol = next.getProtocol().toLowerCase();
            if ("file".equals(protocol)) {
                dirs.add(new File(next.getFile()));
            } else if ("jar".equals(protocol)) {
                String fileName = new URL(next.getFile()).getFile();
                
                // JAR URL specs must contain the string "!/" which separates the name
                // of the JAR file from the path of the resource contained in it, even
                // if the path is empty.
                int sep = fileName.indexOf("!/");
                if (sep > 0) {
                    dirs.add(new File(fileName.substring(0, sep)));
                }
                // otherwise the URL was invalid
            }
        }
        ArrayList<Class<?>> classes = new ArrayList<Class<?>>();
        for (File directory : dirs) {
            String fullPath = directory.getAbsolutePath();
            
            if (fullPath.toLowerCase().endsWith(".jar")) {
                // scan the jar
                classes.addAll(findClassesInJar(new File(fullPath), packageName));
            } else {
                // scan the classpath
                classes.addAll(findClasses(directory, packageName));
            }
        }
        return classes.toArray(new Class[classes.size()]);
    }

    /**
     * Recursive method used to find all classes in a given directory and subdirs.
     *
     * @param directory   The base directory
     * @param packageName The package name for classes found inside the base directory
     * @return The classes
     * @throws ClassNotFoundException
     */
    private static List<Class<?>> findClasses(File directory, String packageName) throws ClassNotFoundException {
        List<Class<?>> classes = new ArrayList<Class<?>>();
        if (!directory.exists()) {
            System.out.println("Directory does not exist: " + directory.getAbsolutePath());
            return classes;
        }
        
        File[] files = directory.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                assert !file.getName().contains(".");
                classes.addAll(findClasses(file, packageName + "." + file.getName()));
            } else if (file.getName().endsWith(".class")) {
                String className = packageName + '.' + file.getName().substring(0, file.getName().length() - 6);
                classes.add(Class.forName(className));
            }
        }
        return classes;
    }


    private static List<Class<?>> findClassesInJar(File jarFile, String packageName) throws IOException, ClassNotFoundException {
        List<Class<?>> classes = new ArrayList<Class<?>>();
        if (!jarFile.exists()) {
            System.out.println("Jar file does not exist here: " + jarFile.getAbsolutePath());
            return classes;
        }
        
        try (ZipInputStream zip = new ZipInputStream(new FileInputStream(jarFile))) {
            for (ZipEntry entry = zip.getNextEntry(); entry != null; entry = zip.getNextEntry()) {
                if (entry.getName().endsWith(".class") && !entry.isDirectory()) {
                    
                    StringBuilder className = new StringBuilder();
                    for (String part : entry.getName().split("/")) {
                        if(className.length() != 0) {
                            className.append(".");
                        }
                        
                        className.append(part);
                        
                        if(part.endsWith(".class")) {
                            className.setLength(className.length()-".class".length());
                        }
                    }
                    
                    if (className.toString().contains(packageName)) {
                        classes.add(Class.forName(className.toString()));
                    }
                }
            }
        }
        return classes;
    }
}
