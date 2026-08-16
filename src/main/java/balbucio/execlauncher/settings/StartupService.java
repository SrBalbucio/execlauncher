package balbucio.execlauncher.settings;

import balbucio.execlauncher.Main;
import balbucio.execlauncher.utils.JavaUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.prefs.Preferences;

@Slf4j
public final class StartupService {

    private static final String RUN_KEY = "Software\\Microsoft\\Windows\\CurrentVersion\\Run";
    private static final String APP_NAME = "Execlauncher";

    private StartupService() {
    }

    public static boolean isEnabled() {
        if (!JavaUtils.isWindows()) return false;
        try {
            return Preferences.userRoot().node(RUN_KEY).get(APP_NAME, null) != null;
        } catch (Exception e) {
            log.error("Failed to read startup preference", e);
            return false;
        }
    }

    public static void setEnabled(boolean enabled) {
        if (!JavaUtils.isWindows()) return;
        try {
            Preferences prefs = Preferences.userRoot().node(RUN_KEY);
            if (enabled) {
                prefs.put(APP_NAME, launchCommand());
            } else {
                prefs.remove(APP_NAME);
            }
        } catch (Exception e) {
            log.error("Failed to update startup preference", e);
        }
    }

    private static String launchCommand() {
        String appPath = System.getProperty("jpackage.app-path");
        if (appPath != null && new File(appPath).isFile()) {
            return "\"" + appPath + "\"";
        }
        try {
            File jar = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            File javaBin = new File(System.getProperty("java.home"), "bin/" + JavaUtils.javaExecutableName());
            return "\"" + javaBin.getAbsolutePath() + "\" -jar \"" + jar.getAbsolutePath() + "\"";
        } catch (Exception e) {
            log.error("Failed to resolve the launcher command", e);
            return "\"" + APP_NAME + "\"";
        }
    }
}