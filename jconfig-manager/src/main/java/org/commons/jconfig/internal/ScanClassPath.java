package org.commons.jconfig.internal;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.log4j.Logger;
import org.commons.jconfig.config.ConfigRuntimeException;


public class ScanClassPath<T extends Annotation> {

    private final Class<T> annoClazz;

    /**
     * package excludeFilter to filter common classes, like jdk and apache jars. this
     * dramatically reduces look up time.
     */
    private final Set<String> excludeFilter;
    
    private final Set<String> allowFilter = new HashSet<String>();

    /**
     * Set of all the resource paths which will be scanned. This set is build as
     * and when paths are scanned and will block scanning the same path again
     * thereby avoiding recursion.
     */
    private final Set<String> dirLookupSet = new HashSet<String>(); 
    
    private ScanClassPath(final Class<T> annoClazz) {
        this.annoClazz = annoClazz;
        String[] packageFilter = { "java.", "javax.", "org.ietf.jgss", "org.omg.", "org.w3c.dom.", "org.xml.sax.",
                "sun.tools.", "sun.jvmstat.", "com.sun.", "org.junit.", "org.testng.", "bsh.", "org.relaxng.",
                "mockit.", "com.beust.", "org.apache.log4j." };
        excludeFilter = new HashSet<String>(Arrays.asList(packageFilter));
    }

    public ScanClassPath(final Class<T> annoClazz, List<String> allowFilter) {
        this(annoClazz);
        if(allowFilter == null) {
            throw new IllegalArgumentException("Allow filter cannot be null");
        }
        this.allowFilter.addAll(allowFilter);
    }

    
    private boolean isAllowed(final String clazzName) {
        // use allow filters
        if (!allowFilter.isEmpty()) {
            for (String allowPrefix : allowFilter) {
                if (clazzName.startsWith(allowPrefix)) {
                    return true;
                }
            }
            return false;
        }

        // if allowfilters are empty use default exclude filters
        String[] tokens = clazzName.split("\\.");
        String path = "";
        for (String token : tokens) {
            path += token + ".";
            if (excludeFilter.contains(path)) {
                return false;
            }
        }
        return true;
    }

    private final Logger logger = Logger.getLogger(this.getClass());

    /**
     * Helper class to filter all the files in a directory with a particular
     * extension
     */
    private class FileListFilter implements FilenameFilter {

        private final String name;

        private final String extension;

        public FileListFilter(final String name, final String extension) {
            this.name = name;
            this.extension = extension;
        }

        @Override
        public boolean accept(final File directory, final String filename) {
            boolean fileOK = true;

            if (name != null) {
                fileOK &= filename.startsWith(name);
            }

            if (extension != null) {
                fileOK &= filename.endsWith('.' + extension);
            }
            return fileOK;
        }

    }

    /**
     * Return all the classes in the jars in a given path
     * 
     * @param path
     * @return Set of classes in given jar
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public Set<Class<?>> scanJarAnnotatedClasses(final JarFile jar) throws IOException {
        Set<Class<?>> clazzez = new HashSet<Class<?>>();
        Enumeration<JarEntry> it = jar.entries();
        while (it.hasMoreElements()) {
            JarEntry jarEntry = it.nextElement();
            if (jarEntry.getName().endsWith(".class")) {
                String className = jarEntry.getName().replaceAll("/", "\\.");
                Class<?> clazz = applyFilter(className.substring(0, className.length() - 6));
                if (clazz != null) {
                    clazzez.add(clazz);
                }
            }
        }
        return clazzez;
    }

    /**
     * Returns all the classes in the classpath that are annotated.
     */
    public Set<Class<?>> scanAnnotatedClasses() {
        Set<Class<?>> clazzez = new HashSet<Class<?>>();
        clazzez.addAll(scanPackagesAnnotatedClasses());
        clazzez.addAll(scanURLClassLoaderAnnotatedClasses());
        return clazzez;
    }

    private Set<Class<?>> scanPackagesAnnotatedClasses() {
        Set<Class<?>> clazzez = new HashSet<Class<?>>();
        Package[] packages = Package.getPackages();
        for (Package lPackage : packages) {
            try {
                clazzez.addAll(scanPackageAnnotatedClasses(lPackage));
            } catch (IOException e) {
                logger.warn("ScanConfigClasses failed for package: " + lPackage.getName(), e);
            }
        }
        return clazzez;
    }

