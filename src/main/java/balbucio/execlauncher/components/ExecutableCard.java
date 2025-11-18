package balbucio.execlauncher.components;

import balbucio.execlauncher.Executor;
import balbucio.execlauncher.Main;
import balbucio.execlauncher.action.CreateOrUpdateExecutable;
import balbucio.execlauncher.model.Executable;
import balbucio.execlauncher.ui.LogsFrame;
import balbucio.execlauncher.utils.FileUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;

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
        run.setPreferredSize(new Dimension(50, 25));
        run.addActionListener(e -> {
            if (!active) {
                executor.start(executable);
            } else {
                executor.stop(executable);
            }
        });
        panel.add(run);

        JButton edit = new JButton("✏️");
        edit.setPreferredSize(new Dimension(50, 25));
        edit.addActionListener(e -> new CreateOrUpdateExecutable(executable));
        panel.add(edit);

        JButton remove = new JButton("🗑️");
        remove.setPreferredSize(new Dimension(50, 25));
        remove.addActionListener(e -> Executor.getInstance().delete(executable));
        panel.add(remove);

        if (active) {
            JButton showLogs = new JButton("Show Logs");
            showLogs.addActionListener(e -> {
                LogsFrame logsFrame = executable.getLogsFrame();
                if (logsFrame != null && logsFrame.isVisible()) {
                    logsFrame.requestFocus();
                } else {
                    executable.setLogsFrame(new LogsFrame(executable));
                }
            });
            panel.add(showLogs);
        }

        return panel;
    }

    public JPopupMenu getPopupMenu() {
        JPopupMenu popupMenu = new JPopupMenu();

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
                System.out.println(executable.getEnv());
                Main.instance.getStorage().saveExecutable(executable);
            }
        });
        popupMenu.add(addEnvFile);

        popupMenu.addSeparator();

        JMenuItem addOption = new JMenuItem("Manage command line options");
        addOption.addActionListener(e -> {
            executable.showOptions();
            Main.instance.getStorage().saveExecutable(executable);
        });
        popupMenu.add(addOption);

        return popupMenu;
    }
}
