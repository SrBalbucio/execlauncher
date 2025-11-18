package balbucio.execlauncher;

import balbucio.execlauncher.model.Executable;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Executor {

    @Getter
    @Setter
    private static Executor instance;

    private Main main;
    private List<Executable> active = new CopyOnWriteArrayList<>();
    private List<Executable> saved;

    public Executor(Main main) {
        setInstance(this);
        this.main = main;
        this.saved = new CopyOnWriteArrayList<>(main.getStorage().executables());
    }

    public void addExecutable(Executable executable) {
        this.saved.add(executable);
        this.main.getStorage().saveExecutable(executable);
        this.main.getMainFrame().update();
    }

    public boolean isActive(Executable executable) {
        return this.active.contains(executable);
    }

    public void start(Executable executable) {
        this.active.add(executable);
    }

    public void stop(Executable executable) {

    }

    public void delete(Executable executable) {
        stop(executable);
        main.getStorage().removeExecutable(executable);
        main.getMainFrame().update();
    }
}
