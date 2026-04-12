package com.scanforge;

import com.googlecode.lanterna.SGR;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;
import java.util.*;

public class ExtensionSelector {

  public static Set<String> select(Set<String> extensions) throws Exception {

    Terminal terminal = new DefaultTerminalFactory().createTerminal();
    Screen screen = new TerminalScreen(terminal);

    try {
      screen.startScreen();

      List<String> all = new ArrayList<>(extensions);
      Collections.sort(all);

      List<String> filtered = new ArrayList<>(all);
      Set<String> selected = new HashSet<>();

      int cursor = 0;
      StringBuilder search = new StringBuilder();

      while (true) {
        screen.clear();

        TextGraphics tg = screen.newTextGraphics();

        tg.putString(2, 1, "Search: " + search);

        for (int i = 0; i < filtered.size(); i++) {
          String ext = filtered.get(i);

          String prefix = selected.contains(ext) ? "[✔] " : "[ ] ";
          String line = prefix + ext;

          if (i == cursor) {
            tg.enableModifiers(SGR.REVERSE);
            tg.putString(2, i + 3, line);
            tg.disableModifiers(SGR.REVERSE);
          } else {
            tg.putString(2, i + 3, line);
          }
        }

        tg.putString(2, filtered.size() + 5, "↑ ↓ navigate | SPACE select | ENTER confirm");

        screen.refresh();

        KeyStroke key = screen.readInput();

        switch (key.getKeyType()) {
          case ArrowUp:
            cursor = Math.max(0, cursor - 1);
            break;

          case ArrowDown:
            cursor = Math.min(filtered.size() - 1, cursor + 1);
            break;

          case Enter:
            return selected;

          case Character:
            char c = key.getCharacter();

            if (c == ' ') {
              if (!filtered.isEmpty()) {
                String ext = filtered.get(cursor);
                if (selected.contains(ext)) selected.remove(ext);
                else selected.add(ext);
              }
            } else {
              search.append(c);
              filtered = filter(all, search.toString());
              cursor = 0;
            }
            break;

          case Backspace:
            if (search.length() > 0) {
              search.deleteCharAt(search.length() - 1);
              filtered = filter(all, search.toString());
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

  private static List<String> filter(List<String> all, String query) {
    List<String> result = new ArrayList<>();
    for (String ext : all) {
      if (ext.contains(query.toLowerCase())) {
        result.add(ext);
      }
    }
    return result;
  }
}
