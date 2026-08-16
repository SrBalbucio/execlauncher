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
    private File jarPath;

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

        if (executable.getName() == null || executable.getName().isBlank()) {
            main.getUi().showErrorDialog("There is missing data; please check that you entered the name correctly.", "Execlauncher cannot create a new executable.");
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

        String jarArgument = jarArgument();
        if (jarArgument == null || jarArgument.isBlank()) {
            main.getUi().showErrorDialog("Please select the JAR file.", "Execlauncher cannot create a new executable.");
            return;
        }

        List<String> tokens = new ArrayList<>();
        tokens.add(javaHomeFile.getAbsolutePath());
        tokens.add("-jar");
        tokens.add(jarArgument);
        executable.setCmd(CommandLineUtils.joinQuoted(tokens));

        Executor.getInstance().addExecutable(executable);
    }

    private String jarArgument() {
        if (jarPath != null) {
            Path jarAbsolute = jarPath.toPath().toAbsolutePath().normalize();
            Path workspaceAbsolute = executable.getFilePath().toPath().toAbsolutePath().normalize();
            if (jarAbsolute.startsWith(workspaceAbsolute)) {
                return workspaceAbsolute.relativize(jarAbsolute).toString();
            }
            return jarAbsolute.getFileName().toString();
        }

        List<String> tokens = CommandLineUtils.parse(executable.getCmd());
        int idx = tokens.indexOf("-jar");
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

    public void selectJar() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setFileFilter(new FileNameExtensionFilter("JAR Files", "jar"));

        if (executable.getPath() != null) {
            chooser.setCurrentDirectory(executable.getFilePath());
        }

        chooser.showOpenDialog(main.getMainFrame());

        if (chooser.getSelectedFile() != null) {
            this.jarPath = chooser.getSelectedFile();
            try {
                executable.setCmd(chooser.getSelectedFile().getCanonicalPath());
            } catch (Exception e) {
                main.showError(e);
            }
        }
    }
}