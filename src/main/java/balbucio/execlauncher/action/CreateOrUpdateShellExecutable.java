package balbucio.execlauncher.action;

import balbucio.execlauncher.Executor;
import balbucio.execlauncher.Main;
import balbucio.execlauncher.model.Executable;
import balbucio.execlauncher.utils.CommandLineUtils;
import balbucio.execlauncher.utils.FileUtils;
import balbucio.execlauncher.utils.JavaUtils;
import de.milchreis.uibooster.model.Form;

import javax.swing.*;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class CreateOrUpdateShellExecutable {

    private final Main main;
    private final Executable executable;
    private File scriptFile;

    public CreateOrUpdateShellExecutable() {
        this(new Executable());
    }

    public CreateOrUpdateShellExecutable(Executable executable) {
        this.executable = executable;
        this.main = Main.instance;
        if (executable.getScriptFile() != null && !executable.getScriptFile().isBlank()) {
            this.scriptFile = new File(executable.getScriptFile());
        }

        String currentCmd = executable.getCmd() != null ? executable.getCmd() : "";

        Form form = main.getUi()
                .createForm("Create or update a Shell executable")
                .addText("Executable Name:", executable.getName() != null ? executable.getName() : "")
                .addText("Command:", currentCmd)
                .addButton("Select workspace path", this::selectWorkspacePath)
                .addButton("Select script file (.sh, .bat, .cmd, .ps1)", this::selectScriptFile)
                .addButton("Manage environment variables", executable::showVars)
                .addButton("Manage command line options", executable::showOptions)
                .show();

        String name = form.getByIndex(0).asString();
        String command = form.getByIndex(1).asString();

        executable.setName(name);

        if (name == null || name.isBlank()) {
            main.getUi().showErrorDialog("There is missing data; please check that you entered the name correctly.", "Execlauncher cannot create a new executable.");
            return;
        }

        if (executable.getPath() == null || executable.getPath().isBlank()) {
            main.getUi().showErrorDialog("Please select the workspace path.", "Execlauncher cannot create a new executable.");
            return;
        }

        // If user selected a script file and command is empty, build command from script
        if ((command == null || command.isBlank()) && scriptFile != null) {
            command = buildCommandForScript(scriptFile);
        } else if (scriptFile != null && (command == null || command.isBlank() || command.equals(currentCmd))) {
            // script was selected but command was not edited -> prefer script-based command
            // keep existing check above, already handled
        }

        // If still empty and script selected, fallback
        if ((command == null || command.isBlank()) && scriptFile != null) {
            command = buildCommandForScript(scriptFile);
        }

        if (command == null || command.isBlank()) {
            main.getUi().showErrorDialog("Please inform the command or select a script file.", "Execlauncher cannot create a new executable.");
            return;
        }

        executable.setCmd(command);
        executable.setType("SHELL");
        if (scriptFile != null) {
            executable.setScriptFile(scriptFile.getAbsolutePath());
        }

        Executor.getInstance().addExecutable(executable);
    }

    private String buildCommandForScript(File script) {
        if (script == null) return null;
        String scriptName = script.getName().toLowerCase();
        String absolute = script.getAbsolutePath();
        // Prefer relative path if script is inside workspace
        try {
            if (executable.getPath() != null && !executable.getPath().isBlank()) {
                Path workspace = new File(executable.getPath()).toPath().toAbsolutePath().normalize();
                Path scriptPath = script.toPath().toAbsolutePath().normalize();
                if (scriptPath.startsWith(workspace)) {
                    String rel = workspace.relativize(scriptPath).toString();
                    // Use relative for portability
                    absolute = rel;
                }
            }
        } catch (Exception ignored) {
        }

        List<String> tokens = new ArrayList<>();

        if (scriptName.endsWith(".sh")) {
            if (JavaUtils.isWindows()) {
                File bash = FileUtils.findOnPath("bash");
                String bashCmd = bash != null ? bash.getAbsolutePath() : "bash";
                tokens.add(bashCmd);
                tokens.add(absolute);
            } else {
                File bash = FileUtils.findOnPath("bash");
                String bashCmd = bash != null ? bash.getAbsolutePath() : "bash";
                tokens.add(bashCmd);
                tokens.add(absolute);
            }
        } else if (scriptName.endsWith(".bat") || scriptName.endsWith(".cmd")) {
            if (JavaUtils.isWindows()) {
                tokens.add("cmd.exe");
                tokens.add("/c");
                tokens.add(absolute);
            } else {
                // On Unix, try to run via cmd-like fallback: just exec via bash if possible
                tokens.add(absolute);
            }
        } else if (scriptName.endsWith(".ps1")) {
            if (JavaUtils.isWindows()) {
                File pwsh = FileUtils.findOnPath("pwsh");
                if (pwsh == null) pwsh = FileUtils.findOnPath("powershell");
                String shell = pwsh != null ? pwsh.getAbsolutePath() : "powershell.exe";
                tokens.add(shell);
                tokens.add("-ExecutionPolicy");
                tokens.add("Bypass");
                tokens.add("-File");
                tokens.add(absolute);
            } else {
                File pwsh = FileUtils.findOnPath("pwsh");
                String shell = pwsh != null ? pwsh.getAbsolutePath() : "pwsh";
                tokens.add(shell);
                tokens.add("-File");
                tokens.add(absolute);
            }
        } else {
            // Generic file or executable: run directly
            // If executable bit not set on Unix, user can prefix manually
            tokens.add(absolute);
        }

        return CommandLineUtils.joinQuoted(tokens);
    }

    public void selectWorkspacePath() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (executable.getPath() != null) {
            chooser.setCurrentDirectory(executable.getFilePath());
        }
        chooser.showOpenDialog(main.getMainFrame());
        if (chooser.getSelectedFile() != null) {
            executable.setPath(chooser.getSelectedFile().getAbsolutePath());
        }
    }

    public void selectScriptFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        if (executable.getPath() != null) {
            chooser.setCurrentDirectory(executable.getFilePath());
        }
        chooser.showOpenDialog(main.getMainFrame());
        if (chooser.getSelectedFile() != null) {
            this.scriptFile = chooser.getSelectedFile();
            // Auto-fill workspace if not set
            if (executable.getPath() == null || executable.getPath().isBlank()) {
                File parent = scriptFile.getParentFile();
                if (parent != null) executable.setPath(parent.getAbsolutePath());
            }
            // Give feedback via dialog? UiBooster button callback has no text field reference to update.
            // We store the file and build command after form; also set a provisional cmd for preview.
            try {
                String preview = buildCommandForScript(scriptFile);
                executable.setCmd(preview);
            } catch (Exception e) {
                main.showError(e);
            }
        }
    }
}
