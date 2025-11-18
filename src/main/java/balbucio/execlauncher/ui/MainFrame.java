package balbucio.execlauncher.ui;

import balbucio.execlauncher.Executor;
import balbucio.execlauncher.Main;
import balbucio.execlauncher.action.CreateOrUpdateExecutable;
import balbucio.execlauncher.action.CreateOrUpdateJavaExecutable;
import balbucio.execlauncher.components.ExecutableCard;
import balbucio.execlauncher.model.Executable;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {

    private Main main;
    private Executable selected;

    public MainFrame(Main main) {
        super("Execlauncher");
        this.main = main;
        this.setDefaultCloseOperation(HIDE_ON_CLOSE);
        this.setLayout(new BorderLayout());
        this.setMinimumSize(new Dimension(600, 360));
        this.add(getActions(), BorderLayout.NORTH);
        this.add(getMainPanel(), BorderLayout.CENTER);
        this.pack();
        this.setVisible(true);
        SwingUtilities.invokeLater(this::update);
    }

    public JPanel getActions() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        {
            JButton button = new JButton("Add. Executable");
            button.addActionListener(e -> {
                String type = main.getUi().showSelectionDialog("Select the executable type:", "Add. Executable", "Java", "Script");

                if (type == null) return;

                switch (type) {
                    case "Java" -> new CreateOrUpdateJavaExecutable();
                    case "Script" -> new CreateOrUpdateExecutable();
                }
            });
            panel.add(button);
        }

        {
            JButton button = new JButton("Start All");
            panel.add(button);
        }

        {
            JButton button = new JButton("Stop All");
            panel.add(button);
        }

        return panel;
    }

    JPanel listPanel;

    public JScrollPane getMainPanel() {
        this.listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));

        JScrollPane panel = new JScrollPane(listPanel);
        panel.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        return panel;
    }

    public void update() {
        listPanel.setVisible(false);
        listPanel.removeAll();

        for (Executable executable : Executor.getInstance().getSaved()) {
            System.out.println(executable);
            listPanel.add(new ExecutableCard(executable, Executor.getInstance().isActive(executable)));
        }

        listPanel.setVisible(true);
        listPanel.revalidate();
        listPanel.repaint();
    }
}
