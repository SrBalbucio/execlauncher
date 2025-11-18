package balbucio.execlauncher;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class Tray {

    private final Main main;

    public Tray(Main main) {
        this.main = main;

        SystemTray systemTray = SystemTray.getSystemTray();
        try {
            systemTray.add(getIcon());
        } catch (Exception e) {
            main.showError(e);
        }
    }

    private TrayIcon icon;

    public TrayIcon getIcon() {
        icon = new TrayIcon(Toolkit.getDefaultToolkit().getImage(Tray.class.getResource("/icon.png")));
        icon.setPopupMenu(getPopupMenu());
        icon.setToolTip("Executables currently running: " + main.getExecutor().activeNow());
        icon.addActionListener((e) -> main.getMainFrame().open());
        return icon;
    }

    public void update(){
        icon.setToolTip("Executables currently running: " + main.getExecutor().activeNow());
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
