package balbucio.execlauncher.action;

import balbucio.execlauncher.Executor;
import balbucio.execlauncher.Main;
import balbucio.execlauncher.model.Executable;
import balbucio.execlauncher.utils.CommandLineUtils;
import balbucio.execlauncher.utils.FileUtils;
import de.milchreis.uibooster.model.Form;

import javax.swing.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CreateOrUpdatePNPMExecutable {

    private final Main main;
    private final Executable executable;

    public CreateOrUpdatePNPMExecutable() {
        this(new Executable());
    }

    public CreateOrUpdatePNPMExecutable(Executable executable) {
        this.executable = executable;
        this.main = Main.instance;

        String pnpmPath = FileUtils.findOnPath("pnpm").getAbsolutePath();
        if (pnpmPath == null) {
            pnpmPath = System.getenv("LOCALAPPDATA") + "\\pnpm\\pnpm.cmd";
        }
        if (pnpmPath == null || pnpmPath.isBlank()) {
            pnpmPath = System.getProperty("java.home"); // fails intentionally below
        }

        Form form = main.getUi()
                .createForm("Create or update a PNPM executable")
                .addText("Executable Name:", executable.getName() != null ? executable.getName() : "")
                .addText("Script name:", currentScript() != null ? currentScript() : "")
                .addSelection("Select PNPM Path:", List.of(pnpmPath))
                .addButton("Select workspace path", this::selectWorkspacePath)
                .addButton("Manage environment variables", executable::showVars)
                .addButton("Manage command line options", executable::showOptions)
                .show();

        executable.setName(form.getByIndex(0).asString());
        String scriptName = form.getByIndex(1).asString();
        pnpmPath = form.getByIndex(2).asString();

        if (executable.getName() == null || executable.getName().isBlank()) {
            main.getUi().showErrorDialog("There is missing data; please check that you entered the name correctly.", "Execlauncher cannot create a new executable.");
            return;
        }

        if (scriptName == null || scriptName.isBlank()) {
            main.getUi().showErrorDialog("Please inform the script name.", "Execlauncher cannot create a new executable.");
            return;
        }

        if (executable.getPath() == null || executable.getPath().isBlank()) {
            main.getUi().showErrorDialog("Please select the workspace path.", "Execlauncher cannot create a new executable.");
            return;
        }

        File pnpmFile = new File(pnpmPath);
        if (!pnpmFile.exists()) {
            main.getUi().showErrorDialog("PNPM was not found at " + pnpmPath + ". Please verify your installation.", "Execlauncher cannot create a new executable.");
            return;
        }

        List<String> tokens = new ArrayList<>();
        String program = pnpmPath.toLowerCase().endsWith(".cmd") || pnpmPath.toLowerCase().endsWith(".bat")
                ? "cmd.exe /c " + pnpmPath
                : pnpmPath;
        for (String token : CommandLineUtils.parse(program)) {
            tokens.add(token);
        }
        tokens.add("run");
        tokens.add(scriptName);
        executable.setCmd(CommandLineUtils.joinQuoted(tokens));

        Executor.getInstance().addExecutable(executable);
    }

    private String currentScript() {
        List<String> tokens = CommandLineUtils.parse(executable.getCmd());
        int idx = tokens.indexOf("run");
        if (idx >= 0 && idx + 1 < tokens.size()) {
            return tokens.get(idx + 1);
        }
        return null;
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
}