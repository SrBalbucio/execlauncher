package balbucio.execlauncher;

import balbucio.execlauncher.ui.MainFrame;
import com.formdev.flatlaf.intellijthemes.FlatCyanLightIJTheme;
import com.formdev.flatlaf.intellijthemes.FlatGradiantoMidnightBlueIJTheme;
import com.formdev.flatlaf.intellijthemes.FlatSpacegrayIJTheme;
import de.milchreis.uibooster.UiBooster;
import de.milchreis.uibooster.model.UiBoosterOptions;
import lombok.Getter;

import javax.swing.plaf.basic.BasicLookAndFeel;
import java.awt.*;
import java.io.File;
import java.util.Optional;
import java.util.spi.ToolProvider;

@Getter
public class Main {

    public static File INSTALL_PATH = new File(System.getenv("APPDATA"), "Execlauncher");
    public static File DB_PATH = new File(INSTALL_PATH, "storage.db");
    public static Main instance;
    public static Desktop desktop = Desktop.getDesktop();

    public static void main(String[] args) {
        ToolProvider.findFirst("jpackage").ifPresent((tool) -> tool.run(System.out, System.err));
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

    public void showError(Exception throwable) {
        ui.showException(
                "Check below for the cause of this failure; Execlauncher will likely continue to function. Check the status on the Execlauncher main screen.",
                "Execlauncher encountered problems during execution!",
                throwable);
    }

    public void exit() {
        executor.stopAll();
        executor.getExecutor().shutdown();
        storage.close();
        System.exit(0);
    }
}