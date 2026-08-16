package balbucio.execlauncher.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class FileUtilsTest {

    @TempDir
    Path tempDir;

    @Test
    void readsEnvFile() throws IOException {
        File envFile = tempDir.resolve(".env").toFile();
        Files.writeString(envFile.toPath(),
                "# comment\n"
                        + "KEY=value\n"
                        + "QUOTED=\"hello world\"\n"
                        + "SINGLE='single value'\n"
                        + "EXPORTED=export VALUE\n"
                        + "BADLINE\n"
                        + "EMPTY=\n"
                        + "  SPACED = padded  \n");

        Map<String, String> vars = FileUtils.readVars(envFile);

        assertEquals("value", vars.get("KEY"));
        assertEquals("hello world", vars.get("QUOTED"));
        assertEquals("single value", vars.get("SINGLE"));
        assertEquals("export VALUE", vars.get("EXPORTED"));
        assertEquals("", vars.get("EMPTY"));
        assertEquals("padded", vars.get("SPACED"));
        assertNull(vars.get("BADLINE"));
    }
}