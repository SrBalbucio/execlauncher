package balbucio.execlauncher;

import balbucio.execlauncher.model.Executable;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Getter;
import lombok.Setter;
import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;

import java.util.UUID;
import java.util.Vector;
import java.util.stream.Collectors;

public class Storage {

    @Getter
    @Setter
    private static Storage instance;

    private final MVStore mvStore;
    private final Gson gson;
    private final MVMap<String, String> settings;
    private final MVMap<UUID, String> executables;

    public Storage() {
        setInstance(this);
        MVStore.Builder builder = new MVStore.Builder()
                .fileName(Main.DB_PATH.getAbsolutePath())
                .compress()
                .autoCommitDisabled()
                .autoCompactFillRate(90)
                .pageSplitSize(65536);

        this.gson = new GsonBuilder()
                .excludeFieldsWithoutExposeAnnotation()
                .enableComplexMapKeySerialization()
                .serializeNulls()
                .create();

        this.mvStore = builder.open();
        this.settings = mvStore.openMap("settings");
        this.executables = mvStore.openMap("executables");
    }

    public Vector<Executable> executables() {
        return executables.values().stream().map((str) -> gson.fromJson(str, Executable.class)).collect(Collectors.toCollection(Vector::new));
    }

    public void saveExecutable(Executable executable) {
        if (executable == null) throw new NullPointerException("Executable is null!");
        executables.put(executable.getId(), gson.toJson(executable));
        mvStore.commit();
    }

    public void removeExecutable(Executable executable) {
        if (executable == null) throw new NullPointerException("Executable is null!");
        executables.remove(executable.getId());
        mvStore.commit();
    }

    public String getSetting(String key, String defaultValue) {
        return settings.getOrDefault(key, defaultValue);
    }

    public void setSetting(String key, String value) {
        settings.put(key, value);
    }

    public String toJSON(Executable executable) {
        return gson.toJson(executable);
    }

    public Executable importFromJSON(String json) {
        Executable executable =  gson.fromJson(json, Executable.class);
        saveExecutable(executable);
        return executable;
    }
}
