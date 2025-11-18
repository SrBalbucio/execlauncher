package balbucio.execlauncher.model;

import balbucio.execlauncher.Main;
import balbucio.execlauncher.ui.LogsFrame;
import balbucio.execlauncher.utils.MapUtils;
import com.google.gson.annotations.Expose;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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
    private String type;
    private BufferedWriter outputWriter;
    private InputStream errorStream;
    private InputStream inputStream;
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
        System.out.println(Arrays.deepToString(envs));
        this.setEnv(MapUtils.array2dToMap(envs));
    }

    public void showOptions() {
        String[][] ops = Main.instance.getUi().showTable(MapUtils.mapToArray2d(this.getOptions()), new String[]{"Key", "Value"}, "Command Line Options");
        this.setOptions(MapUtils.array2dToMap(ops));
    }
}
