package com.scanforge;

import java.nio.file.Path;
import java.util.Set;

public class SelectionResult {
  public Set<String> extensions;
  public Set<Path> files;

  public SelectionResult(Set<String> extensions, Set<Path> files) {
    this.extensions = extensions;
    this.files = files;
  }
}
