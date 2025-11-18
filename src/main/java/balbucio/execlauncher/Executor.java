package balbucio.execlauncher;

import balbucio.execlauncher.model.Executable;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Getter
@Setter
public class Executor {

    @Getter
    @Setter
    private static Executor instance;

    private final Main main;
    private final List<Executable> saved;
    private Map<Executable, Process> processes = new ConcurrentHashMap<>();
    private ScheduledExecutorService executor;

    public Executor(Main main) {
        setInstance(this);
        this.main = main;
        this.saved = new CopyOnWriteArrayList<>(main.getStorage().executables());
        this.executor = Executors.newSingleThreadScheduledExecutor();
    }

    public void addExecutable(Executable executable) {
        this.saved.add(executable);
        this.main.getStorage().saveExecutable(executable);
        this.main.getMainFrame().update();
    }

    public boolean isActive(Executable executable) {
        return this.processes.containsKey(executable) && this.processes.get(executable).isAlive();
    }

    public void start(Executable executable) {
        StringBuilder cmd = new StringBuilder(executable.getCmd());

        if (!executable.getOptions().isEmpty()) {
            executable.getOptions().forEach((key, value) -> cmd.append(" \"").append(key).append("=").append(value).append("\""));
//            cmd.deleteCharAt(cmd.length() - 1);
        }

        System.out.println(cmd.toString());
        ProcessBuilder processBuilder = new ProcessBuilder(cmd.toString());
        processBuilder.redirectErrorStream(true);
        processBuilder.redirectOutput(ProcessBuilder.Redirect.PIPE);
        processBuilder.redirectError(ProcessBuilder.Redirect.PIPE);
        processBuilder.directory(executable.getFilePath());
        processBuilder.environment().putAll(executable.getEnv());
        executor.submit(() -> {
            try {
                Process process = processBuilder.start();
                executable.setOutputWriter(process.outputWriter());
                executable.setErrorStream(process.getErrorStream());
                executable.setInputStream(process.getInputStream());
                this.processes.put(executable, process);
                main.getMainFrame().update();
                process.waitFor();
                stop(executable);
            } catch (Exception e) {
                main.showError(e);
            }
        });
    }

    public void stop(Executable executable) {
        Process process = this.processes.get(executable);
        if (process != null && process.isAlive()) process.destroy();
        executable.setOutputWriter(null);
        executable.setErrorStream(null);
        executable.setInputStream(null);
        this.processes.remove(executable);
        main.getMainFrame().update();
    }

    public void delete(Executable executable) {
        stop(executable);
        this.saved.remove(executable);
        main.getStorage().removeExecutable(executable);
        main.getMainFrame().update();
    }
}
