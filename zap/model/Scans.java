package io.securecodebox.zap.zap.model;

import lombok.Data;

import java.util.List;


/**
 * List of {@link Scan}s.
 */
@Data
public class Scans {
    private List<Scan> scans;


    @Override
    public String toString() {
        return "Scans [scans=" + (scans != null ? scans.subList(0, Math.min(scans.size(), 10)) : null) + ']';
    }
}
