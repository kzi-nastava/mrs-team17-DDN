package org.example.backend.dto.request;

public class RideCancelRequestDto {
    private String reason;

    public RideCancelRequestDto() {}

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
