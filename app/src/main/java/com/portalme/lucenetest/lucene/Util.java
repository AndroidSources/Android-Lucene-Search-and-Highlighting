package com.portalme.lucenetest.lucene;



import java.io.IOException;
import org.lukhnos.portmobile.file.FileVisitResult;
import org.lukhnos.portmobile.file.Files;
import org.lukhnos.portmobile.file.Path;
import org.lukhnos.portmobile.file.SimpleFileVisitor;
import org.lukhnos.portmobile.file.attribute.BasicFileAttributes;

public class Util {
  public static void deletePath(Path path) throws IOException {
    Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
      @Override
      public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        Files.delete(file);
        return FileVisitResult.CONTINUE;
      }

      @Override
      public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        Files.delete(dir);
        return FileVisitResult.CONTINUE;
      }
    });
  }
}
