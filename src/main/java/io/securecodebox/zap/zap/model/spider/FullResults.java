package io.securecodebox.zap.zap.model.spider;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;


@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class FullResults {
    private List<SpiderResultUrl> urlsInScope;

    public List<SpiderResultUrl> getUrlsInScope() {
        return urlsInScope;
    }

    @Override
    public String toString() {
        return "FullResults [urlsInScope=" + (urlsInScope != null ? urlsInScope.subList(0, Math.min(urlsInScope.size(), 10)) : null) + ']';
    }
}
