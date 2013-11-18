package fxsampler.model;

import fxsampler.Sample;

/**
 * Represents a project such as ControlsFX or JFXtras
 */
public class Project {
    
    private final String name;
    
    private final String basePackage;

    // A Project has a Tree of samples
    private final SampleTree sampleTree;
    
    public Project(String name) {
        this.name = name;
        this.sampleTree = new SampleTree(new EmptySample(name));
        
        // FIXME we shouldn't be hard coding like this, but it'll do for now
        switch (name) {
            case "ControlsFX": basePackage = "org.controlsfx.samples"; break;
            case "JFXtras":    basePackage = "jfxtras.labs.samples"; break;
            default:           basePackage = ""; break;
        }
    }

    public void addSample(String packagePath, Sample sample) {
        // convert something like 'org.controlsfx.samples.actions' to 'samples.actions'
        String packagesWithoutBase = "";
        try {
            if (! basePackage.equals(packagePath)) {
                packagesWithoutBase = packagePath.substring(basePackage.length() + 1);
            }
        } catch (StringIndexOutOfBoundsException e) {
            System.out.println("packagePath: " + packagePath + ", basePackage: " + basePackage);
            e.printStackTrace();
            return;
        }
        
        // then split up the packages into separate strings
        String[] packages = packagesWithoutBase.isEmpty() ? new String[] { } : packagesWithoutBase.split("\\.");
        
        // then for each package convert to a prettier form
        for (int i = 0; i < packages.length; i++) {
            String packageName = packages[i];
            if (packageName.isEmpty()) continue;
            
            packageName = packageName.substring(0, 1).toUpperCase() + packageName.substring(1);
            packageName = packageName.replace("_", " ");
            packages[i] = packageName;
        }
        
        // now we have the pretty package names, we add this sample into the
        // tree in the appropriate place
        sampleTree.addSample(packages, sample);
    }
    
    public SampleTree getSampleTree() {
        return sampleTree;
    }
    
    @Override public String toString() {
        StringBuilder sb = new StringBuilder();
        
        sb.append("Project [ name: ");
        sb.append(name);
        sb.append(", sample count: ");
        sb.append(sampleTree.size());
        sb.append(", tree: ");
        sb.append(sampleTree);
        sb.append(" ]");
        
        return sb.toString();
    }
}
