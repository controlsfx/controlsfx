package fxsampler.model;

import javafx.scene.Node;
import javafx.stage.Stage;
import fxsampler.Sample;

public class EmptySample implements Sample {
    private final String name;

    public EmptySample(String name) {
        this.name = name;
    }

    @Override public String getSampleName() {
        return name;
    }

    @Override public String getSampleDescription() {
        return null;
    }
    
    @Override public String getProjectName() {
        return null;
    }

	@Override
	public String getProjectVersion() {
		return null;
	}
	
    @Override public Node getPanel(Stage stage) {
        return null;
    }

    @Override public String getJavaDocURL() {
        return null;
    }
    
    @Override public String getSampleSourceURL() {
        return null;
    }

    @Override public boolean isVisible() {
        return true;
    }

    @Override public Node getControlPanel() {
        return null;
    }
    
    public double getControlPanelDividerPosition() {
    	return 0.6;
    }

	@Override
	public String getControlStylesheetURL() {
		return null;
	}

}