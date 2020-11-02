/**
 * Copyright (c) 2013, 2020, ControlsFX
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
package fxsampler;

import fxsampler.model.EmptySample;
import fxsampler.model.Project;
import fxsampler.model.SampleTree.TreeNode;
import fxsampler.model.WelcomePage;
import fxsampler.util.SampleScanner;
import javafx.application.Application;
import javafx.concurrent.Worker;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.function.Function;

public final class FXSampler extends Application {
    
    private static final String TAB_LOAD_CACHE = "TAB_LOAD_CACHE";
    
    private Map<String, Project> projectsMap;

    private Stage stage;
    private GridPane grid;
    
    private Sample selectedSample;
    
    private TreeView<Sample> samplesTreeView;
    private TreeItem<Sample> root;

    private TabPane tabPane;
    private Tab welcomeTab;
    private Tab sampleTab;
    private Tab javaDocTab;
    private Tab sourceTab;
    private Tab cssTab;

    private ProgressIndicator progressIndicator;
    private StackPane progressIndicatorPane;
    
    private WebView javaDocWebView;
    private WebView sourceWebView;
    private WebView cssWebView;
    private Project selectedProject;

    public static void main(String[] args) {
        launch(args);
    }

    @Override 
    public void start(final Stage primaryStage) {

        this.stage = primaryStage;
        
        ServiceLoader<FXSamplerConfiguration> configurationServiceLoader = ServiceLoader.load(FXSamplerConfiguration.class);

        projectsMap = new SampleScanner().discoverSamples();
        buildSampleTree(null);

        // simple layout: TreeView on left, sample area on right
        grid = new GridPane();
        grid.setPadding(new Insets(5, 10, 10, 10));
        grid.setHgap(10);
        grid.setVgap(10);

        // --- left hand side
        // search box
        final TextField searchBox = new TextField();
        searchBox.setPromptText("Search");
        searchBox.getStyleClass().add("search-box");
        searchBox.textProperty().addListener(o -> buildSampleTree(searchBox.getText()));
        GridPane.setMargin(searchBox, new Insets(5, 0, 0, 0));
        grid.add(searchBox, 0, 0);
        
        // treeview
        samplesTreeView = new TreeView<>(root);
        samplesTreeView.setShowRoot(false);
        samplesTreeView.getStyleClass().add("samples-tree");
        samplesTreeView.setMinWidth(200);
        samplesTreeView.setMaxWidth(200);
        samplesTreeView.setCellFactory(new Callback<>() {
            @Override
            public TreeCell<Sample> call(TreeView<Sample> param) {
                return new TreeCell<>() {
                    @Override
                    protected void updateItem(Sample item, boolean empty) {
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
        samplesTreeView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newSample) -> {
            if (newSample == null) {
                return;
            } else if (newSample.getValue() instanceof EmptySample) {
                Sample selectedSample = newSample.getValue();
                selectedProject = projectsMap.get(selectedSample.getSampleName());
                if(selectedProject != null) {
                    changeToWelcomeTab(selectedProject.getWelcomePage());
                }
                return;
            }
            if ( selectedSample != null ) {
                selectedSample.dispose();
            }
            selectedSample = newSample.getValue();
            changeSample();
        });
        GridPane.setVgrow(samplesTreeView, Priority.ALWAYS);
        grid.add(samplesTreeView, 0, 1);
        
        // ProgressIndicator
        progressIndicator = new ProgressIndicator();
        progressIndicatorPane = new StackPane(progressIndicator);

        // right hand side
        tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);
        tabPane.getStyleClass().add(TabPane.STYLE_CLASS_FLOATING);
        tabPane.getSelectionModel().selectedItemProperty().addListener(o -> updateTab());
        GridPane.setHgrow(tabPane, Priority.ALWAYS);
        GridPane.setVgrow(tabPane, Priority.ALWAYS);
        grid.add(tabPane, 1, 0, 1, 2);

        sampleTab = new Tab("Sample");
        javaDocTab = new Tab("JavaDoc");
        javaDocWebView = new WebView();
        javaDocTab.setContent(javaDocWebView);

        sourceTab = new Tab("Source");
        sourceWebView = new WebView();
        sourceTab.setContent(sourceWebView);

        cssTab = new Tab("Css");
        cssWebView = new WebView();
        cssTab.setContent(cssWebView);

        // by default we'll show the welcome message of first project in the tree
        // if no projects are available, we'll show the default page
        List<TreeItem<Sample>> projects = samplesTreeView.getRoot().getChildren();
        if(!projects.isEmpty()) {
            TreeItem<Sample> firstProject = projects.get(0);
            samplesTreeView.getSelectionModel().select(firstProject);
        } else {
            changeToWelcomeTab(null);
        }

        // put it all together
        Scene scene = new Scene(grid);
        scene.getStylesheets().add(getClass().getResource("fxsampler.css").toExternalForm());
        for (FXSamplerConfiguration fxsamplerConfiguration : configurationServiceLoader) {
            String stylesheet = fxsamplerConfiguration.getSceneStylesheet();
            if (stylesheet != null) {
                scene.getStylesheets().add(stylesheet);
            }
        }
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(1000);
        primaryStage.setMinHeight(600);
        
        // set width / height values to be 75% of users screen resolution
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        primaryStage.setWidth(screenBounds.getWidth() * 0.75);
        primaryStage.setHeight(screenBounds.getHeight() * .75);
        
        primaryStage.setTitle("FXSampler!");
        primaryStage.show();

        samplesTreeView.requestFocus();
    }

    public final GridPane getGrid() {
        return grid;
    }

    public final TabPane getTabPane() {
        return tabPane;
    }
    // should never be null
    public final Tab getWelcomeTab() {
        return welcomeTab;
    }

    public final Tab getSampleTab() {
        return sampleTab;
    }

    public final Tab getJavaDocTab() {
        return javaDocTab;
    }

    public final Tab getSourceTab() {
        return sourceTab;
    }

    public final Tab getCssTab() {
        return cssTab;
    }

    protected void buildSampleTree(String searchText) {
        // rebuild the whole tree (it isn't memory intensive - we only scan
        // classes once at startup)
        root = new TreeItem<>(new EmptySample("FXSampler"));
        root.setExpanded(true);
        
        for (String projectName : projectsMap.keySet()) {
            final Project project = projectsMap.get(projectName);
            if (project == null) continue;
            
            // now work through the project sample tree building the rest
            TreeNode n = project.getSampleTree().getRoot();
            root.getChildren().add(n.createTreeItem());
        }
        
        // with this newly built and full tree, we filter based on the search text
        if (searchText != null) {
           pruneSampleTree(root, searchText); 
           
           // FIXME weird bug in TreeView I think
           samplesTreeView.setRoot(null);
           samplesTreeView.setRoot(root);
        }
        
        // and finally we sort the display a little
        sort(root, Comparator.comparing(o -> o.getValue().getSampleName()));
    }

    protected void changeSample() {
        if (selectedSample == null) {
            return;
        }
        if (tabPane.getTabs().contains(welcomeTab)) {
            tabPane.getTabs().setAll(sampleTab, javaDocTab, sourceTab, cssTab);
        }
        tabPane.getTabs().forEach(tab -> tab.getProperties().put(TAB_LOAD_CACHE, false));
        updateTab();
    }
    
    private void sort(TreeItem<Sample> node, Comparator<TreeItem<Sample>> comparator) {
        node.getChildren().sort(comparator);
        for (TreeItem<Sample> child : node.getChildren()) {
            sort(child, comparator);
        }
    }
    
    // true == keep, false == delete
    private boolean pruneSampleTree(TreeItem<Sample> treeItem, String searchText) {
        // we go all the way down to the leaf nodes, and check if they match
        // the search text. If they do, they stay. If they don't, we remove them.
        // As we pop back up we check if the branch nodes still have children,
        // and if not we remove them too
        if (searchText == null) return true;
        
        if (treeItem.isLeaf()) {
            // check for match. Return true if we match (to keep), and false
            // to delete
            return treeItem.getValue().getSampleName().toUpperCase().contains(searchText.toUpperCase());
        } else {
            // go down the tree...
            List<TreeItem<Sample>> toRemove = new ArrayList<>();
            
            for (TreeItem<Sample> child : treeItem.getChildren()) {
                boolean keep = pruneSampleTree(child, searchText);
                if (! keep) {
                    toRemove.add(child);
                }
            }
            
            // remove the unrelated tree items
            treeItem.getChildren().removeAll(toRemove);
            
            // return true if there are children to this branch, false otherwise
            // (by returning false we say that we should delete this now-empty branch)
            return ! treeItem.getChildren().isEmpty();
        }
    }
    
    private void updateTab() {
        Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();

        // If the tab was already loaded and its just a tab switch, no need to reload.
        final Object tabLoadCache = selectedTab.getProperties().get(TAB_LOAD_CACHE);
        if (tabLoadCache != null && (boolean)tabLoadCache) return;

        progressIndicator.progressProperty().unbind();
        // we only update the selected tab - leaving the other tabs in their
        // previous state until they are selected
        if (selectedTab == sampleTab) {
            sampleTab.setContent(buildSampleTabContent(selectedSample));
        } else if (selectedTab == javaDocTab) {
            prepareTabContent(javaDocTab, javaDocWebView);
            loadWebViewContent(javaDocWebView, selectedSample, Sample::getJavaDocURL, sample -> "No Javadoc available");
        } else if (selectedTab == sourceTab) {
            prepareTabContent(sourceTab, sourceWebView);
            loadWebViewContent(sourceWebView, selectedSample, Sample::getSampleSourceURL, this::formatSourceCode);
        } else if (selectedTab == cssTab) {
            prepareTabContent(cssTab, cssWebView);
            loadWebViewContent(cssWebView, selectedSample, Sample::getControlStylesheetURL, this::formatCss);
        }  
    }

    private void loadWebViewContent(WebView webView, Sample sample,
                                    Function<Sample, String> urlFunction, Function<Sample, String> contentFunction) {
        final String url = urlFunction.apply(sample);
        if (url != null && url.startsWith("http")) {
            webView.getEngine().load(url);
        } else {
            webView.getEngine().loadContent(contentFunction.apply(sample));
        }
    }

    private void prepareTabContent(Tab tab, WebView webView) {
        tab.setContent(progressIndicatorPane);
        final WebEngine engine = webView.getEngine();
        progressIndicator.progressProperty().bind(engine.getLoadWorker().progressProperty());
        engine.getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == Worker.State.SUCCEEDED) {
                tab.setContent(webView);
                tab.getProperties().put(TAB_LOAD_CACHE,true);
            }
        });
    }

    private String getResource(String resourceName, Class<?> baseClass) {
        Class<?> clz = baseClass == null ? getClass() : baseClass;
        return getResource(clz.getResourceAsStream(resourceName));
    }

    private String getResource(InputStream is) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
            String line;
            StringBuilder sb = new StringBuilder();
            while ((line = br.readLine()) != null) {
                sb.append(line);
                sb.append("\n");
            } 
            return sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }
    
    private String formatSourceCode(Sample sample) {
        String sourceURL = sample.getSampleSourceURL();
        String src;
        if (sourceURL == null) {
            src = "No sample source available";
        } else {
            src = "Sample Source not found";
            try {
               src = getSourceCode(sample);
            } catch(Throwable ex){
                ex.printStackTrace();
            }
        }
        
        // Escape '<' by "&lt;" to ensure correct rendering by SyntaxHighlighter
        src = src.replace("<", "&lt;");
        
        String template = getResource("/fxsampler/util/SourceCodeTemplate.html", null);
        return template.replace("<source/>", src);
    }

    private String getSourceCode(Sample sample) {
        String sourceURL = sample.getSampleSourceURL();
        try {
            // try loading via the web or local file system
            URL url = new URL(sourceURL);
            InputStream is = url.openStream();
            return getResource(is);
        } catch (IOException e) {
            // no-op - the URL may not be valid, no biggy
        }
        return getResource(sourceURL, sample.getClass());
    }

    private String formatCss(Sample sample) {
        String cssUrl = sample.getControlStylesheetURL();
        String src;
        if (cssUrl == null) {
            src = "No CSS source available";
        } else {
            src = "Css not found";
            try {
                if (selectedProject != null && !selectedProject.getModuleName().isEmpty()) {
                    // module-path
                    final Optional<Module> projectModuleOptional = ModuleLayer.boot().findModule(selectedProject.getModuleName());
                    if (projectModuleOptional.isPresent()) {
                        final Module projectModule = projectModuleOptional.get();
                        src = getResource(projectModule.getResourceAsStream(cssUrl));
                    } else {
                        System.err.println("Module name defined doesn't exist");
                    }
                } else {
                    // classpath
                    src = getResource(getClass().getResourceAsStream(cssUrl));
                }
            } catch(Throwable ex){
                ex.printStackTrace();
            }
        }

        // Escape '<' by "&lt;" to ensure correct rendering by SyntaxHighlighter
        src = src.replace("<", "&lt;");
        
        String template = getResource("/fxsampler/util/CssTemplate.html", null);
        return template.replace("<source/>", src);
    }

    private Node buildSampleTabContent(Sample sample) {
        return SampleBase.buildSample(sample, stage);
    }

    private void changeToWelcomeTab(WelcomePage wPage) {
        if(null == wPage) {
            wPage = getDefaultWelcomePage();
        }
        welcomeTab = new Tab(wPage.getTitle());
        welcomeTab.setContent(wPage.getContent());
        tabPane.getTabs().setAll(welcomeTab);
    }
    
    private WelcomePage getDefaultWelcomePage() {
        // line 1
        Label welcomeLabel1 = new Label("Welcome to FXSampler!");
        welcomeLabel1.setStyle("-fx-font-size: 2em; -fx-padding: 0 0 0 5;");

        // line 2
        Label welcomeLabel2 = new Label(
                "Explore the available UI controls and other interesting projects "
                + "by clicking on the options to the left.");
        welcomeLabel2.setStyle("-fx-font-size: 1.25em; -fx-padding: 0 0 0 5;");

        return new WelcomePage("Welcome!", new VBox(5, welcomeLabel1, welcomeLabel2));
    }
}


