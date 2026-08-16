package balbucio.execlauncher.settings;

import com.formdev.flatlaf.intellijthemes.FlatCyanLightIJTheme;
import com.formdev.flatlaf.intellijthemes.FlatGradiantoMidnightBlueIJTheme;
import com.formdev.flatlaf.intellijthemes.FlatSpacegrayIJTheme;

import javax.swing.*;
import java.awt.*;
import java.util.function.Supplier;

public enum AppTheme {
    SPACEGRAY("Spacegray", FlatSpacegrayIJTheme::new),
    CYAN_LIGHT("Cyan Light", FlatCyanLightIJTheme::new),
    MIDNIGHT_BLUE("Midnight Blue", FlatGradiantoMidnightBlueIJTheme::new);

    private final String displayName;
    private final Supplier<LookAndFeel> factory;

    AppTheme(String displayName, Supplier<LookAndFeel> factory) {
        this.displayName = displayName;
        this.factory = factory;
    }

    public LookAndFeel create() {
        return factory.get();
    }

    public String getDisplayName() {
        return displayName;
    }

    public void install() {
        try {
            UIManager.setLookAndFeel(create());
        } catch (UnsupportedLookAndFeelException e) {
            throw new IllegalStateException(e);
        }
    }

    public static AppTheme fromName(String name) {
        for (AppTheme theme : values()) {
            if (theme.name().equalsIgnoreCase(name)) return theme;
        }
        return SPACEGRAY;
    }

    public static AppTheme fromDisplay(String displayName) {
        for (AppTheme theme : values()) {
            if (theme.displayName.equals(displayName)) return theme;
        }
        return SPACEGRAY;
    }

    public static String[] displayNames() {
        String[] names = new String[values().length];
        for (int i = 0; i < values().length; i++) {
            names[i] = values()[i].displayName;
        }
        return names;
    }
}