    private Set<Class<?>> scanURLClassLoaderAnnotatedClasses() {
        final ClassLoader classLoader = this.getClass().getClassLoader();
        if (!(classLoader instanceof URLClassLoader)) {
            throw new IllegalArgumentException("Classloader is not a URL classloader");
        }
        final URLClassLoader urlClassLoader = (URLClassLoader) classLoader;
        URL[] urls = urlClassLoader.getURLs();
        Set<Class<?>> clazzez = new HashSet<Class<?>>();
        for (URL url : urls) {
            try {
                clazzez.addAll(scanURLAnnotatedClasses(url, ""));
            } catch (IOException e) {
                // Ignore IOException and continue to iterate thru the array
                // elements.
            }
        }
        return clazzez;
    }

    private Class<?> applyFilter(final String clazzName) {
        if (isAllowed(clazzName)) {
            try {
                Class<?> clazz = Class.forName(clazzName, false, this.getClass().getClassLoader());
                T annotation = clazz.getAnnotation(annoClazz);
                if (annotation != null) {
                    return clazz;
                }
            } catch (ClassNotFoundException e) {
                logger.trace("Unable to search classes for annotations: " + clazzName);
                // Ignore class not found
            } catch (NoClassDefFoundError e) {
                logger.trace("Unable to search classes for annotations: " + clazzName);
                // Ignore class not found
            } catch (UnsatisfiedLinkError e) {
                logger.trace("Unable to search classes for annotations: " + clazzName);
                // Ignore classes that required jni libraries that are not
                // present to be loaded
            } catch (UnsupportedClassVersionError e) {
                logger.trace("Unable to search classes for annotations: " + clazzName);
                // Ignore unsupported classes
            }
        }
        return null;
    }

    /**
     * Returns all classes with the annotation which belong to the given package
     * and sub-packages.
     * 
     * @param package The base package
     * @return The classes
     * @throws IOException
     */
    public Set<Class<?>> scanPackageAnnotatedClasses(final Package pPackage) throws IOException {
        String path = pPackage.getName().replace('.', '/');
        Enumeration<URL> resources = Thread.currentThread().getContextClassLoader().getResources(path);
        Set<Class<?>> clazzes = new HashSet<Class<?>>();

        while (resources.hasMoreElements()) {
            clazzes.addAll(scanURLAnnotatedClasses(resources.nextElement(), pPackage.getName()));
        }
        return clazzes;
    }

    /**
     * Helper function to recursively traverse the directory to find annotated
     * classes
     * 
     * @param directory
     * @param packageName
     * @return
     * @throws IOException
     * @throws Exception
     */
    private Set<Class<?>> scanURLAnnotatedClasses(final URL directoryUrl, final String packageName) throws IOException {
        Set<Class<?>> clazzez = new HashSet<Class<?>>();
        String directoryName = directoryUrl.toExternalForm();
        if (directoryName.startsWith("jar:file:") && directoryName.contains("!")) {
            String[] split = directoryName.split("!");
            split = split[0].split(":");
            clazzez.addAll(scanJarAnnotatedClasses(new JarFile(split[2])));
            return clazzez;
        } else if (directoryName.startsWith("file:")) {
            directoryName = directoryName.substring(5);
            File directory = new File(directoryName);
            if (!directory.exists()) {
                return clazzez;
            }
            if(directory.isFile() && directory.getPath().endsWith(".jar")) {
                clazzez.addAll(scanJarAnnotatedClasses(directory));
                return clazzez;
            }
            File[] files = directory.listFiles();
            if (files == null) {
                return clazzez;
            }
            for (File file : files) {
                if (file.isDirectory()) {
                    if (!dirLookupSet.contains(file.getCanonicalPath())) {
                        dirLookupSet.add(file.getCanonicalPath());
                        String prefix = packageName + ".";
                        if (packageName.isEmpty()) {
                            prefix = "";
                        }
                        clazzez.addAll(scanURLAnnotatedClasses(new URL("file:" + file.getAbsolutePath()), prefix + file.getName()));
                    }
                } else if (file.getName().endsWith(".class")) {
                    String className = packageName + '.' + file.getName();
                    Class<?> clazz = applyFilter(className.substring(0, className.length() - 6));
                    if (clazz != null) {
                        clazzez.add(clazz);
                    }
                }
            }
            return clazzez;
        } else {
            logger.error("code should never reach here");
            return clazzez;
        }
    }

