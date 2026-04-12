package com.scanforge;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;

public class App {

  public static void main(String[] args) {
    try {
      Path path;
      List<String> extArgs = new ArrayList<>();

      if (args.length == 0) {
        path = Paths.get(".");
      } else {
        Path possiblePath = Paths.get(args[0]);
        if (Files.exists(possiblePath)) {
          path = possiblePath;
          extArgs = Arrays.asList(Arrays.copyOfRange(args, 1, args.length));
        } else {
          path = Paths.get(".");
          extArgs = Arrays.asList(args);
        }
      }

      validatePath(path);

      List<Path> allFiles = collectFiles(path);
      Set<String> existingExtensions = collectExtensions(allFiles);

      SelectionResult result;

      if (extArgs.isEmpty()) {
        result = SelectorUI.select(existingExtensions, allFiles);
      } else {
        Set<String> normalized = normalizeExtensions(extArgs);
        result = new SelectionResult(normalized, new HashSet<>());
      }

      Set<String> ignoredExtensions = new HashSet<>();
      Set<String> ignoredFolders = new HashSet<>();
      Set<String> ignoredNames = new HashSet<>();

      loadGitignore(path, ignoredExtensions, ignoredFolders, ignoredNames);

      Set<Path> finalFiles = resolveFinalFiles(allFiles, result);

      run(finalFiles, ignoredExtensions, ignoredFolders, ignoredNames);

    } catch (Exception e) {
      logError(e);
    }
  }

  private static List<Path> collectFiles(Path path) throws IOException {
    List<Path> files = new ArrayList<>();

    try (Stream<Path> paths = Files.walk(path)) {
      paths.filter(Files::isRegularFile).forEach(files::add);
    }

    return files;
  }

  private static Set<String> collectExtensions(List<Path> files) {
    Set<String> extensions = new HashSet<>();

    for (Path p : files) {
      String name = p.getFileName().toString();
      int i = name.lastIndexOf('.');
      if (i > 0) {
        extensions.add(name.substring(i).toLowerCase());
      }
    }

    return extensions;
  }

  private static Set<Path> resolveFinalFiles(List<Path> allFiles, SelectionResult result) {
    Set<Path> finalFiles = new HashSet<>();

    for (Path p : allFiles) {
      String name = p.getFileName().toString();
      int i = name.lastIndexOf('.');
      if (i > 0) {
        String ext = name.substring(i).toLowerCase();
        if (result.extensions.contains(ext)) {
          finalFiles.add(p);
        }
      }
    }

    finalFiles.addAll(result.files);
    return finalFiles;
  }

  private static void run(
      Set<Path> files,
      Set<String> ignoredExtensions,
      Set<String> ignoredFolders,
      Set<String> ignoredNames)
      throws Exception {

    Process process = new ProcessBuilder("pbcopy").start();

    try (OutputStream os = process.getOutputStream()) {
      for (Path p : files) {
        processFile(p, ignoredExtensions, ignoredFolders, ignoredNames, os);
      }
    }

    process.waitFor();
  }

  private static void processFile(
      Path p,
      Set<String> ignoredExtensions,
      Set<String> ignoredFolders,
      Set<String> ignoredNames,
      OutputStream os) {

    try {
      String name = p.getFileName().toString();

      if (isIgnored(p, name, ignoredExtensions, ignoredFolders, ignoredNames)) return;
      if (isBinaryFile(p)) return;

      writeFile(p, os);

    } catch (Exception e) {
      logError(e);
    }
  }

  private static boolean isIgnored(
      Path p,
      String name,
      Set<String> ignoredExtensions,
      Set<String> ignoredFolders,
      Set<String> ignoredNames) {

    for (Path part : p) {
      String segment = part.toString();
      if (ignoredFolders.contains(segment)) return true;
      if (ignoredNames.contains(segment)) return true;
    }

    int i = name.lastIndexOf('.');
    if (i > 0) {
      String ext = name.substring(i).toLowerCase();
      if (ignoredExtensions.contains(ext)) return true;
    }

    return false;
  }

  private static void writeFile(Path p, OutputStream os) throws IOException {
    String header = "\n===== file: " + p.toAbsolutePath() + " =====\n";
    os.write(header.getBytes(StandardCharsets.UTF_8));

    try (Stream<String> lines = Files.lines(p)) {
      for (String line : (Iterable<String>) lines::iterator) {
        os.write((line + "\n").getBytes(StandardCharsets.UTF_8));
      }
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

  private static void loadGitignore(
      Path path,
      Set<String> ignoredExtensions,
      Set<String> ignoredFolders,
      Set<String> ignoredNames) {

    Path gitignore = path.resolve(".gitignore");
    if (!Files.exists(gitignore)) return;

    try {
      for (String line : Files.readAllLines(gitignore)) {
        line = line.trim();
        if (line.isEmpty() || line.startsWith("#")) continue;

        if (line.startsWith("*.")) {
          ignoredExtensions.add(line.substring(1).toLowerCase());
        } else if (line.endsWith("/")) {
          ignoredFolders.add(line.substring(0, line.length() - 1));
        } else if (!line.contains(".")) {
          ignoredFolders.add(line);
        } else {
          ignoredNames.add(line);
        }
      }
    } catch (IOException e) {
      logError(e);
    }
  }

  private static void validatePath(Path path) {
    if (!Files.exists(path) || !Files.isDirectory(path)) {
      throw new IllegalArgumentException("Invalid directory path");
    }
  }

  private static Set<String> normalizeExtensions(List<String> args) {
    Set<String> set = new HashSet<>();
    for (String ext : args) {
      ext = ext.toLowerCase();
      if (!ext.startsWith(".")) ext = "." + ext;
      set.add(ext);
    }
    return set;
  }

  private static void logError(Exception e) {
    System.out.println(e.getClass().getSimpleName() + ": " + e.getMessage());
  }
}
