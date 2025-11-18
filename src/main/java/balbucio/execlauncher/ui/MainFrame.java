package balbucio.execlauncher.ui;

import balbucio.execlauncher.Executor;
import balbucio.execlauncher.Main;
import balbucio.execlauncher.Storage;
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
        this.setIconImage(Toolkit.getDefaultToolkit().getImage(MainFrame.class.getResource("/icon.png")));
        this.main = main;
        this.setLocationRelativeTo(null);
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
                String type = main.getUi().showSelectionDialog("Select the executable type:", "Add. Executable", "Java");

                if (type == null) return;

                switch (type) {
                    case "Java" -> new CreateOrUpdateJavaExecutable();
                }
            });
            panel.add(button);
        }

        {
            JButton button = new JButton("Start All");
            button.addActionListener((e) -> Executor.getInstance().startAll());
            panel.add(button);
        }

        {
            JButton button = new JButton("Stop All");
            button.addActionListener((e) -> Executor.getInstance().stopAll());
            panel.add(button);
        }

        {
            JButton button = new JButton("Import...");
            button.addActionListener(e -> {
                String json = main.getUi().showTextInputDialog("Enter the configuration JSON.");
                Storage.getInstance().importFromJSON(json);
            });
            panel.add(button);
        }

        return panel;
    }

    JPanel listPanel;

    public JScrollPane getMainPanel() {
        this.listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));

        JScrollPane panel = new JScrollPane(listPanel);
        JPopupMenu popupMenu = new JPopupMenu();

        {
            JMenuItem item = new JMenuItem("Refresh...");
            item.addActionListener((e) -> this.update());
            popupMenu.add(item);
        }

        panel.setComponentPopupMenu(popupMenu);
        panel.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        return panel;
    }

    public void update() {
        listPanel.setVisible(false);
        listPanel.removeAll();

        for (Executable executable : Executor.getInstance().getSaved()) {
            listPanel.add(new ExecutableCard(executable, Executor.getInstance().isActive(executable)));
        }

        listPanel.setVisible(true);
        listPanel.revalidate();
        listPanel.repaint();
    }

    public void open() {
        update();
        this.setVisible(true);
    }
}
