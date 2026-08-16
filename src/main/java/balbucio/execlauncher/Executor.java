package balbucio.execlauncher;

import balbucio.execlauncher.model.CmdOptions;
import balbucio.execlauncher.model.Executable;
import balbucio.execlauncher.utils.CommandLineUtils;
import balbucio.execlauncher.utils.system.SystemServiceFactory;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Getter
@Setter
@Slf4j
public class Executor {

    @Getter
    @Setter
    private static Executor instance;

    private final Main main;
    private final List<Executable> saved;
    private Map<Executable, Process> processes = new ConcurrentHashMap<>();
    private Map<Executable, Process> startupProcesses = new ConcurrentHashMap<>();
    private Map<Executable, Thread> threads = new ConcurrentHashMap<>();

    public Executor(Main main) {
        setInstance(this);
        this.main = main;
        this.saved = new CopyOnWriteArrayList<>(main.getStorage().executables());
    }

    public void addExecutable(Executable executable) {
        this.saved.add(executable);
        this.main.getStorage().saveExecutable(executable);
        this.updateUI();
    }

    public int activeNow() {
        return processes.size();
    }

    public boolean isActive(Executable executable) {
        Process process = this.processes.get(executable);
        if (process != null && process.isAlive()) return true;
        Process startup = this.startupProcesses.get(executable);
        return startup != null && startup.isAlive();
    }

    public void init(Executable executable) {
        if (isActive(executable)) return;

        List<String> cmd = buildCommand(executable);
        if (cmd.isEmpty()) {
            main.showError(new IllegalArgumentException("Empty command for executable: " + executable.getName()));
            return;
        }

        Runnable launch = () -> {
            try {
                applyDelay(executable);
                postInit(executable, cmd);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                log.error("Failed to start {}", executable.getName(), e);
                main.showError(e);
            }
        };

        Runnable task = executable.getStartCmds().isEmpty()
                ? launch
                : () -> {
                    try {
                        runStartCommands(executable, executable.startCmds());
                        launch.run();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } catch (Exception e) {
                        log.error("Failed to run start commands for {}", executable.getName(), e);
                        main.showError(e);
                    }
                };

        Thread thread = new Thread(task, "Execlauncher-Start-" + executable.getName());
        thread.setDaemon(false);
        thread.start();
        threads.put(executable, thread);
    }

    private List<String> buildCommand(Executable executable) {
        List<String> cmd = CommandLineUtils.parse(executable.getCmd());
        executable.getOptions().forEach((key, value) -> cmd.add(key + "=" + value));
        return cmd;
    }

    private void applyDelay(Executable executable) throws InterruptedException {
        CmdOptions cmdOptions = executable.getCmdOptions();
        if (cmdOptions != null && cmdOptions.isDelayRun() && cmdOptions.getDelayRunInSecs() > 0) {
            Thread.sleep(Duration.ofSeconds(cmdOptions.getDelayRunInSecs()));
        }
    }

    private void runStartCommands(Executable executable, String[] cmds) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder(cmds);
        processBuilder.redirectErrorStream(true);
        processBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
        processBuilder.redirectError(ProcessBuilder.Redirect.INHERIT);
        processBuilder.directory(executable.getFilePath());
        processBuilder.environment().putAll(executable.getEnv());
        Process process = processBuilder.start();
        startupProcesses.put(executable, process);
        try {
            process.waitFor();
        } finally {
            startupProcesses.remove(executable);
        }
    }

    private void postInit(Executable executable, List<String> cmd) {
        ProcessBuilder processBuilder = new ProcessBuilder(cmd);
        processBuilder.redirectErrorStream(true);
        processBuilder.redirectOutput(ProcessBuilder.Redirect.PIPE);
        processBuilder.redirectError(ProcessBuilder.Redirect.PIPE);
        processBuilder.directory(executable.getFilePath());
        processBuilder.environment().putAll(executable.getEnv());
        try {
            Process process = processBuilder.start();
            executable.setOutputWriter(process.outputWriter());
            executable.setErrorStream(process.getErrorStream());
            executable.setInputStream(process.getInputStream());
            this.processes.put(executable, process);

            SwingUtilities.invokeLater(() -> {
                executable.createLogsFrame();
                if (executable.isAutoShowLogs()) executable.showLogsFrame();
                updateUI();
            });

            process.waitFor();
            stop(executable, process);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.error("Failed to run {}", executable.getName(), e);
            main.showError(e);
        }
    }

    public void stop(Executable executable) {
        stop(executable, this.processes.get(executable));
    }

    private void stop(Executable executable, Process process) {
        Process running = this.processes.get(executable);

        Process startup = this.startupProcesses.get(executable);
        if (startup != null && startup.isAlive()) {
            startup.destroyForcibly();
            startupProcesses.remove(executable);
        }

        if (running == null) {
            Thread thread = this.threads.remove(executable);
            if (thread != null) thread.interrupt();
            cleanupStreams(executable);
            updateUI();
            return;
        }

        if (process != null && running != process) {
            return;
        }

        boolean wasAlive = running.isAlive();
        if (wasAlive) {
            ProcessHandle handle = running.toHandle();
            handle.descendants().forEach(ProcessHandle::destroyForcibly);
            SystemServiceFactory.create().closeAllProcesses(handle.pid());
            running.destroy();
        }

        Thread thread = this.threads.remove(executable);
        if (thread != null && !wasAlive) {
            thread.interrupt();
        }

        cleanupStreams(executable);

        updateUI();

        if (!executable.getStopCmds().isEmpty()) {
            runCommandAsync(executable, executable.stopCmds());
        }
    }

    private void cleanupStreams(Executable executable) {
        if (executable.getLogsFrame() != null) executable.getLogsFrame().stopLogStream();
        executable.setOutputWriter(null);
        executable.setErrorStream(null);
        executable.setInputStream(null);
    }

    private void runCommandAsync(Executable executable, String[] cmds) {
        Thread thread = new Thread(() -> {
            try {
                runCommand(executable, cmds);
            } catch (Exception e) {
                log.error("Failed to run commands for {}", executable.getName(), e);
                main.showError(e);
            }
        }, "Execlauncher-Command-" + executable.getName());
        thread.setDaemon(true);
        thread.start();
    }

    private void runCommand(Executable executable, String[] cmds) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder(cmds);
        processBuilder.redirectErrorStream(true);
        processBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
        processBuilder.redirectError(ProcessBuilder.Redirect.INHERIT);
        processBuilder.directory(executable.getFilePath());
        processBuilder.environment().putAll(executable.getEnv());
        Process process = processBuilder.start();
        process.waitFor();
    }

    private void updateUI() {
        SwingUtilities.invokeLater(() -> {
            main.getMainFrame().update();
            main.getTray().update();
        });
    }

    public void delete(Executable executable) {
        stop(executable);
        this.saved.remove(executable);
        main.getStorage().removeExecutable(executable);
        updateUI();
    }

    public void startAll() {
        this.saved.forEach(this::init);
    }

    public void stopAll() {
        this.saved.forEach(this::stop);
    }

}