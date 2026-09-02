package balbucio.execlauncher.action;

import balbucio.execlauncher.Executor;
import balbucio.execlauncher.Main;
import balbucio.execlauncher.model.Executable;
import balbucio.execlauncher.utils.CommandLineUtils;
import de.milchreis.uibooster.model.Form;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class CreateOrUpdateCmdExecutable {

    private final Main main;
    private final Executable executable;
    private File scriptFile;

    public CreateOrUpdateCmdExecutable() {
        this(new Executable());
    }

    public CreateOrUpdateCmdExecutable(Executable executable) {
        this.executable = executable;
        this.main = Main.instance;

        Form form = main.getUi()
                .createForm("Create or update a CMD executable")
                .addText("Executable Name:", executable.getName() != null ? executable.getName() : "")
                .addText("CMD command / args:", currentCommand() != null ? currentCommand() : "")
                .addButton("Select workspace path", this::selectWorkspacePath)
                .addButton("Select CMD/BAT Script", this::selectScript)
                .addButton("Manage environment variables", executable::showVars)
                .addButton("Manage command line options", executable::showOptions)
                .show();

        executable.setName(form.getByIndex(0).asString());
        String command = form.getByIndex(1).asString();

        if (executable.getName() == null || executable.getName().isBlank()) {
            main.getUi().showErrorDialog("There is missing data; please check that you entered the name correctly.", "Execlauncher cannot create a new executable.");
            return;
        }

        if (executable.getPath() == null || executable.getPath().isBlank()) {
            main.getUi().showErrorDialog("Please select the workspace path.", "Execlauncher cannot create a new executable.");
            return;
        }

        List<String> tokens = new ArrayList<>();

        if (scriptFile != null) {
            Path scriptAbsolute = scriptFile.toPath().toAbsolutePath().normalize();
            Path workspaceAbsolute = executable.getFilePath().toPath().toAbsolutePath().normalize();
            String scriptArg;
            if (scriptAbsolute.startsWith(workspaceAbsolute)) {
                scriptArg = workspaceAbsolute.relativize(scriptAbsolute).toString();
            } else {
                scriptArg = scriptAbsolute.toString();
            }
            tokens.add("cmd.exe");
            tokens.add("/c");
            tokens.add(scriptArg);
            if (command != null && !command.isBlank()) {
                tokens.addAll(CommandLineUtils.parse(command));
            }
        } else {
            if (command == null || command.isBlank()) {
                main.getUi().showErrorDialog("Please inform the cmd command or select a script file.", "Execlauncher cannot create a new executable.");
                return;
            }
            List<String> parsed = CommandLineUtils.parse(command);
            boolean alreadyCmd = !parsed.isEmpty() && parsed.get(0).equalsIgnoreCase("cmd.exe");
            if (alreadyCmd) {
                tokens.addAll(parsed);
            } else {
                tokens.add("cmd.exe");
                tokens.add("/c");
                tokens.add(command);
            }
        }

        executable.setType("CMD");
        executable.setCmd(CommandLineUtils.joinQuoted(tokens));

        Executor.getInstance().addExecutable(executable);
    }

    private String currentCommand() {
        if (executable.getCmd() == null || executable.getCmd().isBlank()) return null;
        List<String> tokens = CommandLineUtils.parse(executable.getCmd());
        if (tokens.isEmpty()) return null;
        if (tokens.get(0).equalsIgnoreCase("cmd.exe")) {
            int cIdx = -1;
            for (int i = 0; i < tokens.size(); i++) {
                if (tokens.get(i).equalsIgnoreCase("/c")) { cIdx = i; break; }
            }
            if (cIdx >= 0 && cIdx + 1 < tokens.size()) {
                return String.join(" ", tokens.subList(cIdx + 1, tokens.size()));
            } else if (tokens.size() > 1) {
                return String.join(" ", tokens.subList(1, tokens.size()));
            }
        }
        return executable.getCmd();
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

    public void selectScript() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setFileFilter(new FileNameExtensionFilter("Batch Files", "bat", "cmd"));
        if (executable.getPath() != null) {
            chooser.setCurrentDirectory(executable.getFilePath());
        }
        chooser.showOpenDialog(main.getMainFrame());
        if (chooser.getSelectedFile() != null) {
            this.scriptFile = chooser.getSelectedFile();
            try {
                executable.setCmd(chooser.getSelectedFile().getCanonicalPath());
            } catch (Exception e) {
                main.showError(e);
            }
        }
    }
}
