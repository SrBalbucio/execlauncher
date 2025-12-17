package balbucio.execlauncher.action;

import balbucio.execlauncher.Executor;
import balbucio.execlauncher.Main;
import balbucio.execlauncher.model.Executable;
import balbucio.execlauncher.utils.JavaUtils;
import de.milchreis.uibooster.model.Form;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.util.ArrayList;

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
        StringBuilder cmd = new StringBuilder();

        File appData = new File(new File(System.getenv("APPDATA")).getParentFile(), "Local");
        File pnpmFile = new File(appData, "pnpm/pnpm.exe");

        if(!pnpmFile.exists()) {
            JOptionPane.showMessageDialog(null, "PNPM executable not found, install with 'npm install -g pnpm'.", "It was not possible to create the executable.", JOptionPane.ERROR_MESSAGE);
            return;
        }

        cmd.append("\"").append(pnpmFile.getAbsolutePath()).append("\"")
                .append(" \"run\" \"").append(scriptName).append("\"");

        executable.setCmd(cmd.toString());
        Executor.getInstance().addExecutable(executable);
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
