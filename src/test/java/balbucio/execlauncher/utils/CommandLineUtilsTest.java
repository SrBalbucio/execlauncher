package balbucio.execlauncher.utils;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CommandLineUtilsTest {

    @Test
    void parsesQuotedArguments() {
        List<String> tokens = CommandLineUtils.parse("\"C:\\Program Files\\Java\\java.exe\" -jar \"C:\\path with space\\app.jar\" --port=8080");
        assertEquals(List.of(
                "C:\\Program Files\\Java\\java.exe",
                "-jar",
                "C:\\path with space\\app.jar",
                "--port=8080"), tokens);
    }

    @Test
    void parsesSingleQuotedArguments() {
        List<String> tokens = CommandLineUtils.parse("'C:/Program Files/java' run dev");
        assertEquals(List.of("C:/Program Files/java", "run", "dev"), tokens);
    }

    @Test
    void parsesNullAndBlank() {
        assertEquals(List.of(), CommandLineUtils.parse(null));
        assertEquals(List.of(), CommandLineUtils.parse("   "));
    }

    @Test
    void joinQuotedRoundTrips() {
        List<String> tokens = List.of("C:\\Program Files\\Java\\java.exe", "-jar", "target/app.jar");
        String joined = CommandLineUtils.joinQuoted(tokens);
        assertEquals(tokens, CommandLineUtils.parse(joined));
    }

    @Test
    void replaceProgramWithQuotedPath() {
        String cmd = CommandLineUtils.replaceProgram("\"C:\\old\\java.exe\" -jar \"app.jar\"", "C:\\new\\java.exe");
        assertEquals("\"C:\\new\\java.exe\" -jar \"app.jar\"", cmd);
    }

    @Test
    void replaceProgramWithUnquotedPath() {
        String cmd = CommandLineUtils.replaceProgram("C:\\old\\java.exe -jar app.jar", "C:\\new\\java.exe");
        assertEquals("\"C:\\new\\java.exe\" -jar app.jar", cmd);
    }

    @Test
    void replaceProgramKeepsSingleToken() {
        String cmd = CommandLineUtils.replaceProgram("C:\\old\\java.exe", "C:\\new\\java.exe");
        assertEquals("\"C:\\new\\java.exe\"", cmd);
    }
}