package balbucio.execlauncher;

import balbucio.execlauncher.model.CmdOptions;
import balbucio.execlauncher.model.Executable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;
import java.util.Vector;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StorageTest {

    @TempDir
    Path tempDir;

    private Storage storage;

    @AfterEach
    void tearDown() {
        if (storage != null) storage.close();
    }

    private void configurePaths() {
        File install = tempDir.resolve("Execlauncher").toFile();
        install.mkdirs();
        Main.INSTALL_PATH = install;
        Main.DB_PATH = new File(install, "storage.db");
    }

    @Test
    void persistsCmdOptionsAcrossRestart() {
        configurePaths();

        storage = new Storage();
        Executable executable = new Executable();
        executable.setName("Test");
        executable.setCmd("java -jar app.jar");
        executable.setPath(tempDir.toString());
        executable.setCmdOptions(CmdOptions.builder().delayRun(true).delayRunInSecs(42).build());
        storage.saveExecutable(executable);
        storage.close();

        storage = new Storage();
        Vector<Executable> loaded = storage.executables();
        assertEquals(1, loaded.size());

        CmdOptions options = loaded.get(0).getCmdOptions();
        assertNotNull(options);
        assertTrue(options.isDelayRun());
        assertEquals(42, options.getDelayRunInSecs());
    }

    @Test
    void rejectsInvalidImport() {
        configurePaths();
        storage = new Storage();

        boolean threw = false;
        try {
            storage.importFromJSON("{not valid json");
        } catch (IllegalArgumentException e) {
            threw = true;
        }
        assertTrue(threw);
    }

    @Test
    void rejectsNullImport() {
        configurePaths();
        storage = new Storage();

        boolean threw = false;
        try {
            storage.importFromJSON(null);
        } catch (IllegalArgumentException e) {
            threw = true;
        }
        assertTrue(threw);
    }

    @Test
    void honorsSavedOrderAcrossRestart() {
        configurePaths();
        storage = new Storage();

        Executable first = new Executable();
        first.setName("First");
        first.setCmd("java -jar a.jar");
        first.setPath(tempDir.toString());
        storage.saveExecutable(first);

        Executable second = new Executable();
        second.setName("Second");
        second.setCmd("java -jar b.jar");
        second.setPath(tempDir.toString());
        storage.saveExecutable(second);

        Executable third = new Executable();
        third.setName("Third");
        third.setCmd("java -jar c.jar");
        third.setPath(tempDir.toString());
        storage.saveExecutable(third);

        Vector<Executable> reversed = new Vector<>();
        reversed.add(third);
        reversed.add(first);
        reversed.add(second);
        storage.saveOrder(reversed);
        storage.close();

        storage = new Storage();
        Vector<Executable> loaded = storage.executables();
        assertEquals(3, loaded.size());
        assertEquals("Third", loaded.get(0).getName());
        assertEquals("First", loaded.get(1).getName());
        assertEquals("Second", loaded.get(2).getName());
    }

    @Test
    void persistsSettings() {
        configurePaths();
        storage = new Storage();
        storage.setSetting(Storage.SETTING_THEME, "MIDNIGHT_BLUE");
        storage.setBooleanSetting(Storage.SETTING_CLOSE_TO_EXIT, true);
        storage.close();

        storage = new Storage();
        assertEquals("MIDNIGHT_BLUE", storage.getSetting(Storage.SETTING_THEME, "SPACEGRAY"));
        assertTrue(storage.getBooleanSetting(Storage.SETTING_CLOSE_TO_EXIT, false));
    }
}