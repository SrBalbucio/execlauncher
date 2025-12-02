package balbucio.execlauncher.components;

import balbucio.execlauncher.Executor;
import balbucio.execlauncher.Main;
import balbucio.execlauncher.Storage;
import balbucio.execlauncher.model.Executable;
import balbucio.execlauncher.utils.FileUtils;
import balbucio.execlauncher.utils.JavaUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.util.List;

public class ExecutableCard extends JPanel {

    public static Rectangle MAX_WINDOW = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
    private static final Executor executor = Executor.getInstance();

    private final Executable executable;
    private final boolean active;

    public ExecutableCard(Executable executable, boolean active) {
        super(new BorderLayout());
        this.setBorder(new EmptyBorder(5, 5, 5, 5));
        this.executable = executable;
        this.active = active;
        JPanel leftPanel = getLeftPanel();
        JPanel rightPanel = getRightPanel();
        this.add(leftPanel, BorderLayout.WEST);
        this.add(rightPanel, BorderLayout.EAST);
        this.setMaximumSize(new Dimension(MAX_WINDOW.width, 60));
        this.setComponentPopupMenu(getPopupMenu());
    }

    public JPanel getLeftPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel name = new JLabel(executable.getName());
        name.setFont(name.getFont().deriveFont(14f));
        panel.add(name);

        JLabel path = new JLabel(executable.getPath());
        path.setFont(path.getFont().deriveFont(12f));
        path.setForeground(Color.gray);
        panel.add(path);

        return panel;
    }

    public JPanel getRightPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.RIGHT));

        JLabel status = new JLabel(active ? "Active" : "Inactive");
        status.setForeground(active ? Color.GREEN : Color.RED);
        panel.add(status);

        JButton run = new JButton(active ? "⏹️" : "▶️");
        run.setToolTipText(active ? "Stop the execution of the executable." : "Start the executable.");
        run.setPreferredSize(new Dimension(60, 28));
        run.addActionListener(e -> {
            if (!active) {
                executor.init(executable);
            } else {
                executor.stop(executable);
            }
        });
        panel.add(run);

//        JButton edit = new JButton("✏️");
//        edit.setPreferredSize(new Dimension(50, 25));
//        edit.addActionListener(e -> new CreateOrUpdateExecutable(executable));
//        panel.add(edit);

        JButton remove = new JButton("🗑️");
        remove.setToolTipText("Delete the executable. (confirmation required)");
        remove.setPreferredSize(new Dimension(60, 28));
        remove.addActionListener(e -> Main.instance.getUi().showConfirmDialog(
                "Do you really want this action?",
                "Are you sure?",
                () -> Executor.getInstance().delete(executable),
                () -> Main.instance.getMainFrame().update()));
        panel.add(remove);

        if (active) {
            JButton showLogs = new JButton("Show Logs");
            showLogs.setToolTipText("Displays the executable logs.");
            showLogs.addActionListener(e -> executable.showLogsFrame());
            panel.add(showLogs);
        }

        return panel;
    }

    public JPopupMenu getPopupMenu() {
        JPopupMenu popupMenu = new JPopupMenu();

        JCheckBoxMenuItem autoShow = new JCheckBoxMenuItem("Auto Show Logs", executable.isAutoShowLogs());
        autoShow.addActionListener(e -> {
            executable.setAutoShowLogs(autoShow.getState());
            Storage.getInstance().saveExecutable(executable);
        });
        popupMenu.add(autoShow);

        popupMenu.addSeparator();

        JMenuItem manageEnv = new JMenuItem("Manage environment variables");
        manageEnv.addActionListener(e -> executable.showVars());
        popupMenu.add(manageEnv);

        JMenuItem addEnvFile = new JMenuItem("Add .env file");
        addEnvFile.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            fileChooser.setCurrentDirectory(executable.getFilePath());
            fileChooser.showOpenDialog(this);
            File envFile = fileChooser.getSelectedFile();

            if (envFile != null) {
                executable.addEnvVars(FileUtils.readVars(envFile));
                Storage.getInstance().saveExecutable(executable);
            }
        });
        popupMenu.add(addEnvFile);

        popupMenu.addSeparator();

        JMenuItem addOption = new JMenuItem("Manage command line options");
        addOption.addActionListener(e -> {
            executable.showOptions();
            Storage.getInstance().saveExecutable(executable);
        });
        popupMenu.add(addOption);
        popupMenu.addSeparator();

        JMenuItem startCmds = new JMenuItem("Add start cmds");
        startCmds.addActionListener(e -> {
            executable.showStartCmds();
            Storage.getInstance().saveExecutable(executable);
        });
        popupMenu.add(startCmds);

        JMenuItem stopCmds = new JMenuItem("Add stop cmds");
        stopCmds.addActionListener(e -> {
            executable.showStopCmds();
            Storage.getInstance().saveExecutable(executable);
        });
        popupMenu.add(stopCmds);

        JMenuItem optCmds = new JMenuItem("Manage cmd options");
        optCmds.addActionListener(e -> executable.showCmdOptions());
        popupMenu.add(optCmds);

        popupMenu.addSeparator();

        JMenuItem changeName = new JMenuItem("Change name");
        changeName.addActionListener(e -> {
            String name = Main.instance.getUi().showTextInputDialog("What will the new name be?");
            executable.setName(name);
            Storage.getInstance().saveExecutable(executable);
        });
        popupMenu.add(changeName);

        if (executable.getType() != null && executable.getType().equalsIgnoreCase("Java")) {
            JMenuItem changeJava = new JMenuItem("Change Java");
            changeJava.addActionListener(e -> {
                String javaPath = Main.instance.getUi().showSelectionDialog("Select a new version of Java.", "Change Java", (List<String>) JavaUtils.getJavaAvailable().values());

                String[] cmdParts = executable.getCmd().split(" ");
                cmdParts[0] = "\"" + javaPath + "\"";
                executable.setCmd(String.join(" ", cmdParts));

                Storage.getInstance().saveExecutable(executable);
            });
            popupMenu.add(changeJava);
        }

        JMenuItem export = new JMenuItem("Export...");
        export.addActionListener(e -> {
            String json = Storage.getInstance().toJSON(executable);
            Main.instance.getUi().showTextArea("Save the JSON below so you can import this executable into another instance:", "Export executable", json);
        });
        popupMenu.add(export);

        return popupMenu;
    }
}
