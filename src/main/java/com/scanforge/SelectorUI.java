package com.scanforge;

import com.googlecode.lanterna.SGR;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;
import java.nio.file.Path;
import java.util.*;

public class SelectorUI {

  enum Mode {
    EXTENSION,
    FILE
  }

  public static SelectionResult select(Set<String> extensions, List<Path> files) throws Exception {

    Terminal terminal = new DefaultTerminalFactory().createTerminal();
    Screen screen = new TerminalScreen(terminal);

    try {
      screen.startScreen();

      List<String> allExt = new ArrayList<>(extensions);
      Collections.sort(allExt);

      List<Path> allFiles = new ArrayList<>(files);

      List<String> filteredExt = new ArrayList<>(allExt);
      List<Path> filteredFiles = new ArrayList<>(allFiles);

      Set<String> selectedExt = new HashSet<>();
      Set<Path> selectedFiles = new HashSet<>();

      Mode mode = Mode.EXTENSION;
      int cursor = 0;
      StringBuilder search = new StringBuilder();

      while (true) {
        screen.clear();
        TextGraphics tg = screen.newTextGraphics();

        tg.putString(2, 1, "Mode: " + mode + " (TAB to switch)");
        tg.putString(2, 2, "Search: " + search);

        if (mode == Mode.EXTENSION) {
          for (int i = 0; i < filteredExt.size(); i++) {
            String ext = filteredExt.get(i);
            String prefix = selectedExt.contains(ext) ? "[✔] " : "[ ] ";
            draw(tg, ext, prefix, i, cursor);
          }
        } else {
          for (int i = 0; i < filteredFiles.size(); i++) {
            Path p = filteredFiles.get(i);
            String name = p.toString();
            String prefix = selectedFiles.contains(p) ? "[✔] " : "[ ] ";
            draw(tg, name, prefix, i, cursor);
          }
        }

        tg.putString(2, 30, "↑ ↓ navigate | SPACE select | TAB switch | ENTER confirm");

        screen.refresh();

        KeyStroke key = screen.readInput();

        switch (key.getKeyType()) {
          case ArrowUp:
            cursor = Math.max(0, cursor - 1);
            break;

          case ArrowDown:
            int size = (mode == Mode.EXTENSION) ? filteredExt.size() : filteredFiles.size();
            cursor = Math.min(size - 1, cursor + 1);
            break;

          case Tab:
            mode = (mode == Mode.EXTENSION) ? Mode.FILE : Mode.EXTENSION;
            cursor = 0;
            search.setLength(0);
            break;

          case Enter:
            return new SelectionResult(selectedExt, selectedFiles);

          case Character:
            char c = key.getCharacter();

            if (c == ' ') {
              toggleSelection(mode, filteredExt, filteredFiles, selectedExt, selectedFiles, cursor);
            } else {
              search.append(c);
              filteredExt = filterExt(allExt, search.toString());
              filteredFiles = filterFiles(allFiles, search.toString());
              cursor = 0;
            }
            break;

          case Backspace:
            if (search.length() > 0) {
              search.deleteCharAt(search.length() - 1);
              filteredExt = filterExt(allExt, search.toString());
              filteredFiles = filterFiles(allFiles, search.toString());
              cursor = 0;
            }
            break;

          default:
            break;
        }
      }

    } finally {
      screen.stopScreen();
    }
  }

  private static void draw(TextGraphics tg, String text, String prefix, int i, int cursor) {
    if (i == cursor) {
      tg.enableModifiers(SGR.REVERSE);
      tg.putString(2, i + 4, prefix + text);
      tg.disableModifiers(SGR.REVERSE);
    } else {
      tg.putString(2, i + 4, prefix + text);
    }
  }

  private static void toggleSelection(
      Mode mode,
      List<String> ext,
      List<Path> files,
      Set<String> selectedExt,
      Set<Path> selectedFiles,
      int cursor) {

    if (mode == Mode.EXTENSION && !ext.isEmpty()) {
      String e = ext.get(cursor);
      if (selectedExt.contains(e)) selectedExt.remove(e);
      else selectedExt.add(e);
    }

    if (mode == Mode.FILE && !files.isEmpty()) {
      Path p = files.get(cursor);
      if (selectedFiles.contains(p)) selectedFiles.remove(p);
      else selectedFiles.add(p);
    }
  }

  private static List<String> filterExt(List<String> all, String q) {
    q = q.toLowerCase();
    List<String> out = new ArrayList<>();
    for (String s : all) {
      if (s.contains(q) || fuzzy(s, q)) out.add(s);
    }
    return out;
  }

  private static List<Path> filterFiles(List<Path> all, String q) {
    q = q.toLowerCase();
    List<Path> out = new ArrayList<>();
    for (Path p : all) {
      if (p.toString().toLowerCase().contains(q) || fuzzy(p.toString(), q)) out.add(p);
    }
    return out;
  }

  private static boolean fuzzy(String text, String q) {
    int ti = 0, qi = 0;
    text = text.toLowerCase();

    while (ti < text.length() && qi < q.length()) {
      if (text.charAt(ti) == q.charAt(qi)) qi++;
      ti++;
    }

    return qi == q.length();
  }
}
