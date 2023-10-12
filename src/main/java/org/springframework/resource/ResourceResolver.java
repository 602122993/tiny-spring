package org.springframework.resource;

import org.springframework.annotations.ComponentScan;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ResourceResolver {




    public static List<Class<?>> scanPackage(String packageName) throws ClassNotFoundException {
        List<Class<?>> classes = new ArrayList<>();
        String path = packageName.replace('.', '/');
        //获取当前类加载器
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try {
            //获取资源下所有的class资源
            Enumeration<URL> resources = classLoader.getResources(path);
            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                File file = new File(resource.getFile());
                if (file.isDirectory()) {
                    //文件夹递归扫描
                    scanClassesInDirectory(packageName, file, classes);
                } else if (resource.getProtocol().equals("jar")) {
                    //jar包单独扫描
                    JarURLConnection connection = (JarURLConnection) resource.openConnection();
                    JarFile jarFile = connection.getJarFile();
                    classes.addAll(scanClassesFromJar(jarFile, packageName));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return classes;
    }


    private static void scanClassesInDirectory(String packageName, File directory, List<Class<?>> classes) throws ClassNotFoundException {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    scanClassesInDirectory(packageName + "." + file.getName(), file, classes);
                } else if (file.getName().endsWith(".class")) {
                    String className = packageName + '.' + file.getName().substring(0, file.getName().length() - 6);
                    Class<?> clazz = Class.forName(className);
                    classes.add(clazz);
                }
            }
        }
    }

    private static List<Class<?>> scanClassesFromJar(JarFile jarFile, String packageName) throws ClassNotFoundException {
        List<Class<?>> classes = new ArrayList<>();
        Enumeration<JarEntry> entries = jarFile.entries();
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            String entryName = entry.getName().replace("/", ".");
            if (entryName.startsWith(packageName) && entryName.endsWith(".class")) {
                String className = entryName.substring(0, entryName.length() - 6);
                Class<?> clazz = Class.forName(className);
                classes.add(clazz);
            }
        }

        return classes;
    }
    public static List<String> findScanPackageName(Class<?> startPackage) {
        ComponentScan componentScan = startPackage.getAnnotation(ComponentScan.class);
        if (componentScan == null || componentScan.packages() == null) {
            return Collections.singletonList(startPackage.getName().contains(".")?startPackage.getName().substring(0, startPackage.getName().lastIndexOf(".")): startPackage.getName());
        }
        return new ArrayList<>(Arrays.asList(componentScan.packages()));
    }
}
