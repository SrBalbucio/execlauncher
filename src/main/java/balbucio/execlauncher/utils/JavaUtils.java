package balbucio.execlauncher.utils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class JavaUtils {

    public static File JDK_PATH = new File(System.getProperty("user.home"), ".jdks");

    public static Map<String, String> getJavaAvailable() {
        Map<String, String> result = new HashMap<>();

        File[] files = JDK_PATH.listFiles();
        if (files == null) return result;

        for (File file : files) {
            if (file.isDirectory()) {
                result.put(file.getName(), file.getAbsolutePath());
            }
        }

        return result;
    }

    public static boolean isWindows() {
        return System.getProperty("os.name", "").toLowerCase().contains("win");
    }

    public static String javaExecutableName() {
        return isWindows() ? "java.exe" : "java";
    }
}