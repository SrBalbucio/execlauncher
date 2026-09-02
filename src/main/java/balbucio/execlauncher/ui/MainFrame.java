package balbucio.execlauncher.ui;

import balbucio.execlauncher.Executor;
import balbucio.execlauncher.Main;
import balbucio.execlauncher.Storage;
import balbucio.execlauncher.action.CreateOrUpdateBashExecutable;
import balbucio.execlauncher.action.CreateOrUpdateCmdExecutable;
import balbucio.execlauncher.action.CreateOrUpdateJavaExecutable;
import balbucio.execlauncher.action.CreateOrUpdatePNPMExecutable;
import balbucio.execlauncher.action.CreateOrUpdateShellExecutable;
import balbucio.execlauncher.action.SettingsDialog;
import balbucio.execlauncher.components.ExecutableCard;
import balbucio.execlauncher.model.Executable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.nio.file.Files;
import java.util.List;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

public class MainFrame extends JFrame {

    private static final int ROW_HEIGHT = 64;
    private static final Preferences PREFS = Preferences.userNodeForPackage(MainFrame.class);

    private final Main main;
    private JTextField searchField;
    private JPanel listPanel;
    private JLabel statusBar;

    private Executable drag;
    private int dragStartIndex = -1;
    private int dragStartY;

    public MainFrame(Main main) {
        super("Execlauncher");
        this.setIconImage(Toolkit.getDefaultToolkit().getImage(MainFrame.class.getResource("/icon.png")));
        this.main = main;
        this.setDefaultCloseOperation(HIDE_ON_CLOSE);
        this.setLayout(new BorderLayout());
        this.setMinimumSize(new Dimension(600, 360));
        this.add(getActions(), BorderLayout.NORTH);
        this.add(getMainPanel(), BorderLayout.CENTER);
        this.add(getStatusBar(), BorderLayout.SOUTH);
        restoreBounds();
        setupCloseBehavior();
        installShortcuts();
        this.setVisible(true);
        SwingUtilities.invokeLater(this::update);
    }

