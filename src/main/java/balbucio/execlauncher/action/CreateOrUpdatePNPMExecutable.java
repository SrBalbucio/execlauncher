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

        Form form = main.getUi()
                .createForm("Create or update an executable")
                .addText("Executable Name:", executable.getName() != null ? executable.getName() : "")
                .addText("Script Name:", "dev")
                .addButton("Select workspace path", this::selectWorkspacePath)
                .addButton("Manage environment variables", executable::showVars)
                .addButton("Manage command line options", executable::showOptions)
                .show();

        executable.setName(form.getByIndex(0).asString());
        String scriptName = form.getByIndex(1).asString();

        if (executable.getName() == null || executable.getName().isBlank()) {
            main.getUi().showErrorDialog("There is missing data; please check that you entered the name correctly.", "Execlauncher cannot create a new executable.");
            return;
        }

        if (scriptName == null || scriptName.isBlank()) {
            main.getUi().showErrorDialog("Please enter the script name.", "Execlauncher cannot create a new executable.");
            return;
        }

        if (executable.getPath() == null || executable.getPath().isBlank()) {
            main.getUi().showErrorDialog("Please select the workspace path.", "Execlauncher cannot create a new executable.");
            return;
        }

        File pnpmFile = FileUtils.findOnPath("pnpm");
        if (pnpmFile == null) {
            pnpmFile = findLocalAppDataPnpm();
        }

        if (pnpmFile == null) {
            JOptionPane.showMessageDialog(null, "PNPM executable not found, install with 'npm install -g pnpm'.", "It was not possible to create the executable.", JOptionPane.ERROR_MESSAGE);
            return;
        }

        List<String> tokens = new ArrayList<>();
        if (pnpmFile.getName().endsWith(".cmd") || pnpmFile.getName().endsWith(".bat")) {
            tokens.add("cmd.exe");
            tokens.add("/c");
        }
        tokens.add(pnpmFile.getAbsolutePath());
        tokens.add("run");
        tokens.add(scriptName);

        executable.setCmd(CommandLineUtils.joinQuoted(tokens));
        Executor.getInstance().addExecutable(executable);
    }

    private File findLocalAppDataPnpm() {
        String localAppData = System.getenv("LOCALAPPDATA");
        if (localAppData == null || localAppData.isBlank()) return null;

        File pnpm = new File(localAppData, "pnpm/pnpm.exe");
        return pnpm.isFile() ? pnpm : null;
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