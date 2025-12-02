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

        // se o executável for de versão anterior
        if(cmdOptions == null) cmdOptions = new CmdOptions();

        Form form = main.getUi()
                .createForm("Update cmd options")
                .addCheckbox("Delay after command execution?", cmdOptions.isDelayRun())
                .addText("Delay in seconds after command execution.", String.valueOf(cmdOptions.getDelayRunInSecs()))
                .show();

        boolean delayRun = (boolean) form.getByIndex(0).getValue();
        int delaySecs = Integer.parseInt(form.getByIndex(1).asString());

        try {
            if (delaySecs > 600)
                throw new RuntimeException("The startup delay is very long and can cause system slowdown.");

            cmdOptions.setDelayRun(delayRun);
            cmdOptions.setDelayRunInSecs(delaySecs);
            executable.setCmdOptions(cmdOptions);
            main.getStorage().saveExecutable(executable);
            main.getMainFrame().update();
        } catch (Exception e) {
            main.showError(e);
        }
    }


}
