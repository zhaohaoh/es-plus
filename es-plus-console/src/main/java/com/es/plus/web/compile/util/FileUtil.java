package com.es.plus.web.compile.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

public class FileUtil {
    public static boolean del(File file)   {
        if (file != null && file.exists()) {
            if (file.isDirectory()) {
                boolean isOk = clean(file);
                if (!isOk) {
                    return false;
                }
            }
            
            Path path = file.toPath();
            
            try {
                delFile(path);
            } catch (DirectoryNotEmptyException var3) {
                del((Path)path);
            } catch (IOException var4) {
                throw new  RuntimeException(var4);
            }
            
            return true;
        } else {
            return true;
        }
    }
    public static boolean clean(File directory)  {
        if (directory != null && directory.exists() && directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (null != files) {
                File[] var2 = files;
                int var3 = files.length;
                
                for(int var4 = 0; var4 < var3; ++var4) {
                    File childFile = var2[var4];
                    if (!del(childFile)) {
                        return false;
                    }
                }
            }
            
            return true;
        } else {
            return true;
        }
    }
    protected static void delFile(Path path) throws IOException {
        try {
            Files.delete(path);
        } catch (AccessDeniedException var2) {
            if (!path.toFile().delete()) {
                throw var2;
            }
        }
        
    }
    
    
    public static boolean isFile(File file) {
        return null != file && file.isFile();
    }
    public static String getCanonicalPath(File file) {
        if (null == file) {
            return null;
        } else {
            try {
                return file.getCanonicalPath();
            } catch (IOException var2) {
                throw new  RuntimeException(var2);
            }
        }
    }
 
    
    public static boolean del(Path path)   {
        if (Files.notExists(path, new LinkOption[0])) {
            return true;
        } else {
            try {
                if (isDirectory(path)) {
                    Files.walkFileTree(path, DelVisitor.INSTANCE);
                } else {
                    delFile(path);
                }
                
                return true;
            } catch (IOException var2) {
                throw new  RuntimeException(var2);
            }
        }
    }
    
    
    
    public static boolean isDirectory(Path path) {
        return isDirectory(path, false);
    }
    
    public static boolean isDirectory(Path path, boolean isFollowLinks) {
        return null == path ? false : Files.isDirectory(path, getLinkOptions(isFollowLinks));
    }
    
    public static LinkOption[] getLinkOptions(boolean isFollowLinks) {
        return isFollowLinks ? new LinkOption[0] : new LinkOption[]{LinkOption.NOFOLLOW_LINKS};
    }
    
    
    
    public static class DelVisitor extends SimpleFileVisitor<Path> {
        public static DelVisitor INSTANCE = new DelVisitor();
        
        public DelVisitor() {
        }
        
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            Files.delete(file);
            return FileVisitResult.CONTINUE;
        }
        
        public FileVisitResult postVisitDirectory(Path dir, IOException e) throws IOException {
            if (e == null) {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            } else {
                throw e;
            }
        }
    }
    
}
