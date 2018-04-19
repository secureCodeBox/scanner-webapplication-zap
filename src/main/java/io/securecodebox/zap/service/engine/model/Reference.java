package io.securecodebox.zap.service.engine.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.AllArgsConstructor;
import lombok.Data;


@Data
@AllArgsConstructor
@JsonInclude(Include.NON_EMPTY)
public class Reference {
    private String id;
    private String source;


    public Reference(String source) {
        this.source = source;
    }
}
