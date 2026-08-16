package balbucio.execlauncher.action;

import balbucio.execlauncher.Main;
import balbucio.execlauncher.Storage;
import balbucio.execlauncher.settings.AppTheme;
import balbucio.execlauncher.settings.StartupService;
import de.milchreis.uibooster.model.Form;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.util.List;

@Slf4j
public class SettingsDialog {

    private final Main main;

    public SettingsDialog() {
        this.main = Main.instance;
        Storage storage = Storage.getInstance();

        String currentTheme = AppTheme.fromName(storage.getSetting(Storage.SETTING_THEME, AppTheme.SPACEGRAY.name())).getDisplayName();
        boolean closeToExit = storage.getBooleanSetting(Storage.SETTING_CLOSE_TO_EXIT, false);
        boolean startWithWindows = storage.getBooleanSetting(Storage.SETTING_START_WITH_WINDOWS, false);

        Form form = main.getUi()
                .createForm("Execlauncher settings")
                .addSelection("Theme:", themeOptions(currentTheme))
                .addCheckbox("Start with Windows", startWithWindows)
                .addCheckbox("Exit when the window is closed", closeToExit)
                .show();

        AppTheme theme = AppTheme.fromDisplay(form.getByIndex(0).asString());
        boolean newStartWithWindows = (Boolean) form.getByIndex(1).getValue();
        boolean newCloseToExit = (Boolean) form.getByIndex(2).getValue();

        storage.setSetting(Storage.SETTING_THEME, theme.name());
        storage.setBooleanSetting(Storage.SETTING_CLOSE_TO_EXIT, newCloseToExit);

        StartupService.setEnabled(newStartWithWindows);
        storage.setBooleanSetting(Storage.SETTING_START_WITH_WINDOWS, newStartWithWindows);

        try {
            theme.install();
            SwingUtilities.updateComponentTreeUI(main.getMainFrame());
        } catch (Exception e) {
            log.error("Failed to apply the selected theme", e);
        }
    }

    private static List<String> themeOptions(String currentTheme) {
        List<String> options = new java.util.ArrayList<>(List.of(AppTheme.displayNames()));
        options.remove(currentTheme);
        options.add(0, currentTheme);
        return options;
    }
}