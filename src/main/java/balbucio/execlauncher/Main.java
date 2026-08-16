package balbucio.execlauncher;

import balbucio.execlauncher.ui.MainFrame;
import com.formdev.flatlaf.intellijthemes.FlatSpacegrayIJTheme;
import de.milchreis.uibooster.UiBooster;
import de.milchreis.uibooster.model.UiBoosterOptions;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.File;

@Getter
@Slf4j
public class Main {

    public static File INSTALL_PATH = resolveInstallPath();
    public static File DB_PATH = new File(INSTALL_PATH, "storage.db");
    public static Main instance;

    public static void main(String[] args) {
        INSTALL_PATH.mkdirs();
        instance = new Main();
    }

    private final UiBooster ui;
    private final Storage storage;
    private final Tray tray;
    private final Executor executor;
    private final MainFrame mainFrame;

    public Main() {
        this.ui = new UiBooster(new UiBoosterOptions(new FlatSpacegrayIJTheme(), "/icon.png", UiBoosterOptions.defaultLoadingImage));
        this.storage = new Storage();
        this.executor = new Executor(this);
        this.tray = new Tray(this);
        this.mainFrame = new MainFrame(this);
    }

    private static File resolveInstallPath() {
        String appData = System.getenv("APPDATA");
        if (appData != null && !appData.isBlank()) {
            return new File(appData, "Execlauncher");
        }
        return new File(System.getProperty("user.home"), ".execlauncher");
    }

    public void showError(Exception throwable) {
        log.error("Execlauncher encountered an error", throwable);
        ui.showException(
                "Check below for the cause of this failure; Execlauncher will likely continue to function. Check the status on the Execlauncher main screen.",
                "Execlauncher encountered problems during execution!",
                throwable);
    }

    public void exit() {
        executor.stopAll();
        storage.close();
        System.exit(0);
    }
}