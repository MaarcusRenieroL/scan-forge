package com.scanforge;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;

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
      if (!ext.startsWith(".")) ext = "." + ext;
      requiredExtensions.add(ext);
    }

    Set<String> ignoredExtensions = new HashSet<>();
    Set<String> ignoredFolders = new HashSet<>();
    Set<String> ignoredNames = new HashSet<>();

    Path gitignorePath = path.resolve(".gitignore");

    if (Files.exists(gitignorePath)) {
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
    }

    Process process = new ProcessBuilder("pbcopy").start();
    OutputStream outputStream = process.getOutputStream();

    Files.walk(path)
        .filter(Files::isRegularFile)
        .forEach(
            p -> {
              String fileName = p.getFileName().toString();

              for (String folder : ignoredFolders) {
                if (p.toString().contains("/" + folder + "/")) return;
              }

              if (ignoredNames.contains(fileName)) return;

              int index = fileName.lastIndexOf('.');
              if (index == -1 || index == 0) return;

              String ext = fileName.substring(index).toLowerCase();

              if (ignoredExtensions.contains(ext)) return;

              if (!requiredExtensions.contains(ext)) return;

              String header = "\n===== file: " + p.toAbsolutePath() + " =====\n";

              try {
                outputStream.write(header.getBytes(StandardCharsets.UTF_8));

                try (Stream<String> lines = Files.lines(p)) {
                  lines.forEach(
                      line -> {
                        try {
                          outputStream.write((line + "\n").getBytes(StandardCharsets.UTF_8));
                        } catch (Exception ignored) {
                        }
                      });
                }

              } catch (Exception ignored) {
              }
            });

    outputStream.close();
    process.waitFor();
  }
}
