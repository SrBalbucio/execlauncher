package balbucio.execlauncher.components;

import balbucio.execlauncher.Executor;
import balbucio.execlauncher.action.CreateOrUpdateExecutable;
import balbucio.execlauncher.model.Executable;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class ExecutableCard extends JPanel {

    public static Rectangle MAX_WINDOW = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();

    private final Executable executable;
    private final boolean active;

    public ExecutableCard(Executable executable, boolean active) {
        super(new BorderLayout());
        this.setBorder(new EmptyBorder(5, 5,5,5));
        this.executable = executable;
        this.active = active;
        JPanel leftPanel = getLeftPanel();
        JPanel rightPanel = getRightPanel();
        this.add(leftPanel, BorderLayout.WEST);
        this.add(rightPanel, BorderLayout.EAST);
        this.setMaximumSize(new Dimension(MAX_WINDOW.width, 60));
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
        status.setForeground(active ? Color.BLUE : Color.RED);
        panel.add(status);

        JButton run = new JButton(active ? "⏹️" : "▶️");
        run.setPreferredSize(new Dimension(50, 25));
        run.addActionListener(e -> {
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

        if(active) {
            JButton showLogs = new JButton("Show Logs");
            showLogs.addActionListener(e -> {
            });
            panel.add(showLogs);
        }

        return panel;
    }
}
