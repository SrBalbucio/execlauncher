package balbucio.execlauncher.utils;

public class SystemServiceFactory {

    public static SystemService create() {
        String os = System.getProperty("os.name").toLowerCase();

        if (os.contains("win")) {
            return new WindowsSystemService();
        } else if (os.contains("nix") || os.contains("nux") || os.contains("aix")) {
            return new LinuxSystemService();
        }

        throw new UnsupportedOperationException("Sistema operacional não suportado: " + os);
    }
}