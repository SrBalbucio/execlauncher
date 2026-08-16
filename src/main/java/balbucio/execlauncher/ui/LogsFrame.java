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
import java.io.InputStream;
import java.util.Scanner;

public class LogsFrame extends JFrame implements ComponentListener, ContainerListener {

    private final Executable executable;
    private JTextPane textPane;
    private volatile Thread readerThread;
    private volatile InputStream readingStream;

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
                    try (FileWriter fileWriter = new FileWriter(fileChooser.getSelectedFile())) {
                        fileWriter.write(textPane.getText());
                        addLog("Saved with successfully in " + fileChooser.getSelectedFile().getAbsolutePath());
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

    public JScrollPane getLogs() {
        this.textPane = new JTextPane();
        textPane.setText(this.executable.getLogs().toString());
        this.textPane.setEditable(false);
        return new JScrollPane(this.textPane);
    }

    public void addLog(String msg) {
        SwingUtilities.invokeLater(() -> {
            if (textPane == null) return;
            executable.getLogs().append("\n").append(msg);
            textPane.setText(executable.getLogs().toString());
        });
    }

    public void clearLogs() {
        executable.getLogs().setLength(0);
        SwingUtilities.invokeLater(() -> {
            if (textPane != null) textPane.setText("");
        });
    }

    public synchronized void initLogStream() {
        InputStream stream = executable.getInputStream();
        if (stream == null) return;
        if (readingStream == stream && readerThread != null && readerThread.isAlive()) return;

        if (readerThread != null) readerThread.interrupt();
        readingStream = stream;

        Thread thread = new Thread(() -> readLogs(stream), "Execlauncher-Logs-" + executable.getName());
        thread.setDaemon(true);
        thread.start();
        readerThread = thread;
    }

    private void readLogs(InputStream stream) {
        try (Scanner scanner = new Scanner(stream)) {
            while (scanner.hasNextLine()) {
                addLog(scanner.nextLine());
            }
        } catch (Exception ignored) {
        }
    }

    public synchronized void stopLogStream() {
        if (readerThread == null) return;
        readerThread.interrupt();
        readerThread = null;
        readingStream = null;
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