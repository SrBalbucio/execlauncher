package balbucio.execlauncher.components;

import balbucio.execlauncher.Executor;
import balbucio.execlauncher.Main;
import balbucio.execlauncher.Storage;
import balbucio.execlauncher.action.CreateOrUpdateJavaExecutable;
import balbucio.execlauncher.action.CreateOrUpdatePNPMExecutable;
import balbucio.execlauncher.model.Executable;
import balbucio.execlauncher.utils.CommandLineUtils;
import balbucio.execlauncher.utils.FileUtils;
import balbucio.execlauncher.utils.JavaUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class ExecutableCard extends JPanel {

    private final Executable executable;
    private final boolean active;
    private final JPanel leftPanel;
    private final JPanel rightPanel;

    public ExecutableCard(Executable executable, boolean active, boolean selected) {
        super(new BorderLayout());
        this.setBorder(new EmptyBorder(6, 8, 6, 8));
        this.executable = executable;
        this.active = active;

        this.leftPanel = buildLeftPanel();
        this.rightPanel = buildRightPanel();
        this.add(leftPanel, BorderLayout.WEST);
        this.add(rightPanel, BorderLayout.EAST);
        this.setMaximumSize(new Dimension(Integer.MAX_VALUE, 64));
        this.setComponentPopupMenu(getPopupMenu());

        Color base = selected ? UIManager.getColor("List.selectionBackground") : UIManager.getColor("Panel.background");
        applyBackground(base);
        installHover(base);
    }

    private void applyBackground(Color color) {
        setBackground(color);
        leftPanel.setBackground(color);
        rightPanel.setBackground(color);
    }

    private void installHover(Color base) {
        Color hover = UIManager.getColor("List.selectionBackground");
        MouseAdapter adapter = new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                applyBackground(hover);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                applyBackground(base);
            }
        };
        addMouseListener(adapter);
        leftPanel.addMouseListener(adapter);
        rightPanel.addMouseListener(adapter);
    }

    public JPanel getLeftPanel() {
        return leftPanel;
    }

    public JPanel getRightPanel() {
        return rightPanel;
    }

    private JPanel buildLeftPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        top.setOpaque(false);

        JLabel icon = new JLabel(typeIcon());
        icon.setFont(icon.getFont().deriveFont(16f));
        top.add(icon);

        JLabel name = new JLabel(executable.getName());
        name.setFont(name.getFont().deriveFont(Font.BOLD, 14f));
        top.add(name);

        panel.add(top);

        {
            JPanel opt = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
            opt.setBackground(new Color(0, 0, 0, 0));
            opt.setOpaque(false);
            JLabel path = new JLabel(executable.getPath());
            path.setFont(path.getFont().deriveFont(12f));
            path.setForeground(Color.GRAY);
            path.setBorder(new EmptyBorder(2, 20, 0, 0));
            opt.add(path);
            panel.add(opt);
        }

        return panel;
    }

    private String typeIcon() {
        String type = executable.getType();
        if (type != null && type.equalsIgnoreCase("PNPM")) return "📦";
        return "☕";
    }

    private JPanel buildRightPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.RIGHT, 4, 8));

        JLabel dot = new JLabel("●");
        dot.setFont(dot.getFont().deriveFont(12f));
        dot.setForeground(active ? new Color(0, 160, 80) : new Color(200, 60, 60));
        panel.add(dot);

        JLabel status = new JLabel(active ? "Active" : "Inactive");
        status.setForeground(Color.GRAY);
        panel.add(status);

        JButton run = new JButton(active ? "⏹️" : "▶️");
        run.setToolTipText(active ? "Stop the execution of the executable." : "Start the executable.");
        run.setPreferredSize(new Dimension(50, 28));
        run.addActionListener(e -> {
            if (!active) {
                Executor.getInstance().init(executable);
            } else {
                Executor.getInstance().stop(executable);
            }
        });
        panel.add(run);

        JButton edit = new JButton("✏️");
        edit.setToolTipText("Edit the executable.");
        edit.setPreferredSize(new Dimension(50, 28));
        edit.addActionListener(e -> openEditor());
        panel.add(edit);

        if (active) {
            JButton showLogs = new JButton("Show Logs");
            showLogs.setToolTipText("Displays the executable logs.");
            showLogs.addActionListener(e -> executable.showLogsFrame());
            panel.add(showLogs);
        }

        JButton remove = new JButton("🗑️");
        remove.setToolTipText("Delete the executable. (confirmation required)");
        remove.setPreferredSize(new Dimension(50, 28));
        remove.addActionListener(e -> Main.instance.getUi().showConfirmDialog(
                "Do you really want this action?",
                "Are you sure?",
                () -> Executor.getInstance().delete(executable),
                () -> Main.instance.getMainFrame().update()));
        panel.add(remove);

        return panel;
    }

    private void openEditor() {
        if (executable.getType() != null && executable.getType().equalsIgnoreCase("PNPM")) {
            new CreateOrUpdatePNPMExecutable(executable);
        } else {
            new CreateOrUpdateJavaExecutable(executable);
        }
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
            if (name == null || name.isBlank()) return;
            executable.setName(name);
            Storage.getInstance().saveExecutable(executable);
            Main.instance.getMainFrame().update();
        });
        popupMenu.add(changeName);

        JMenuItem changePath = new JMenuItem("Change path");
        changePath.addActionListener(e -> {
            String path = Main.instance.getUi().showTextInputDialog("What is the new path to the executable?");
            if (path == null || path.isBlank()) return;
            executable.setPath(path);
            Storage.getInstance().saveExecutable(executable);
            Main.instance.getMainFrame().update();
        });
        popupMenu.add(changePath);

        if (executable.getType() != null && executable.getType().equalsIgnoreCase("Java")) {
            JMenuItem changeJava = new JMenuItem("Change Java");
            changeJava.addActionListener(e -> {
                List<String> javas = new ArrayList<>(JavaUtils.getJavaAvailable().values());
                String javaPath = Main.instance.getUi().showSelectionDialog("Select a new version of Java.", "Change Java", javas);
                if (javaPath == null || javaPath.isBlank()) return;

                executable.setCmd(CommandLineUtils.replaceProgram(executable.getCmd(), javaPath));
                Storage.getInstance().saveExecutable(executable);
            });
            popupMenu.add(changeJava);
        }

        JMenuItem openExplorer = new JMenuItem("Open in Explorer (only Windows)");
        openExplorer.addActionListener(e -> {
            try {
                new ProcessBuilder("explorer.exe", executable.getPath()).start();
            } catch (Exception ignored) {
            }
        });
        popupMenu.add(openExplorer);

        JMenuItem export = new JMenuItem("Export...");
        export.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            chooser.setFileFilter(new FileNameExtensionFilter("JSON Files", "json"));
            chooser.setSelectedFile(new File(safeFileName(executable.getName()) + ".json"));
            if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

            try {
                Files.writeString(chooser.getSelectedFile().toPath(), Storage.getInstance().toJSON(executable));
            } catch (Exception ex) {
                Main.instance.showError(ex);
            }
        });
        popupMenu.add(export);

        return popupMenu;
    }

    private String safeFileName(String name) {
        if (name == null || name.isBlank()) return "executable";
        return name.replaceAll("[\\\\/:*?\"<>|]", "_");
    }
}