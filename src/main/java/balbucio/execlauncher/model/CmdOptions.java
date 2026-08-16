package balbucio.execlauncher.model;

import com.google.gson.annotations.Expose;
import lombok.*;

@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CmdOptions {

    @Expose
    private boolean delayRun = false;
    @Expose
    private int delayRunInSecs = 5;
}