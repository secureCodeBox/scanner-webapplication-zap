package io.securecodebox.zap.service.engine.model.zap;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.securecodebox.zap.service.engine.model.Target;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Data
@Slf4j
@JsonIgnoreProperties(ignoreUnknown = true)
public class ZapTask {

    private String jobId;
    private List<Target> targets;
}
