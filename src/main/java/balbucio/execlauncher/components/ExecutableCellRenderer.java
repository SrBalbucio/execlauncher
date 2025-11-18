package balbucio.execlauncher.components;

import balbucio.execlauncher.Executor;
import balbucio.execlauncher.model.Executable;

import javax.swing.*;
import java.awt.*;

public class ExecutableCellRenderer implements ListCellRenderer<Executable> {
    @Override
    public Component getListCellRendererComponent(JList<? extends Executable> jList, Executable executable, int i, boolean b, boolean b1) {
        return new ExecutableCard(executable, Executor.getInstance().isActive(executable));
    }
}
