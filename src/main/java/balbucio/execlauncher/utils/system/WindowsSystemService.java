package balbucio.execlauncher.utils.system;

public class WindowsSystemService implements SystemService {

    @Override
    public void closeAllProcesses(long PID) {
        try {
            new ProcessBuilder("taskkill", "/PID", String.valueOf(PID), "/T", "/F").start();
        } catch(Exception ignored){}
    }
}