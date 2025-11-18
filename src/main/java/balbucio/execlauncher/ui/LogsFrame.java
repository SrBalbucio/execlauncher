package balbucio.execlauncher.ui;

import balbucio.execlauncher.model.Executable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.io.File;
import java.io.FileWriter;
import java.util.Scanner;

public class LogsFrame extends JFrame implements ComponentListener, ContainerListener {

    private final Executable executable;

    public LogsFrame(Executable executable) {
        super(executable.getName() + " in Execlauncher");
        this.setIconImage(Toolkit.getDefaultToolkit().getImage(MainFrame.class.getResource("/icon.png")));
        this.setMinimumSize(new Dimension(500, 500));
        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        this.executable = executable;
        this.setLayout(new BorderLayout());
        this.add(getActions(), BorderLayout.NORTH);
        this.add(getLogs(), BorderLayout.CENTER);
        this.addComponentListener(this);
        this.addContainerListener(this);
        SwingUtilities.invokeLater(this::initLogStream);
    }

    public JPanel getActions() {
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.LEFT));

        {
            JButton button = new JButton("Clear");
            button.addActionListener((e) -> {
                textPane.setText("");
                addLog("Console cleared!");
            });
            panel.add(button);
        }

        {
            JButton button = new JButton("Save All");
            button.addActionListener((e) -> {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                fileChooser.showSaveDialog(this);

                if (fileChooser.getSelectedFile() != null) {
                    try {
                        File selectedFile = fileChooser.getSelectedFile();
                        FileWriter fileWriter = new FileWriter(selectedFile);
                        fileWriter.write(textPane.getText());
                        fileWriter.close();
                        textPane.setText("Saved with successfully in " + selectedFile.getAbsolutePath());
                    } catch (Exception ex) {
                        addLog("---------------------------------------------------");
                        addLog("Execlauncher exception : " + ex.getMessage());
                        addLog("---------------------------------------------------");
                    }
                }
            });
            panel.add(button);
        }

        return panel;
    }

    private JTextPane textPane;

    public JScrollPane getLogs() {
        this.textPane = new JTextPane();
        textPane.setText(this.executable.getLogs().toString());
        this.textPane.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(this.textPane);
        scrollPane.setViewportView(this.textPane);
        return scrollPane;
    }

    public void addLog(String msg) {
        executable.getLogs().append("\n").append(msg);
        textPane.setText(executable.getLogs().toString());
    }

    private Thread thread;

    public void initLogStream() {
        if (executable.getInputStream() == null) return;
        if (thread != null) thread.interrupt();

        this.thread = new Thread(() -> {
            Scanner scanner = new Scanner(executable.getInputStream());
            while (scanner.hasNextLine()) {
                addLog(scanner.nextLine());
            }
            scanner.close();
        });
        thread.start();
    }

    public void stopLogStream() {
        if (thread == null) return;

        thread.interrupt();
        thread = null;
    }

    @Override
    public void componentResized(ComponentEvent componentEvent) {

    }

    @Override
    public void componentMoved(ComponentEvent componentEvent) {

    }

    @Override
    public void componentShown(ComponentEvent componentEvent) {
    }

    @Override
    public void componentHidden(ComponentEvent componentEvent) {

    }

    @Override
    public void componentAdded(ContainerEvent containerEvent) {

    }

    @Override
    public void componentRemoved(ContainerEvent containerEvent) {

    }
}
