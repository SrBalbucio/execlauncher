package balbucio.execlauncher.action;

import balbucio.execlauncher.Executor;
import balbucio.execlauncher.Main;
import balbucio.execlauncher.model.Executable;
import balbucio.execlauncher.utils.CommandLineUtils;
import balbucio.execlauncher.utils.JavaUtils;
import de.milchreis.uibooster.model.Form;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class CreateOrUpdateJavaExecutable {

    private final Main main;
    private final Executable executable;

    public CreateOrUpdateJavaExecutable() {
        this(new Executable());
    }

    public CreateOrUpdateJavaExecutable(Executable executable) {
        this.executable = executable;
        this.main = Main.instance;

        List<String> javaHomes = new ArrayList<>(JavaUtils.getJavaAvailable().values());
        if (javaHomes.isEmpty()) {
            javaHomes.add(System.getProperty("java.home"));
        }

        Form form = main.getUi()
                .createForm("Create or update an executable")
                .addText("Executable Name:", executable.getName() != null ? executable.getName() : "")
                .addSelection("Select Java Home:", javaHomes)
                .addButton("Select workspace path", this::selectWorkspacePath)
                .addButton("Select JAR File", this::selectJar)
                .addButton("Manage environment variables", executable::showVars)
                .addButton("Manage command line options", executable::showOptions)
                .show();

        executable.setName(form.getByIndex(0).asString());
        String javaHome = form.getByIndex(1).asString();

        if (javaHome == null || javaHome.isBlank()) {
            javaHome = System.getProperty("java.home");
        }

        if (executable.getName() == null || executable.getName().isBlank()
                || executable.getCmd() == null || executable.getCmd().isBlank()) {
            main.getUi().showErrorDialog("There is missing data; please check that you entered the name and script correctly.", "Execlauncher cannot create a new executable.");
            return;
        }

        if (executable.getPath() == null || executable.getPath().isBlank()) {
            main.getUi().showErrorDialog("Please select the workspace path.", "Execlauncher cannot create a new executable.");
            return;
        }

        File javaHomeFile = new File(javaHome, "bin/" + JavaUtils.javaExecutableName());

        if (!javaHomeFile.exists()) {
            main.getUi().showErrorDialog("Java Home " + javaHome + " does not have " + javaHomeFile.getName() + " available. Please verify that the Java version listed is higher than 8 and is not the version included in installable applications.", "Execlauncher cannot create a new executable.");
            return;
        }

        List<String> tokens = new ArrayList<>();
        tokens.add(javaHomeFile.getAbsolutePath());
        tokens.add("-jar");
        tokens.add(jarArgument());
        executable.setCmd(CommandLineUtils.joinQuoted(tokens));

        Executor.getInstance().addExecutable(executable);
    }

    private String jarArgument() {
        Path jarPath = Path.of(executable.getCmd()).toAbsolutePath().normalize();
        Path workspacePath = executable.getFilePath().toPath().toAbsolutePath().normalize();

        if (jarPath.startsWith(workspacePath)) {
            return workspacePath.relativize(jarPath).toString();
        }
        return jarPath.getFileName().toString();
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

    public void selectJar() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setFileFilter(new FileNameExtensionFilter("JAR Files", "jar"));

        if (executable.getPath() != null) {
            chooser.setCurrentDirectory(executable.getFilePath());
        }

        chooser.showOpenDialog(main.getMainFrame());

        if (chooser.getSelectedFile() != null) {
            try {
                executable.setCmd(chooser.getSelectedFile().getCanonicalPath());
            } catch (Exception e) {
                main.showError(e);
            }
        }
    }
}