package balbucio.execlauncher.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@ToString
public class Executable {

    private UUID id = UUID.randomUUID();
    private String name;
    private String cmd;
    private String path;
    private Map<String, String> env = new HashMap<>();

    public File getFilePath(){
        return new File(path);
    }
}
