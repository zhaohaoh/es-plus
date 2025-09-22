package com.es.plus.web.compile.core;

import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author mahuibo
 * @Title: MemoryJavaFileManager
 * @email mhb0409@qq.com
 * @time 2025/2/21
 */
public class MemoryJavaFileManager extends ForwardingJavaFileManager<JavaFileManager> {
    private final Map<String, ByteArrayOutputStream> classBytes = new HashMap<>();

    public MemoryJavaFileManager(JavaFileManager fileManager) {
        super(fileManager);
    }

    @Override
    public JavaFileObject getJavaFileForOutput(Location location,
                                               String className,
                                               JavaFileObject.Kind kind,
                                               FileObject sibling) throws IOException {
        if (kind == JavaFileObject.Kind.CLASS) {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            classBytes.put(className, outputStream);
            return new SimpleJavaFileObject(URI.create("mem:///" + className), kind) {
                @Override
                public OutputStream openOutputStream() {
                    return outputStream;
                }
            };
        }
        return super.getJavaFileForOutput(location, className, kind, sibling);
    }

    @Override
    public Iterable<JavaFileObject> list(Location location, String packageName, Set<JavaFileObject.Kind> kinds, boolean recurse) throws IOException {
        return super.list(location, packageName, kinds, recurse);
    }

    public Map<String, byte[]> getClassBytes() {
        Map<String, byte[]> result = new HashMap<>();
        for (Map.Entry<String, ByteArrayOutputStream> entry : classBytes.entrySet()) {
            result.put(entry.getKey(), entry.getValue().toByteArray());
        }
        return result;
    }
}