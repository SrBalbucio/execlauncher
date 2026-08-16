package balbucio.execlauncher.utils;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MapUtilsTest {

    @Test
    void roundTrips() {
        Map<String, String> map = Map.of("KEY", "value", "PORT", "8080");
        Map<String, String> result = MapUtils.array2dToMap(MapUtils.mapToArray2d(map));
        assertEquals(map, result);
    }

    @Test
    void handlesEmptyMap() {
        assertEquals(0, MapUtils.mapToArray2d(Map.of()).length);
        assertTrue(MapUtils.array2dToMap(new String[0][0]).isEmpty());
    }
}