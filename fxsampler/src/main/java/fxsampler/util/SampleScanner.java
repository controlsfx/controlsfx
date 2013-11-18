package fxsampler.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
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
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

import fxsampler.FXSampler;
import fxsampler.FXSamplerProject;
import fxsampler.Sample;
import fxsampler.model.EmptySample;
import fxsampler.model.Project;

/**
 * All the code related to classpath scanning, etc for samples.
 */
public class SampleScanner {
    
    private static Map<String, String> packageToProjectMap = new HashMap<String, String>();
    static {
        System.out.println("Initialising FXSampler sample scanner...");
        System.out.println("\tDiscovering projects...");
        // find all projects on the classpath that expose a FXSamplerProject
        // service. These guys are our friends....
        ServiceLoader<FXSamplerProject> loader = ServiceLoader.load(FXSamplerProject.class);
        for (FXSamplerProject project : loader) {
            final String projectName = project.getProjectName();
            final String basePackage = project.getSampleBasePackage();
            packageToProjectMap.put(basePackage, projectName);
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
            results = loadFromEnumeratedFile();
            if (results == null) {
                results = loadFromJarSniffing();
            }
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
                    final String prettyProjectName = packageToProjectMap.get(key);
                    
                    Project project;
                    if (! projectsMap.containsKey(prettyProjectName)) {
                        project = new Project(prettyProjectName);
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
      
    // TODO this needs to be made generic, to look in the jar files it finds
    private Class<?>[] loadFromEnumeratedFile() throws ClassNotFoundException, IOException {
        ClassLoader classLoader = FXSampler.class.getClassLoader();

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(
                        FXSampler.class.getResourceAsStream("samples/samples.txt")))) 
        {
            List<Class<?>> classes = new ArrayList<>();
            for (String sample = br.readLine(); sample != null; sample = br.readLine()) {
                if (sample.endsWith(".java")) {
                    sample = sample.substring(0, sample.length() - ".java".length());
                    classes.add(classLoader.loadClass(sample.replace("/", ".")));
                }
            }
            return classes.toArray(new Class[classes.size()]);
        } catch (NullPointerException npe) {
            // samples.txt doesn't exist
            return null;
        }
    }


    /**
     * Scans all classes accessible from the context class loader which belong 
     * to the given package and subpackages.
     *
     * @return The classes
     * @throws ClassNotFoundException
     * @throws IOException
     */
    private Class<?>[] loadFromJarSniffing() throws ClassNotFoundException, IOException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
//        String path = "META-INF/MANIFEST.MF";//packageName.replace('.', '/');
        String path = "";//packageName.replace('.', '/');

        Enumeration<URL> resources = classLoader.getResources(path);
        List<File> dirs = new ArrayList<>();
        while (resources.hasMoreElements()) {
            URL next = resources.nextElement();
            if (next.toExternalForm().contains("/jre/")) continue;

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
        ArrayList<Class<?>> classes = new ArrayList<>();
        for (File directory : dirs) {
            String fullPath = directory.getAbsolutePath();
            
            if (fullPath.endsWith("jfxrt.jar")) continue;

            if (fullPath.toLowerCase().endsWith(".jar")) {
                // scan the jar
                classes.addAll(findClassesInJar(new File(fullPath)));
            } else {
                // scan the classpath
                classes.addAll(findClassesInDirectory(directory));
            }
        }
        return classes.toArray(new Class[classes.size()]);
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
    
    private void processPath(Path path, List<Class<?>> classes) throws IOException {
        String root = path.toString();
        
        Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
            @Override public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                String name = file.toString();
                if (name.endsWith(".class")) {
                    
                    // remove root path to make class name correct in all cases
                    name = name.substring(root.length());
                    
                    Class<?> clazz = processClassName(name);
                    if (clazz != null) {
                        classes.add(clazz);
                    }
                }
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
            System.out.println("Class name: " + className);
            System.out.println("Initial filename: " + name);
            e.printStackTrace();
        }
        return clazz;
    } 
}
