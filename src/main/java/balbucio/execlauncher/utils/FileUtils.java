package balbucio.execlauncher.utils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class FileUtils {

    public static String getStringFromFile(File file) {
        try (Scanner scanner = new Scanner(file)) {
            StringBuilder builder = new StringBuilder();
            while (scanner.hasNextLine()) {
                builder.append(scanner.nextLine()).append("\n");
            }
            return builder.toString();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public static Map<String, String> readVars(File file) {
        Map<String, String> map = new HashMap<>();
        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (line.isEmpty() || line.startsWith("#")) continue;

                if (line.startsWith("export")) {
                    line = line.substring("export".length()).trim();
                }

                int idx = line.indexOf('=');
                if (idx <= 0) continue;

                String key = line.substring(0, idx).trim();
                String value = line.substring(idx + 1).trim();
                map.put(key, stripQuotes(value));
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        return map;
    }

    private static String stripQuotes(String value) {
        if (value.length() >= 2) {
            char first = value.charAt(0);
            char last = value.charAt(value.length() - 1);
            if ((first == '"' && last == '"') || (first == '\'' && last == '\'')) {
                return value.substring(1, value.length() - 1);
            }
        }
        return value;
    }

    public static File findOnPath(String name) {
        String path = System.getenv("PATH");
        if (path == null || path.isBlank()) return null;

        boolean windows = JavaUtils.isWindows();
        String[] suffixes = windows ? new String[]{".exe", ".cmd", ".bat", ""} : new String[]{""};

        for (String dir : path.split(File.pathSeparator)) {
            if (dir == null || dir.isBlank()) continue;
            for (String suffix : suffixes) {
                File file = new File(dir, name + suffix);
                if (file.isFile()) return file;
            }
        }
        return null;
    }
}