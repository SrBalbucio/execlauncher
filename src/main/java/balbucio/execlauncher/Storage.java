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

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.Vector;
import java.util.stream.Collectors;

@Slf4j
public class Storage {

    public static final String SETTING_THEME = "theme";
    public static final String SETTING_CLOSE_TO_EXIT = "close_to_exit";
    public static final String SETTING_START_WITH_WINDOWS = "start_with_windows";
    private static final String ORDER_KEY = "executable_order";

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
        Map<UUID, Executable> byId = new LinkedHashMap<>();
        executables.values().forEach(json -> {
            Executable executable = deserialize(json);
            if (executable != null) byId.put(executable.getId(), executable);
        });

        Vector<Executable> result = new Vector<>();
        for (String idStr : orderedIds()) {
            try {
                Executable executable = byId.remove(UUID.fromString(idStr));
                if (executable != null) result.add(executable);
            } catch (IllegalArgumentException ignored) {
            }
        }
        result.addAll(byId.values());
        return result;
    }

    private List<String> orderedIds() {
        String raw = settings.get(ORDER_KEY);
        if (raw == null || raw.isBlank()) return List.of();
        return Arrays.asList(raw.split(","));
    }

    public void saveOrder(List<? extends Executable> executables) {
        String joined = executables.stream()
                .map(e -> e.getId().toString())
                .collect(Collectors.joining(","));
        settings.put(ORDER_KEY, joined);
        mvStore.commit();
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

    public String getSetting(String key, String defaultValue) {
        return settings.getOrDefault(key, defaultValue);
    }

    public boolean getBooleanSetting(String key, boolean defaultValue) {
        String value = settings.get(key);
        return value == null ? defaultValue : Boolean.parseBoolean(value);
    }

    public void setSetting(String key, String value) {
        settings.put(key, value);
        mvStore.commit();
    }

    public void setBooleanSetting(String key, boolean value) {
        setSetting(key, String.valueOf(value));
    }

    public void close() {
        mvStore.commit();
        mvStore.close();
    }
}