package balbucio.execlauncher;

import balbucio.execlauncher.ui.MainFrame;
import de.milchreis.uibooster.UiBooster;
import lombok.Getter;

import java.awt.*;
import java.io.File;

@Getter
public class Main {

    public static File INSTALL_PATH = new File(System.getenv("APPDATA"), "Execlauncher");
    public static File DB_PATH = new File(INSTALL_PATH, "storage.db");
    public static Main instance;
    public static Desktop desktop = Desktop.getDesktop();

    public static void main(String[] args) {
        INSTALL_PATH.mkdirs();
        instance = new Main();
    }

    private final UiBooster ui;
    private final Storage storage;
    private final Executor executor;
    private final MainFrame mainFrame;

    public Main() {
        this.ui = new UiBooster();
        this.storage = new Storage();
        this.executor = new Executor(this);
        this.mainFrame = new MainFrame(this);
    }

    public void showError(Exception throwable) {
        ui.showException(
                "Execlauncher encountered problems during execution!",
                "Check below for the cause of this failure; Execlauncher will likely continue to function. Check the status on the Execlauncher main screen.",
                throwable);
    }
}