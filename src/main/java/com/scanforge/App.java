package com.scanforge;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class App {
  public static void main(String[] args) {
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
  }
}
