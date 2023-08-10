package io.rapidw.mqttrpc.client;

import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

@Slf4j
public class JarStreamClassLoader extends ClassLoader {

    private final HashMap<String, byte[]> classes = new HashMap<>();
    private final HashMap<String, URL> resources = new HashMap<>();

    public JarStreamClassLoader(ClassLoader parent) {
        super(parent);
    }

    public JarStreamClassLoader(JarInputStream jarInputStream, ClassLoader parent) throws IOException {
        super(parent);
        parseJar(jarInputStream);
    }

    private void parseJar(JarInputStream jarInputStream) throws IOException {

        JarEntry entry = jarInputStream.getNextJarEntry();
        while (entry != null) {
            String name = entry.getName();
            boolean isFile = !entry.isDirectory();
            boolean isClassFile = isFile && name.endsWith(".class");
            boolean isJarFile = isFile && name.endsWith(".jar");

            if (isClassFile) {
                String className = pathToClassName(name);
                byte[] classData = readCurrentJarEntry(jarInputStream);
                addClass(className, classData);
            } else if (isJarFile) {
                byte[] jarData = readCurrentJarEntry(jarInputStream);
                ByteArrayInputStream byteStream = new ByteArrayInputStream(jarData);
                JarInputStream jarStream = new JarInputStream(byteStream);
                parseJar(jarStream);
            } else if (isFile) {
                byte[] fileData = readCurrentJarEntry(jarInputStream);
                addResource(name, fileData);
            }
            entry = jarInputStream.getNextJarEntry();
        }
        jarInputStream.close();
    }

    public void addJar(JarInputStream jarInputStream) throws IOException {
        parseJar(jarInputStream);
    }

    public void addClass(String name, byte[] data) {
        log.debug("adding class \"{}\"", name);
        classes.putIfAbsent(name, data);
    }

    public void addResource(String name, byte[] data) {
        log.debug("adding resource \"{}\"", name);
        try {
            URL url = new URL("inputstream", "", 0, name,
                new InputStreamURLStreamHandler(new ByteArrayInputStream(data))) ;
            resources.put(name, url);
        } catch (MalformedURLException e) {
            log.error("add resource error", e);
        }
    }

    @Override
    protected URL findResource(String name) {
        return resources.get(name);
    }

    @Override
    protected Enumeration<URL> findResources(String name) throws IOException {
        return Collections.enumeration(Collections.singleton(findResource(name)));
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        if (classes.containsKey(name)) {
            byte[] bytes = classes.get(name);
            return defineClass(name, bytes, 0, bytes.length);
        } else throw new ClassNotFoundException();
    }

    // ----------------------------------------------------------------------

    private static String pathToClassName(String path) {
        return path.substring(0, path.length() - 6).replace("/", ".");
    }

    private static byte[] readCurrentJarEntry(JarInputStream jarInStream)
        throws IOException {
        // read the whole contents of the
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[2048];
        int len;
        while ((len = jarInStream.read(buffer)) > 0) {
            out.write(buffer, 0, len);
        }
        return out.toByteArray();
    }

    private static class InputStreamURLStreamHandler extends URLStreamHandler {

        InputStream inputStream;
        public InputStreamURLStreamHandler(InputStream inputStream) {
            this.inputStream = inputStream;
        }

        @Override
        protected URLConnection openConnection(URL u) throws IOException {
            return new InputStreamURLConnection(u, inputStream);
        }

        private class InputStreamURLConnection extends URLConnection {
            private InputStream inStream;
            public InputStreamURLConnection(URL url,InputStream inStream) {
                super(url);
                this.inStream = inStream;
            }

            @Override
            public InputStream getInputStream() {
                return inStream;
            }

            @Override
            public void connect() throws IOException {
            }
        }
    }
}
