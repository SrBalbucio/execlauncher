package balbucio.execlauncher.action;

import balbucio.execlauncher.Executor;
import balbucio.execlauncher.Main;
import balbucio.execlauncher.model.Executable;
import balbucio.execlauncher.utils.MapUtils;
import de.milchreis.uibooster.model.Form;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.util.Scanner;

public class CreateOrUpdateExecutable {

    private Main main;
    private Executable executable;

    public CreateOrUpdateExecutable() {
        this(new Executable());
    }

    public CreateOrUpdateExecutable(Executable executable) {
        this.executable = executable;
        this.main = Main.instance;
        Form form = main.getUi()
                .createForm("Create or update an executable")
                .addText("Executable Name:")
                .addTextArea("Execution commands (script body):")
                .addLabel("Note: Leave empty if you intend to use a pre-existing script.")
                .addButton("Select workspace path", this::selectWorkspacePath)
                .addButton("Select script file (replaces the script body)", this::selectBatchPath)
                .addButton("Managing environment variables", this::showVars)
                .show();

        executable.setName(form.getByIndex(0).asString());
        String scriptBody = form.getByIndex(1).asString();

        if (scriptBody != null && !scriptBody.isEmpty()) {
            executable.setCmd(scriptBody);
        }

        if (executable.getPath() == null) {
            main.getUi().showErrorDialog("You need to define the workspace folder for the executable. To add it, use the \"Select workspace path\" button.", "Execlauncher cannot create a new executable.");
            return;
        }

        System.out.println(executable);

        if (executable.getName() == null || executable.getCmd() == null || executable.getName().isBlank() || executable.getCmd().isBlank()) {
            main.getUi().showErrorDialog("There is missing data; please check that you entered the name and script correctly.", "Execlauncher cannot create a new executable.");
            return;
        }

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

    public void selectBatchPath() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setFileFilter(new FileNameExtensionFilter("Script Files", "bat", "sh", "txt"));

        if (executable.getPath() != null) {
            chooser.setCurrentDirectory(executable.getFilePath());
        }

        chooser.showOpenDialog(main.getMainFrame());

        if (chooser.getSelectedFile() != null) {
            try {
                StringBuilder builder = new StringBuilder();
                Scanner scanner = new Scanner(chooser.getSelectedFile());
                while (scanner.hasNextLine()) {
                    builder.append(scanner.nextLine()).append("\n");
                }
                scanner.close();
                executable.setCmd(builder.toString());
            } catch (Exception e) {
                main.showError(e);
            }
        }
    }

    public void showVars() {
        String[][] envs = main.getUi().showTable(MapUtils.mapToArray2d(executable.getEnv()), new String[]{"Key", "Value"}, "Environment Variables");
        executable.setEnv(MapUtils.array2dToMap(envs));
    }
}
