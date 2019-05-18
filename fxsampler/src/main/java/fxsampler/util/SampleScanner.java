package fxsampler.util;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.module.ModuleReader;
import java.lang.module.ResolvedModule;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;

import fxsampler.FXSamplerProject;
import fxsampler.Sample;
import fxsampler.model.EmptySample;
import fxsampler.model.Project;

/**
 * All the code related to classpath scanning, etc for samples.
 */
public class SampleScanner {
    
    private static List<String> ILLEGAL_CLASS_NAMES = new ArrayList<>();
    static {
        ILLEGAL_CLASS_NAMES.add("/com/javafx/main/Main.class");
        ILLEGAL_CLASS_NAMES.add("/com/javafx/main/NoJavaFXFallback.class");
    }
    
    private static Map<String, FXSamplerProject> packageToProjectMap = new HashMap<>();
    static {
        System.out.println("Initialising FXSampler sample scanner...");
        System.out.println("\tDiscovering projects...");
        // find all projects on the classpath that expose a FXSamplerProject
        // service. These guys are our friends....
        ServiceLoader<FXSamplerProject> loader = ServiceLoader.load(FXSamplerProject.class);
        for (FXSamplerProject project : loader) {
            final String projectName = project.getProjectName();
            final String basePackage = project.getSampleBasePackage();
            packageToProjectMap.put(basePackage, project);
            System.out.println("\t\tFound project '" + projectName + 
                    "', with sample base package '" + basePackage + "'");
        }
        
        if (packageToProjectMap.isEmpty()) {
            System.out.println("\tError: Did not find any projects!");
        }
    }
    
    private final Map<String, Project> projectsMap = new HashMap<>();
    
    /**
     * Gets the list of sample classes to load
     *
     * @return The classes
     */
    public Map<String, Project> discoverSamples() {
        Class<?>[] results = new Class[] { };
        
        try {
        	  results = loadFromPathScanning();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        for (Class<?> sampleClass : results) {
            if (! Sample.class.isAssignableFrom(sampleClass)) continue;
            if (sampleClass.isInterface()) continue;
            if (Modifier.isAbstract(sampleClass.getModifiers())) continue;
//            if (Sample.class.isAssignableFrom(EmptySample.class)) continue;
            if (sampleClass == EmptySample.class) continue;
            
            Sample sample = null;
            try {
                sample = (Sample)sampleClass.getDeclaredConstructor().newInstance();
            } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                e.printStackTrace();
            }
            if (sample == null || ! sample.isVisible()) continue;

            final String packageName = sampleClass.getPackage().getName();
            
            for (String key : packageToProjectMap.keySet()) {
                if (packageName.contains(key)) {
                    final String prettyProjectName = packageToProjectMap.get(key).getProjectName();
                    
                    Project project;
                    if (! projectsMap.containsKey(prettyProjectName)) {
                        project = new Project(prettyProjectName, key);
                        project.setWelcomePage(packageToProjectMap.get(key).getWelcomePage());
                        projectsMap.put(prettyProjectName, project);
                    } else {
                        project = projectsMap.get(prettyProjectName);
                    }
                    
                    project.addSample(packageName, sample);
                }
            }
        }
        
        return projectsMap;
    } 

    /**
     * Scans all classes.
     *
     * @return The classes
     * @throws IOException
     */
    private Class<?>[] loadFromPathScanning() throws IOException {

        final Set<Class<?>> classes = new LinkedHashSet<>();
        // scan the module-path
        ModuleLayer.boot().configuration().modules().stream()
                .map(ResolvedModule::reference)
                .filter(rm -> !isSystemModule(rm.descriptor().name()))
                .forEach(mref -> {
                    try (ModuleReader reader = mref.open()) {
                        reader.list().forEach(c -> {
                            final Class<?> clazz = processClassName(c);
                            if (clazz != null) {
                                classes.add(clazz);
                            }
                        });
                    } catch (IOException ioe) {
                        throw new UncheckedIOException(ioe);
                    }
                });
        
        return classes.toArray(new Class[classes.size()]);
    }

    private Class<?> processClassName(final String name) {
        String className = name.replace("\\", ".");
        className = className.replace("/", ".");
        
        // some cleanup code
        if (className.contains("$")) {
            // we don't care about samples as inner classes, so 
            // we jump out
            return null;
        }
        if (className.contains(".bin")) {
            className = className.substring(className.indexOf(".bin") + 4);
            className = className.replace(".bin", "");
        }
        if (className.startsWith(".")) {
            className = className.substring(1);
        }
        if (className.endsWith(".class")) {
            className = className.substring(0, className.length() - 6);
        }

        Class<?> clazz = null;
        try {
            clazz = Class.forName(className);
        } catch (Throwable e) {
            // Throwable, could be all sorts of bad reasons the class won't instantiate
//            System.out.println("ERROR: Class name: " + className);
//            System.out.println("ERROR: Initial filename: " + name);
//            e.printStackTrace();
        }
        return clazz;
    }

    /**
     * Return true if the given module name is a system module. There can be
     * system modules in layers above the boot layer.
     */
    private static boolean isSystemModule(final String moduleName) {
        return moduleName.startsWith("java.")
                || moduleName.startsWith("javax.")
                || moduleName.startsWith("javafx.")
                || moduleName.startsWith("jdk.")
                || moduleName.startsWith("oracle.");
    }
}
