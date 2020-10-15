package org.controlsfx;

import java.io.InputStream;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.controlsfx.samples.Utils;

import fxsampler.SampleBase;

public abstract class ControlsFXSample extends SampleBase {

	private static final ProjectInfo projectInfo = new ProjectInfo();
	
	@Override
	public String getProjectName() {
		return "ControlsFX";
	}

	@Override
	public String getProjectVersion() {
		return projectInfo.getVersion();
	}
	
	@Override public String getSampleSourceURL() {
	    return Utils.SAMPLES_BASE + getClass().getName().replace('.','/') + ".java";
	}
	
	@Override
	public String getControlStylesheetURL() {
		return null;
	}
	
	private static class ProjectInfo {
		
		private String version;
		
		
		public ProjectInfo() {
			
			InputStream s = getClass().getClassLoader().getResourceAsStream(
					"META-INF/MANIFEST.MF");
			
			try {
				Manifest manifest = new Manifest(s);
				Attributes attr = manifest.getMainAttributes();
				version = attr.getValue("Implementation-Version");
			} catch (Throwable e) {
				System.out.println("Unable to load project version for ControlsFX "
				        + "samples project as the manifest file can't be read "
				        + "or the Implementation-Version attribute is unavailable.");
				version = "";
			}
		}
		
		public String getVersion() {
			return version;
		}
	}
}

