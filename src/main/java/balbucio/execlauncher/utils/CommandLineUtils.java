package balbucio.execlauncher.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class CommandLineUtils {

    private CommandLineUtils() {
    }

    public static List<String> parse(String command) {
        List<String> tokens = new ArrayList<>();
        if (command == null || command.isBlank()) return tokens;

        int i = 0;
        int n = command.length();
        while (i < n) {
            char c = command.charAt(i);
            if (Character.isWhitespace(c)) {
                i++;
                continue;
            }
            StringBuilder sb = new StringBuilder();
            if (c == '"' || c == '\'') {
                char quote = c;
                i++;
                while (i < n && command.charAt(i) != quote) {
                    sb.append(command.charAt(i));
                    i++;
                }
                i++;
            } else {
                while (i < n && !Character.isWhitespace(command.charAt(i))) {
                    sb.append(command.charAt(i));
                    i++;
                }
            }
            tokens.add(sb.toString());
        }
        return tokens;
    }

    public static String joinQuoted(List<String> tokens) {
        return tokens.stream()
                .map(t -> t.chars().anyMatch(Character::isWhitespace) ? "\"" + t + "\"" : t)
                .collect(Collectors.joining(" "));
    }

    public static String replaceProgram(String command, String newProgram) {
        if (command == null || command.isBlank()) return command;
        String trimmed = command.trim();
        if (trimmed.startsWith("\"")) {
            int end = trimmed.indexOf('"', 1);
            if (end > 0) {
                return "\"" + newProgram + "\"" + trimmed.substring(end + 1);
            }
        }
        int firstSpace = trimmed.indexOf(' ');
        if (firstSpace < 0) {
            return "\"" + newProgram + "\"";
        }
        return "\"" + newProgram + "\"" + trimmed.substring(firstSpace);
    }
}