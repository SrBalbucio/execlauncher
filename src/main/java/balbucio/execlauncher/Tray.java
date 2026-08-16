package balbucio.execlauncher;

import javax.swing.*;
import java.awt.*;

public class Tray {

    private final Main main;
    private TrayIcon icon;

    public Tray(Main main) {
        this.main = main;

        SystemTray systemTray = SystemTray.getSystemTray();
        try {
            systemTray.add(createIcon());
        } catch (Exception e) {
            main.showError(e);
        }
    }

    private TrayIcon createIcon() {
        icon = new TrayIcon(Toolkit.getDefaultToolkit().getImage(Tray.class.getResource("/icon_16.png")));
        icon.setPopupMenu(getPopupMenu());
        icon.setToolTip("Executables currently running: " + main.getExecutor().activeNow());
        icon.addActionListener((e) -> main.getMainFrame().open());
        return icon;
    }

    public void update() {
        SwingUtilities.invokeLater(() -> {
            if (icon != null) {
                icon.setToolTip("Executables currently running: " + main.getExecutor().activeNow());
            }
        });
    }

    public PopupMenu getPopupMenu() {
        PopupMenu popupMenu = new PopupMenu();

        {
            MenuItem menuItem = new MenuItem("Open dashboard");
            menuItem.addActionListener((e) -> main.getMainFrame().open());
            popupMenu.add(menuItem);
        }

        popupMenu.addSeparator();

        {
            MenuItem menuItem = new MenuItem("Start All");
            menuItem.addActionListener((e) -> main.getExecutor().startAll());
            popupMenu.add(menuItem);
        }

        {
            MenuItem menuItem = new MenuItem("Stop All");
            menuItem.addActionListener((e) -> main.getExecutor().stopAll());
            popupMenu.add(menuItem);
        }

        popupMenu.addSeparator();

        {
            MenuItem menuItem = new MenuItem("Exit");
            menuItem.addActionListener((e) -> main.exit());
            popupMenu.add(menuItem);
        }

        return popupMenu;
    }


}