package fxsampler.util;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Enumeration;
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
     * @throws ClassNotFoundException
     * @throws IOException
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
                sample = (Sample)sampleClass.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
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
     * @throws ClassNotFoundException
     * @throws IOException
     */
    private Class<?>[] loadFromPathScanning() throws ClassNotFoundException, IOException {
        final List<File> dirs = new ArrayList<>();
        final List<File> jars = new ArrayList<>();
        
        // scan the classpath
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        String path = "";
        Enumeration<URL> resources = classLoader.getResources(path);
        while (resources.hasMoreElements()) {
            URL url = resources.nextElement();
            
            if (url.toExternalForm().contains("/jre/")) continue;
            
            // Only "file" and "jar" URLs are recognized, other schemes will be ignored.
            String protocol = url.getProtocol().toLowerCase();
            if ("file".equals(protocol)) {
                dirs.add(new File(url.getFile()));
            } else if ("jar".equals(protocol)) {
                String fileName = new URL(url.getFile()).getFile();
                
                // JAR URL specs must contain the string "!/" which separates the name
                // of the JAR file from the path of the resource contained in it, even
                // if the path is empty.
                int sep = fileName.indexOf("!/");
                if (sep > 0) {
                    jars.add(new File(fileName.substring(0, sep)));
                }
            }
        }

        // and also scan the current working directory
        final Path workingDirectory = new File("").toPath();
        scanPath(workingDirectory, dirs, jars);
        
        // process directories first, then jars, so that classes take precedence
        // over built jars (it makes rapid development easier in the IDE)
        final Set<Class<?>> classes = new LinkedHashSet<>();
        for (File directory : dirs) {
            classes.addAll(findClassesInDirectory(directory));
        }
        for (File jar : jars) {
            String fullPath = jar.getAbsolutePath();
            if (fullPath.endsWith("jfxrt.jar")) continue;
            classes.addAll(findClassesInJar(new File(fullPath)));
        }
        
        return classes.toArray(new Class[classes.size()]);
    }

    private void scanPath(Path workingDirectory, final List<File> dirs, final List<File> jars) throws IOException {
        Files.walkFileTree(workingDirectory, new SimpleFileVisitor<Path>() {
            @Override public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
                final File file = path.toFile();
                final String fullPath = file.getAbsolutePath();
                final String name = file.toString();
                
                if (fullPath.endsWith("jfxrt.jar") || name.contains("jre")) {
                    return FileVisitResult.CONTINUE;
                }
                
                if (file.isDirectory()) {
                    dirs.add(file);
                } else if (name.toLowerCase().endsWith(".jar")) {
                    jars.add(file);
                }
                return FileVisitResult.CONTINUE;
            }

            @Override public FileVisitResult visitFileFailed(Path file, IOException ex) {
                System.err.println(ex + " Skipping...");
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private List<Class<?>> findClassesInDirectory(File directory) throws IOException {
        List<Class<?>> classes = new ArrayList<>();
        if (!directory.exists()) {
            System.out.println("Directory does not exist: " + directory.getAbsolutePath());
            return classes;
        }
        
        processPath(directory.toPath(), classes);
        return classes;
    }

    private List<Class<?>> findClassesInJar(File jarFile) throws IOException, ClassNotFoundException {
        List<Class<?>> classes = new ArrayList<>();
        if (!jarFile.exists()) {
            System.out.println("Jar file does not exist here: " + jarFile.getAbsolutePath());
            return classes;
        }
        
        FileSystem jarFileSystem = FileSystems.newFileSystem(jarFile.toPath(), null);
        processPath(jarFileSystem.getPath("/"), classes);
        return classes;
    }
    
    private void processPath(Path path, final List<Class<?>> classes) throws IOException {
        final String root = path.toString();
        
        Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
            @Override public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                String name = file.toString();
                if (name.endsWith(".class") && ! ILLEGAL_CLASS_NAMES.contains(name)) {
                    
                    // remove root path to make class name correct in all cases
                    name = name.substring(root.length());
                    
                    Class<?> clazz = processClassName(name);
                    if (clazz != null) {
                        classes.add(clazz);
                    }
                }
                return FileVisitResult.CONTINUE;
            }

            @Override public FileVisitResult visitFileFailed(Path file, IOException ex) {
                System.err.println(ex + " Skipping...");
                return FileVisitResult.CONTINUE;
            }
        });        
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
}
