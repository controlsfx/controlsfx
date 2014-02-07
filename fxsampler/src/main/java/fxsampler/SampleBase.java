package fxsampler;

import javafx.application.Application;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;

/**
 * A base class for samples - it is recommended that they extend this class
 * rather than Application, as then the samples can be run either standalone
 * or within FXSampler. 
 */
public abstract class SampleBase extends Application implements Sample {
    
    /** {@inheritDoc} */
    @Override public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle(getSampleName());
        
        Scene scene = new Scene((Parent)buildSample(this, primaryStage), 800, 800);
        scene.getStylesheets().add(SampleBase.class.getResource("fxsampler.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    /** {@inheritDoc} */
    @Override public boolean isVisible() {
        return true;
    }
    
    /** {@inheritDoc} */
    @Override public Node getControlPanel() {
        return null;
    }
    
    /** {@inheritDoc} */
    @Override public String getSampleDescription() {
        return "";
    }
    
    /** {@inheritDoc} */
    @Override public String getProjectName() {
        return "ControlsFX";
    }
    
    /**
     * Utility method to create the default look for samples.
     */
    public static Node buildSample(Sample sample, Stage stage) {
        SplitPane splitPane = new SplitPane();
        splitPane.setDividerPosition(0, 0.6);
        
        // we guarantee that the build order is panel then control panel.
        final Node samplePanel = sample.getPanel(stage);
        final Node controlPanel = sample.getControlPanel();
        
        if (samplePanel != null) {
            splitPane.getItems().add(samplePanel);
        }
        
        final VBox rightPanel = new VBox();
        rightPanel.getStyleClass().add("right-panel");
        rightPanel.setMaxHeight(Double.MAX_VALUE);

        boolean addRightPanel = false;
        
        Label sampleName = new Label(sample.getSampleName());
        sampleName.getStyleClass().add("sample-name");
        rightPanel.getChildren().add(sampleName);
        
        // --- project name & version
        String version = sample.getProjectVersion();
        version = version == null ? "" : 
                  version.equals("@version@") ? "" :
                  " " + version.trim();
        
        final String projectName = sample.getProjectName() + version;
        if (projectName != null && ! projectName.isEmpty()) {
            Label projectNameTitleLabel = new Label("Project: ");
            projectNameTitleLabel.getStyleClass().add("project-name-title");
            
            Label projectNameLabel = new Label(projectName);
            projectNameLabel.getStyleClass().add("project-name");
            projectNameLabel.setWrapText(true);
            
            TextFlow textFlow = new TextFlow(projectNameTitleLabel, projectNameLabel);
            rightPanel.getChildren().add(textFlow);
        }
        
        // --- description
        final String description = sample.getSampleDescription();
        if (description != null && ! description.isEmpty()) {
            Label descriptionLabel = new Label(description);
            descriptionLabel.getStyleClass().add("description");
            descriptionLabel.setWrapText(true);
            rightPanel.getChildren().add(descriptionLabel);
            
            addRightPanel = true;
        }
        
        if (controlPanel != null) {
            rightPanel.getChildren().add(new Separator());
            
            controlPanel.getStyleClass().add("control-panel");
            rightPanel.getChildren().add(controlPanel);
            VBox.setVgrow(controlPanel, Priority.ALWAYS);
            addRightPanel = true;
        }
        
        if (addRightPanel) {
            ScrollPane scrollPane = new ScrollPane(rightPanel);
            scrollPane.setMaxHeight(Double.MAX_VALUE);
            scrollPane.setFitToWidth(true);
            scrollPane.setFitToHeight(true);
            splitPane.getItems().add(scrollPane);
        }
        
        return splitPane;
    }
}
