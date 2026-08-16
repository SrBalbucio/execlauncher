package balbucio.execlauncher;

import balbucio.execlauncher.settings.AppTheme;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class AppThemeTest {

    @Test
    void fromNameResolvesExactTheme() {
        assertEquals(AppTheme.MIDNIGHT_BLUE, AppTheme.fromName("MIDNIGHT_BLUE"));
        assertEquals(AppTheme.CYAN_LIGHT, AppTheme.fromName("cyan_light"));
    }

    @Test
    void fromNameFallsBackToDefault() {
        assertEquals(AppTheme.SPACEGRAY, AppTheme.fromName("unknown-theme"));
        assertEquals(AppTheme.SPACEGRAY, AppTheme.fromName(null));
    }

    @Test
    void fromDisplayResolvesTheme() {
        assertEquals(AppTheme.SPACEGRAY, AppTheme.fromDisplay("Spacegray"));
        assertEquals(AppTheme.CYAN_LIGHT, AppTheme.fromDisplay("Cyan Light"));
    }

    @Test
    void displayNamesCoversAllThemes() {
        assertEquals(AppTheme.values().length, AppTheme.displayNames().length);
    }

    @Test
    void createReturnsFlatLafLookAndFeel() {
        assertInstanceOf(javax.swing.LookAndFeel.class, AppTheme.SPACEGRAY.create());
    }
}