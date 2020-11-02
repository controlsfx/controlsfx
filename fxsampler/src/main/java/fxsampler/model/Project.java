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

    private String moduleName;
    // Pojo that holds the welcome tab content and title
    private WelcomePage welcomePage;
    
    public Project(String name, String basePackage) {
        this.name = name;
        this.basePackage = basePackage;
        this.sampleTree = new SampleTree(new EmptySample(name));
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

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public void setWelcomePage(WelcomePage welcomePage) {
        if(null != welcomePage) {
            this.welcomePage = welcomePage;
        }
    }
    
    public WelcomePage getWelcomePage() {
        return this.welcomePage;
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
