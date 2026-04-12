package com.scanforge;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;

public class App {

  public static void main(String[] args) {

    try {
      validateArgs(args);

      Path path = Paths.get(args[0]);
      validatePath(path);

      Set<String> requiredExtensions = normalizeExtensions(args);

      Set<String> ignoredExtensions = new HashSet<>();
      Set<String> ignoredFolders = new HashSet<>();
      Set<String> ignoredNames = new HashSet<>();

      loadGitignore(path, ignoredExtensions, ignoredFolders, ignoredNames);

      run(path, requiredExtensions, ignoredExtensions, ignoredFolders, ignoredNames);

    } catch (IllegalArgumentException e) {
      logError(e);
    } catch (Exception e) {
      logError(e);
    }
  }

  private static void run(
      Path path,
      Set<String> requiredExtensions,
      Set<String> ignoredExtensions,
      Set<String> ignoredFolders,
      Set<String> ignoredNames) {

    try {
      Process process = new ProcessBuilder("pbcopy").start();

      try (OutputStream outputStream = process.getOutputStream();
          Stream<Path> paths = Files.walk(path)) {

        paths
            .filter(Files::isRegularFile)
            .forEach(
                p ->
                    processFile(
                        p,
                        requiredExtensions,
                        ignoredExtensions,
                        ignoredFolders,
                        ignoredNames,
                        outputStream));
      }

      process.waitFor();

    } catch (IOException e) {
      logError(e);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      logError(e);
    }
  }

  private static void validateArgs(String[] args) {
    if (args.length < 2) {
      throw new IllegalArgumentException("Usage: scanforge <path> <extensions...>");
    }
  }

  private static void validatePath(Path path) {
    if (!Files.exists(path) || !Files.isDirectory(path)) {
      throw new IllegalArgumentException("Invalid directory path");
    }
  }

  private static Set<String> normalizeExtensions(String[] args) {
    Set<String> extensions = new HashSet<>();
    for (int i = 1; i < args.length; i++) {
      String ext = args[i].toLowerCase();
      if (!ext.startsWith(".")) ext = "." + ext;
      extensions.add(ext);
    }
    return extensions;
  }

  private static void loadGitignore(
      Path path,
      Set<String> ignoredExtensions,
      Set<String> ignoredFolders,
      Set<String> ignoredNames) {

    Path gitignorePath = path.resolve(".gitignore");

    if (!Files.exists(gitignorePath)) return;

    try {
      List<String> lines = Files.readAllLines(gitignorePath);

      for (String line : lines) {
        line = line.trim();

        if (line.isEmpty() || line.startsWith("#")) continue;

        if (line.startsWith("*.")) {
          ignoredExtensions.add(line.substring(1).toLowerCase());
        } else if (line.endsWith("/")) {
          ignoredFolders.add(line.substring(0, line.length() - 1));
        } else {
          ignoredNames.add(line);
        }
      }

    } catch (IOException e) {
      logError(e);
    }
  }

  private static void processFile(
      Path p,
      Set<String> requiredExtensions,
      Set<String> ignoredExtensions,
      Set<String> ignoredFolders,
      Set<String> ignoredNames,
      OutputStream outputStream) {

    try {
      String fileName = p.getFileName().toString();

      if (isIgnored(p, fileName, ignoredExtensions, ignoredFolders, ignoredNames)) return;

      int index = fileName.lastIndexOf('.');
      if (index == -1 || index == 0) return;

      String ext = fileName.substring(index).toLowerCase();

      if (!requiredExtensions.contains(ext)) return;
      if (isBinaryFile(p)) return;

      writeFile(p, outputStream);

    } catch (Exception e) {
      logError(e);
    }
  }

  private static boolean isIgnored(
      Path p,
      String fileName,
      Set<String> ignoredExtensions,
      Set<String> ignoredFolders,
      Set<String> ignoredNames) {

    for (String folder : ignoredFolders) {
      if (p.toString().contains("/" + folder + "/")) return true;
    }

    if (ignoredNames.contains(fileName)) return true;

    int index = fileName.lastIndexOf('.');
    if (index != -1 && index != 0) {
      String ext = fileName.substring(index).toLowerCase();
      if (ignoredExtensions.contains(ext)) return true;
    }

    return false;
  }

  private static void writeFile(Path p, OutputStream outputStream) {
    String header = "\n===== file: " + p.toAbsolutePath() + " =====\n";

    try {
      outputStream.write(header.getBytes(StandardCharsets.UTF_8));

      try (Stream<String> lines = Files.lines(p)) {
        lines.forEach(
            line -> {
              try {
                outputStream.write((line + "\n").getBytes(StandardCharsets.UTF_8));
              } catch (IOException e) {
                logError(e);
              }
            });
      }

    } catch (IOException e) {
      logError(e);
    }
  }

  private static boolean isBinaryFile(Path path) {
    try {
      byte[] bytes = Files.readAllBytes(path);
      int limit = Math.min(bytes.length, 1000);

      for (int i = 0; i < limit; i++) {
        if (bytes[i] == 0) return true;
      }
    } catch (IOException e) {
      return true;
    }
    return false;
  }

  private static void logError(Exception e) {
    System.out.println(e.getClass().getSimpleName() + ": " + e.getMessage());
  }
}
