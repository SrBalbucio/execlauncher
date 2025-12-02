package balbucio.execlauncher.model;

import lombok.*;

@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CmdOptions {

    private boolean delayRun = false;
    private int delayRunInSecs = 5;
}
