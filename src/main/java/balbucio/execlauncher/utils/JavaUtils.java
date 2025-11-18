package balbucio.execlauncher.utils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class JavaUtils {

    public static File JDK_PATH = new File(System.getProperty("user.home"), ".jdks");

    public static Map<String, String> getJavaAvailable() {
        Map<String, String> result = new HashMap<>();

        for (File file : Objects.requireNonNull(JDK_PATH.listFiles())) {
            if (file.isDirectory()) {
                result.put(file.getName(), file.getAbsolutePath());
            }
        }

        return result;
    }
}
