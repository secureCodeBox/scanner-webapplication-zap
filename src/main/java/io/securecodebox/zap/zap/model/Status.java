package io.securecodebox.zap.zap.model;

import lombok.Data;

@Data
public class Status {
    private String status;


    public int getStatusInPercent() {
        int result = Integer.parseInt(status);
        if (result >= 0 && result <= 100) {
            return result;
        } else {
            throw new IllegalArgumentException("The current status is not between 0 and 100");
        }
    }
}
