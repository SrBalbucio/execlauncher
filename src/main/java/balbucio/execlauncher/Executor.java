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

    public int activeNow() {
        return processes.size();
    }

    public boolean isActive(Executable executable) {
        return this.processes.containsKey(executable) && this.processes.get(executable).isAlive();
    }

    public void start(Executable executable) {
        if (isActive(executable)) return;

        StringBuilder cmd = new StringBuilder(executable.getCmd());

        if (!executable.getOptions().isEmpty()) {
            executable.getOptions().forEach((key, value) -> cmd.append(" \"").append(key).append("=").append(value).append("\""));
        }

        if (!executable.getStartCmds().isEmpty()) {
            ProcessBuilder processBuilder = new ProcessBuilder(executable.startCmds());
            processBuilder.redirectErrorStream(true);
            processBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
            processBuilder.redirectError(ProcessBuilder.Redirect.INHERIT);
            processBuilder.directory(executable.getFilePath());
            processBuilder.environment().putAll(executable.getEnv());
            executor.submit(() -> {
                try {
                    Process exitProcess = processBuilder.start();
                    exitProcess.waitFor();
                    main.getMainFrame().update();
                    postStart(executable, cmd.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                    main.showError(e);
                }
            });
        } else {
            postStart(executable, cmd.toString());
        }
    }

    private void postStart(Executable executable, String cmd) {
        ProcessBuilder processBuilder = new ProcessBuilder(cmd);
        processBuilder.redirectErrorStream(true);
        processBuilder.redirectOutput(ProcessBuilder.Redirect.PIPE);
        processBuilder.redirectError(ProcessBuilder.Redirect.PIPE);
        processBuilder.directory(executable.getFilePath());
        processBuilder.environment().putAll(executable.getEnv());
        executor.submit(() -> {
            try {
                Process process = processBuilder.start();
                if (executable.isAutoShowLogs()) executable.showLogsFrame();
                executable.setOutputWriter(process.outputWriter());
                executable.setErrorStream(process.getErrorStream());
                executable.setInputStream(process.getInputStream());
                this.processes.put(executable, process);
                main.getTray().update();
                main.getMainFrame().update();
                process.waitFor();
                stop(executable);
            } catch (Exception e) {
                e.printStackTrace();
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
        main.getTray().update();

        if (!executable.getStopCmds().isEmpty()) {
            ProcessBuilder processBuilder = new ProcessBuilder(executable.stopCmds());
            processBuilder.redirectErrorStream(true);
            processBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
            processBuilder.redirectError(ProcessBuilder.Redirect.INHERIT);
            processBuilder.directory(executable.getFilePath());
            processBuilder.environment().putAll(executable.getEnv());
            executor.submit(() -> {
                try {
                    Process exitProcess = processBuilder.start();
                    exitProcess.waitFor();
                    main.getMainFrame().update();
                } catch (Exception e) {
                    e.printStackTrace();
                    main.showError(e);
                }
            });
        }
    }

    public void delete(Executable executable) {
        stop(executable);
        this.saved.remove(executable);
        main.getStorage().removeExecutable(executable);
        main.getMainFrame().update();
        main.getTray().update();
    }

    public void startAll() {
        this.saved.forEach(this::start);
    }

    public void stopAll() {
        this.saved.forEach(this::stop);
    }
}
