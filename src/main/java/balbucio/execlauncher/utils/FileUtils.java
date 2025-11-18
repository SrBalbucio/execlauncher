package balbucio.execlauncher.utils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class FileUtils {

    public static String getStringFromFile(File file) {
        try {
            StringBuilder builder = new StringBuilder();
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                builder.append(scanner.nextLine()).append("\n");
            }
            scanner.close();
            return builder.toString();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public static Map<String, String> readVars(File file) {
        try {
            Map<String, String> map = new HashMap<>();
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                System.out.println(line);
                String[] var = line.split("=");
                map.put(var[0], var[1]);
            }
            scanner.close();
            return map;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
