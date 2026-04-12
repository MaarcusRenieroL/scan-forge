package com.scanforge;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class App {
  public static void main(String[] args) throws Exception {
    if (args.length == 0) {
      System.out.println("Usage: scanforge <path>");
      return;
    }

    String inputPath = args[0];

    if (inputPath.isBlank()) {
      System.out.println("Path cannot be empty");
      return;
    }

    Path path = Paths.get(inputPath);

    if (!Files.exists(path) || !Files.isDirectory(path)) {
      System.out.println("Invalid directory path");
      return;
    }

    Files.walk(path)
        .filter(Files::isRegularFile)
        .forEach(p -> System.out.println(p.toAbsolutePath()));
    Set<String> existingFileExtensions = new HashSet<>();

    Files.walk(path)
        .filter(Files::isRegularFile)
        .forEach(
            p -> {
              String fileName = p.getFileName().toString();
              int index = fileName.lastIndexOf('.');

              if (index == -1 || index == 0) return;

              String ext = fileName.substring(index).toLowerCase();
              existingFileExtensions.add(ext);
            });

    existingFileExtensions.forEach(System.out::println);
  }
}
