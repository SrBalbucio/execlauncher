package balbucio.execlauncher.action;

import balbucio.execlauncher.Executor;
import balbucio.execlauncher.Main;
import balbucio.execlauncher.model.Executable;
import balbucio.execlauncher.utils.JavaUtils;
import balbucio.execlauncher.utils.MapUtils;
import de.milchreis.uibooster.model.Form;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;

public class CreateOrUpdateJavaExecutable {

    private final Main main;
    private final Executable executable;

    public CreateOrUpdateJavaExecutable() {
        this(new Executable());
    }

    public CreateOrUpdateJavaExecutable(Executable executable) {
        this.executable = executable;
        this.main = Main.instance;

        Form form = main.getUi()
                .createForm("Create or update an executable")
                .addText("Executable Name:")
                .addSelection("Select Java Home:", new ArrayList<>(JavaUtils.getJavaAvailable().values()))
                .addButton("Select workspace path", this::selectWorkspacePath)
                .addButton("Select JAR File", this::selectJar)
                .addButton("Manage environment variables", executable::showVars)
                .addButton("Manage command line options", executable::showOptions)
                .show();

        executable.setName(form.getByIndex(0).asString());
        String javaHome = form.getByIndex(1).asString();

        if (javaHome == null) {
            javaHome = System.getProperty("java.home");
        }

        if (executable.getName() == null || executable.getCmd() == null || executable.getName().isBlank() || executable.getCmd().isBlank()) {
            main.getUi().showErrorDialog("There is missing data; please check that you entered the name and script correctly.", "Execlauncher cannot create a new executable.");
            return;
        }

        File javaHomeFile = new File(javaHome + "/bin/java.exe");

        if (!javaHomeFile.exists()) {
            main.getUi().showErrorDialog("Java Home " + javaHome + " does not have java.exe available. Please verify that the Java version listed is higher than 8 and is not the version included in installable applications.", "Execlauncher cannot create a new executable.");
            return;
        }

        String jarFile = executable.getCmd();
        StringBuilder cmd = new StringBuilder();

        cmd.append("\"").append(javaHomeFile.getAbsolutePath()).append("\"").append(" ").append("-jar").append(" \"").append(jarFile.replace(executable.getPath() + File.pathSeparator, "")).append("\"");

        executable.setCmd(cmd.toString());

        System.out.println(executable);
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
