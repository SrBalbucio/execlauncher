package balbucio.execlauncher.utils.system;

public class SystemServiceFactory {

    private static WindowsSystemService windows;

    public static SystemService create() {
        String os = System.getProperty("os.name").toLowerCase();

        if (os.contains("win")) {
            return (windows == null ? windows = new WindowsSystemService() : windows);
        }

        throw new UnsupportedOperationException("Sistema operacional não suportado: " + os);
    }
}