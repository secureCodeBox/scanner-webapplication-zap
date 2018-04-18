package io.securecodebox.zap.service.engine.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Data
public class Reference {

    private String id;
    private String source;

    public Reference(String source) {
        this.source = source;
    }

    public Reference(String id, String source) {
        this.id = id;
        this.source = source;
    }
}