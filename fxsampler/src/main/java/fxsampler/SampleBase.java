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

import java.util.ServiceLoader;

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
    @Override public void start(Stage primaryStage) {
        ServiceLoader<FXSamplerConfiguration> configurationServiceLoader = ServiceLoader.load(FXSamplerConfiguration.class);

        primaryStage.setTitle(getSampleName());

        Scene scene = new Scene((Parent)buildSample(this, primaryStage), 800, 800);
        scene.getStylesheets().add(SampleBase.class.getResource("fxsampler.css").toExternalForm());
        for (FXSamplerConfiguration fxsamplerConfiguration : configurationServiceLoader) {
            String stylesheet = fxsamplerConfiguration.getSceneStylesheet();
            if (stylesheet != null) {
                scene.getStylesheets().add(stylesheet);
            }
        }
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
    @Override
    public double getControlPanelDividerPosition() {
        return 0.6;
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

        // we guarantee that the build order is panel then control panel.
        final Node samplePanel = sample.getPanel(stage);
        final Node controlPanel = sample.getControlPanel();
        splitPane.setDividerPosition(0, sample.getControlPanelDividerPosition());

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
        if (!projectName.isEmpty()) {
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
            SplitPane.setResizableWithParent(scrollPane, false);
            splitPane.getItems().add(scrollPane);
        }

        return splitPane;
    }

    @Override
    public void dispose() {
    }
}
