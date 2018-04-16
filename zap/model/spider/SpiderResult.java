package io.securecodebox.zap.zap.model.spider;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SpiderResult {
    private List<FullResults> fullResults;

    public List<FullResults> getFullResults() {
        return fullResults;
    }

    @Override
    public String toString() {
        return "SpiderResult [fullResults=" + (fullResults != null ? fullResults.subList(0, Math.min(fullResults.size(), 10)) : null) + ']';
    }
}
