package balbucio.execlauncher.action;

import balbucio.execlauncher.Main;
import balbucio.execlauncher.model.CmdOptions;
import balbucio.execlauncher.model.Executable;
import de.milchreis.uibooster.model.Form;

public class UpdateCmdOptions {

    private final Main main;
    private final Executable executable;

    public UpdateCmdOptions(Executable executable) {
        this.executable = executable;
        this.main = Main.instance;

        CmdOptions cmdOptions = executable.getCmdOptions();
        if (cmdOptions == null) cmdOptions = new CmdOptions();

        Form form = main.getUi()
                .createForm("Update cmd options")
                .addCheckbox("Delay after command execution?", cmdOptions.isDelayRun())
                .addText("Delay in seconds after command execution.", String.valueOf(cmdOptions.getDelayRunInSecs()))
                .show();

        boolean delayRun = (boolean) form.getByIndex(0).getValue();
        String delayText = form.getByIndex(1).asString();

        int delaySecs;
        try {
            delaySecs = Integer.parseInt(delayText.trim());
        } catch (NumberFormatException ex) {
            main.getUi().showErrorDialog("Invalid delay value: " + delayText, "Invalid input");
            return;
        }

        if (delaySecs < 0) {
            main.getUi().showErrorDialog("The delay cannot be negative.", "Invalid input");
            return;
        }

        if (delaySecs > 600) {
            main.getUi().showErrorDialog("The startup delay is very long and can cause system slowdown.", "Invalid input");
            return;
        }

        cmdOptions.setDelayRun(delayRun);
        cmdOptions.setDelayRunInSecs(delaySecs);
        executable.setCmdOptions(cmdOptions);
        main.getStorage().saveExecutable(executable);
        main.getMainFrame().update();
    }
}