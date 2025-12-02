package balbucio.execlauncher.model;

import balbucio.execlauncher.Main;
import balbucio.execlauncher.action.UpdateCmdOptions;
import balbucio.execlauncher.ui.LogsFrame;
import balbucio.execlauncher.utils.MapUtils;
import com.google.gson.annotations.Expose;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.*;
import java.util.*;

@Getter
@Setter
@ToString
public class Executable {

    @Expose
    private UUID id = UUID.randomUUID();
    @Expose
    private String name;
    @Expose
    private String cmd;
    @Expose
    private String path;
    @Expose
    private Map<String, String> env = new HashMap<>();
    @Expose
    private Map<String, String> options = new HashMap<>();
    @Expose
    private CmdOptions cmdOptions;
    @Expose
    private List<String> startCmds = new ArrayList<>();
    @Expose
    private List<String> stopCmds = new ArrayList<>();
    @Expose
    private String type;
    @Expose
    private boolean autoShowLogs;
    private BufferedWriter outputWriter;
    private InputStream errorStream;
    private InputStream inputStream;
    private StringBuilder logs = new StringBuilder();
    private LogsFrame logsFrame;

    public File getFilePath() {
        return new File(path);
    }

    public void addEnvVars(Map<String, String> env) {
        this.env.putAll(env);
    }

    public void addEnvVar(String key, String value) {
        this.env.put(key, value);
    }

    public void showVars() {
        System.out.println(env);
        String[][] envs = Main.instance.getUi().showTable(MapUtils.mapToArray2d(this.getEnv()), new String[]{"Key", "Value"}, "Environment Variables");
        this.setEnv(MapUtils.array2dToMap(envs));
    }

    public void showOptions() {
        String[][] ops = Main.instance.getUi().showTable(MapUtils.mapToArray2d(this.getOptions()), new String[]{"Key", "Value"}, "Command Line Options");
        this.setOptions(MapUtils.array2dToMap(ops));
    }

    public void showStartCmds() {
        String[][] table = new String[1][this.startCmds.size()];
        table[0] = this.startCmds.toArray(new String[0]);

        String[][] cmds = Main.instance.getUi().showTable(table, new String[]{"Command"}, "Startup commands for the " + this.name);
        try {
            this.startCmds = !cmds[0][0].equals("null") ? List.of(cmds[0]) : Collections.emptyList();
        } catch (Exception e) {
            this.startCmds = Collections.emptyList();
        }
    }

    public void showStopCmds() {
        String[][] table = new String[1][this.stopCmds.size()];
        table[0] = this.stopCmds.toArray(String[]::new);

        String[][] cmds = Main.instance.getUi().showTable(table, new String[]{"Command"}, "Post-Stop commands for the " + this.name);
        try {
            this.stopCmds = !cmds[0][0].equals("null") ? List.of(cmds[0]) : Collections.emptyList();
        } catch (Exception e) {
            this.stopCmds = Collections.emptyList();
        }
    }

    public String[] startCmds() {
        if (startCmds.isEmpty()) return new String[0];
        return startCmds.stream().filter((str) -> str != null && !str.isBlank() && !str.equals("null")).toArray(String[]::new);
    }

    public String[] stopCmds() {
        if (stopCmds.isEmpty()) return new String[0];
        return stopCmds.stream().filter((str) -> str != null && !str.isBlank() && !str.equals("null")).toArray(String[]::new);
    }

    public void showLogsFrame() {
        LogsFrame logsFrame = this.getLogsFrame();

        if (logsFrame != null) {
            logsFrame.setVisible(true);
            logsFrame.requestFocus();
        } else {
            createLogsFrame();
            this.logsFrame.setVisible(true);
            this.logsFrame.initLogStream();
        }
    }

    public void createLogsFrame() {
        if (this.logsFrame == null) {
            this.logsFrame = new LogsFrame(this);
        }
        this.logs = new StringBuilder();
        logsFrame.stopLogStream();
        logsFrame.initLogStream();
    }

    public void closeLogsFrame() {
        if (logsFrame != null) logsFrame.setVisible(false);
    }

    public void showCmdOptions() {
        new UpdateCmdOptions(this);
    }
}
