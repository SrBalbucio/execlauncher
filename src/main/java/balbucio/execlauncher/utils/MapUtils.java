package balbucio.execlauncher.utils;

import java.util.HashMap;
import java.util.Map;

public class MapUtils {

    public static String[][] mapToArray2d(Map<String, String> map) {
        String[][] array = new String[map.size()][];

        int i = 0;
        for (Map.Entry<String, String> entry : map.entrySet()) {
            array[i] = new String[]{entry.getKey(), entry.getValue()};
        }

        return array;
    }

    public static Map<String, String> array2dToMap(String[][] array) {
        Map<String, String> map = new HashMap<>();

        for (String[] strings : array) {
            map.put(strings[0], strings[1]);
        }

        return map;
    }
}
