package com.scanforge;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class App {
  public static void main(String[] args) throws Exception {

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

    System.out.println("Required extensions:");
    requiredExtensions.forEach(System.out::println);
  }
}
