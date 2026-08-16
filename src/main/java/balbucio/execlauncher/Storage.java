package balbucio.execlauncher;

import balbucio.execlauncher.model.Executable;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;

import java.util.Objects;
import java.util.UUID;
import java.util.Vector;
import java.util.stream.Collectors;

@Slf4j
public class Storage {

    @Getter
    @Setter
    private static Storage instance;

    private final MVStore mvStore;
    private final Gson gson;
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
        this.executables = mvStore.openMap("executables");
    }

    public Vector<Executable> executables() {
        return executables.values().stream()
                .map(this::deserialize)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(Vector::new));
    }

    private Executable deserialize(String json) {
        try {
            return gson.fromJson(json, Executable.class);
        } catch (JsonSyntaxException e) {
            log.error("Failed to deserialize an executable entry, skipping it", e);
            return null;
        }
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

    public String toJSON(Executable executable) {
        return gson.toJson(executable);
    }

    public Executable importFromJSON(String json) {
        if (json == null || json.isBlank()) {
            throw new IllegalArgumentException("The provided JSON is null or empty.");
        }

        Executable executable;
        try {
            executable = gson.fromJson(json, Executable.class);
        } catch (JsonSyntaxException e) {
            throw new IllegalArgumentException("The provided JSON is invalid.", e);
        }

        if (executable == null) {
            throw new IllegalArgumentException("The provided JSON is invalid.");
        }

        if (executable.getId() == null) {
            executable.setId(UUID.randomUUID());
        }

        saveExecutable(executable);
        return executable;
    }

    public void close() {
        mvStore.commit();
        mvStore.close();
    }
}