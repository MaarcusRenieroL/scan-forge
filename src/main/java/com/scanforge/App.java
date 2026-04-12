package com.scanforge;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class App {
  public static void main(String[] args) throws Exception, IOException {

    if (args.length < 2) {
      System.out.println("Usage: scanforge <path> <extensions...>");
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

    String[] requiredFileExtensions = Arrays.copyOfRange(args, 1, args.length);

    Set<String> requiredExtensions = new HashSet<>();
    for (String ext : requiredFileExtensions) {
      ext = ext.toLowerCase();
      if (!ext.startsWith(".")) {
        ext = "." + ext;
      }
      requiredExtensions.add(ext);
    }

    Files.walk(path)
        .filter(Files::isRegularFile)
        .forEach(
            p -> {
              String fileName = p.getFileName().toString();
              int index = fileName.lastIndexOf('.');

              if (index == -1 || index == 0) return;

              String ext = fileName.substring(index).toLowerCase();

              if (requiredExtensions.contains(ext)) {
                System.out.println("\n===== file: " + p.toAbsolutePath() + " =====");

                try {
                  Files.lines(p).forEach(System.out::println);
                } catch (Exception e) {
                  System.out.println("Error reading file: " + p.toAbsolutePath());
                }
              }
            });
  }
}