    /**
     * Return all the classes with annotation in the given path
     * 
     * @param dir
     *            path to a given directory
     * @return
     */
    public Set<Class<?>> scanDirAnnotatedClasses(final File dir) {
        Set<Class<?>> clazzez = new HashSet<Class<?>>();
        FilenameFilter filter = new FileListFilter("", "class");
        if (dir.isDirectory()) {
            for (String file : dir.list(filter)) {
                Class<?> clazz = applyFilter(file.substring(0, file.length() - 6));
                if (clazz != null) {
                    clazzez.add(clazz);
                }
            }
        }
        return clazzez;
    }

    private void addURL(final URL url) {
        final ClassLoader classLoader = this.getClass().getClassLoader();
        if (!(classLoader instanceof URLClassLoader)) {
            throw new IllegalArgumentException("Classloader is not a URL classloader, failed to add '" + url + "'");
        }
        final URLClassLoader urlClassLoader = (URLClassLoader) classLoader;
        List<URL> urls = Arrays.asList(urlClassLoader.getURLs());
        if (!urls.contains(url)) {
            try {
                logger.debug("Adding URL to classpath " + url);

                Method addURL = URLClassLoader.class.getDeclaredMethod("addURL", new Class[] { URL.class });
                addURL.setAccessible(true);
                addURL.invoke(urlClassLoader, new Object[] { url });
            } catch (SecurityException e) {
                throw new ConfigRuntimeException("Classloader failed to load URL '" + url + "'", e);
            } catch (NoSuchMethodException e) {
                throw new ConfigRuntimeException("Classloader failed to load URL '" + url + "'", e);
            } catch (IllegalArgumentException e) {
                throw new ConfigRuntimeException("Classloader failed to load URL '" + url + "'", e);
            } catch (IllegalAccessException e) {
                throw new ConfigRuntimeException("Classloader failed to load URL '" + url + "'", e);
            } catch (InvocationTargetException e) {
                throw new ConfigRuntimeException("Classloader failed to load URL '" + url + "'", e);
            }
        }
    }

    public void addFileToClassPath(final String absolutePath) {
        String urlPath = "jar:file://" + absolutePath + "!/";
        try {
            addURL(new URL(urlPath));
        } catch (MalformedURLException e) {
            throw new ConfigRuntimeException("Classloader failed to load URL '" + urlPath + "'", e);
        }
    }

    public void addFileToClassPath(final File path) {
        addFileToClassPath(path.getAbsolutePath());
    }

    public void addAllFilesToClassPath(final File[] paths) {
        for (File path : paths) {
            addFileToClassPath(path);
        }
    }

    public Set<Class<?>> scanJarAnnotatedClasses(final File[] paths) {
        Set<Class<?>> clazzes = new HashSet<Class<?>>();
        for (File path : paths) {
            try {
                clazzes.addAll(scanJarAnnotatedClasses(path));
            } catch (IOException e) {
                // Ignore IO exception and continue to loop on the rest of the
                // elements of the array
            }
        }
        return clazzes;
    }

    public Set<Class<?>> scanJarAnnotatedClasses(final File path) throws IOException {
        return scanJarAnnotatedClasses(new JarFile(path));
    }

    public Set<String> scanPackageToStringSet(String packageName) throws IOException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        Set<String> names = new HashSet<String>();

        // i "." vanno sostitutiti con "/"
        packageName = packageName.replace(".", File.separator) + File.separator;
        URL packageURL = classLoader.getResource(packageName);

        if (packageURL.getProtocol().equals("jar")) {
            String jarFileName = packageURL.getFile();
            jarFileName = jarFileName.substring(5, jarFileName.indexOf("!"));
            JarFile jf = new JarFile(jarFileName);
            Enumeration<JarEntry> jarEntries = jf.entries();

            while (jarEntries.hasMoreElements()) {
                String entryName = jarEntries.nextElement().getName();
                if (entryName.startsWith(packageName) && entryName.length() > packageName.length() + 5) {
                    entryName = entryName.substring(packageName.length(), entryName.lastIndexOf('.'));
                    names.add(entryName);
                }
            }
        } else {
            File folder = new File(packageURL.getFile());
            File[] files = folder.listFiles();
            for (File actual : files) {
                String entryName = actual.getName();
                entryName = entryName.substring(0, entryName.lastIndexOf('.'));
                names.add(entryName);
            }
        }
        return names;
    }

    public Set<File> scanPackageToFileSet(final String packageName) throws IOException {
        Set<File> clazzez = new HashSet<File>();
        Enumeration<URL> urls = Thread.currentThread().getContextClassLoader().getResources(packageName);
        while (urls.hasMoreElements()) {
            URL url = urls.nextElement();
            File dir = new File(url.getFile());
            clazzez.addAll(Arrays.asList(dir.listFiles()));
        }
        return clazzez;
    }

}