    public JPanel getActions() {
        JPanel panel = new JPanel(new BorderLayout(8, 0));
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 4, 8));

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));

        {
            JButton button = new JButton("Add. Executable");
            button.addActionListener(e -> promptAddExecutable());
            buttons.add(button);
        }

        {
            JButton button = new JButton("Start All");
            button.addActionListener(e -> Executor.getInstance().startAll());
            buttons.add(button);
        }

        {
            JButton button = new JButton("Stop All");
            button.addActionListener(e -> Executor.getInstance().stopAll());
            buttons.add(button);
        }

        {
            JButton button = new JButton("Import...");
            button.addActionListener(e -> importExecutable());
            buttons.add(button);
        }

        {
            JButton button = new JButton("Settings");
            button.addActionListener(e -> new SettingsDialog());
            buttons.add(button);
        }

        {
            JButton button = new JButton("Exit");
            button.addActionListener(e -> main.exit());
            buttons.add(button);
        }

        panel.add(buttons, BorderLayout.WEST);

        this.searchField = new JTextField();
        searchField.setToolTipText("Search by name or path (Ctrl+F)");
        searchField.setPreferredSize(new Dimension(220, 28));
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                update();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                update();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                update();
            }
        });
        searchField.registerKeyboardAction(e -> {
            searchField.setText("");
            update();
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_FOCUSED);

        panel.add(searchField, BorderLayout.EAST);
        return panel;
    }

    private void promptAddExecutable() {
        String type = main.getUi().showSelectionDialog("Select the executable type:", "Add. Executable", "Java", "PNPM", "Bash", "CMD", "Shell (Bash/CMD)");
        if (type == null) return;

        switch (type) {
            case "Java" -> new CreateOrUpdateJavaExecutable();
            case "PNPM" -> new CreateOrUpdatePNPMExecutable();
            case "Bash" -> new CreateOrUpdateBashExecutable();
            case "CMD" -> new CreateOrUpdateCmdExecutable();
            case "Shell (Bash/CMD)" -> new CreateOrUpdateShellExecutable();
        }
    }

    private void importExecutable() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setFileFilter(new FileNameExtensionFilter("JSON Files", "json"));
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;

        try {
            String json = Files.readString(chooser.getSelectedFile().toPath());
            Storage.getInstance().importFromJSON(json);
            update();
        } catch (Exception ex) {
            main.getUi().showErrorDialog("Failed to import: " + ex.getMessage(), "Import failed");
        }
    }

    public JScrollPane getMainPanel() {
        this.listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));

        JScrollPane panel = new JScrollPane(listPanel);
        panel.getVerticalScrollBar().setUnitIncrement(24);
        panel.getVerticalScrollBar().setBlockIncrement(96);
        JPopupMenu popupMenu = new JPopupMenu();

        {
            JMenuItem item = new JMenuItem("Refresh...");
            item.addActionListener(e -> this.update());
            popupMenu.add(item);
        }

        panel.setComponentPopupMenu(popupMenu);
        panel.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        return panel;
    }

    private JLabel getStatusBar() {
        statusBar = new JLabel();
        statusBar.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        statusBar.setForeground(Color.GRAY);
        return statusBar;
    }

    private void updateStatusBar() {
        if (statusBar == null) return;
        int running = Executor.getInstance().activeNow();
        int total = Executor.getInstance().getSaved().size();
        statusBar.setText(total + " executables · " + running + " running");
    }

    public void update() {
        String query = searchField == null ? "" : searchField.getText().trim().toLowerCase();
        List<Executable> saved = Executor.getInstance().getSaved();

        List<Executable> filtered = saved.stream()
                .filter(e -> query.isEmpty()
                        || (e.getName() != null && e.getName().toLowerCase().contains(query))
                        || (e.getPath() != null && e.getPath().toLowerCase().contains(query)))
                .collect(Collectors.toList());

        listPanel.removeAll();

        if (filtered.isEmpty()) {
            listPanel.add(emptyState(saved.isEmpty()));
        } else {
            for (Executable executable : filtered) {
                ExecutableCard card = new ExecutableCard(executable, Executor.getInstance().isActive(executable), false);
                card.getLeftPanel().addMouseListener(dragAdapter(executable));
                card.getLeftPanel().addMouseMotionListener(dragAdapter(executable));
                listPanel.add(card);
            }
        }

        listPanel.revalidate();
        listPanel.repaint();
        updateStatusBar();
    }

    private Component emptyState(boolean noneAtAll) {
        JPanel panel = new JPanel(new GridBagLayout());
        JLabel label = new JLabel(noneAtAll
                ? "No executables yet. Click 'Add. Executable' to create one."
                : "No executables match the current filter.");
        label.setForeground(Color.GRAY);
        panel.add(label);
        panel.setPreferredSize(new Dimension(1, 240));
        return panel;
    }

    private MouseAdapter dragAdapter(Executable executable) {
        return new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (searchField != null && !searchField.getText().isBlank()) return;
                List<Executable> saved = Executor.getInstance().getSaved();
                int idx = saved.indexOf(executable);
                if (idx < 0) return;
                drag = executable;
                dragStartIndex = idx;
                dragStartY = e.getYOnScreen();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (drag == null || drag != executable) return;
                List<Executable> saved = Executor.getInstance().getSaved();
                int deltaY = e.getYOnScreen() - dragStartY;
                int target = dragStartIndex + Math.round(deltaY / (float) ROW_HEIGHT);
                target = Math.max(0, Math.min(saved.size() - 1, target));
                int current = saved.indexOf(executable);
                if (target != current && current >= 0) {
                    saved.remove(current);
                    saved.add(target, executable);
                    update();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (drag == executable) {
                    drag = null;
                    dragStartIndex = -1;
                    Storage.getInstance().saveOrder(Executor.getInstance().getSaved());
                }
            }
        };
    }

    public void open() {
        update();
        this.setVisible(true);
    }

    private void setupCloseBehavior() {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (Storage.getInstance().getBooleanSetting(Storage.SETTING_CLOSE_TO_EXIT, false)) {
                    main.exit();
                }
            }
        });
    }

    private void installShortcuts() {
        InputMap inputMap = getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = getRootPane().getActionMap();

        inputMap.put(KeyStroke.getKeyStroke("control N"), "addExecutable");
        actionMap.put("addExecutable", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                promptAddExecutable();
            }
        });

        inputMap.put(KeyStroke.getKeyStroke("control F"), "focusSearch");
        actionMap.put("focusSearch", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                searchField.requestFocusInWindow();
            }
        });

        inputMap.put(KeyStroke.getKeyStroke("control I"), "importExecutable");
        actionMap.put("importExecutable", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                importExecutable();
            }
        });

        inputMap.put(KeyStroke.getKeyStroke("control COMMA"), "openSettings");
        actionMap.put("openSettings", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                new SettingsDialog();
            }
        });
    }

    private void restoreBounds() {
        int x = PREFS.getInt("x", Integer.MIN_VALUE);
        int y = PREFS.getInt("y", Integer.MIN_VALUE);
        int width = PREFS.getInt("w", 820);
        int height = PREFS.getInt("h", 520);

        if (x != Integer.MIN_VALUE) {
            setLocation(x, y);
        }
        setSize(width, height);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                saveBounds();
            }

            @Override
            public void componentMoved(ComponentEvent e) {
                saveBounds();
            }
        });
    }

    private void saveBounds() {
        if (!isShowing() || getExtendedState() != Frame.NORMAL) return;
        Rectangle bounds = getBounds();
        PREFS.putInt("x", bounds.x);
        PREFS.putInt("y", bounds.y);
        PREFS.putInt("w", bounds.width);
        PREFS.putInt("h", bounds.height);
    }
}
