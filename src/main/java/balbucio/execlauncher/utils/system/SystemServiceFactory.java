package balbucio.execlauncher.utils.system;

public class SystemServiceFactory {

    private static WindowsSystemService windows;
    private static final SystemService NOOP = pid -> {
    };

    public static SystemService create() {
        String os = System.getProperty("os.name", "").toLowerCase();
        if (os.contains("win")) {
            return (windows == null ? windows = new WindowsSystemService() : windows);
        }
        return NOOP;
    }
}