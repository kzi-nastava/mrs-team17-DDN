package org.example.backend.dto.request;

public class RidePauseRequestDto {
    private String currentAddress;
    private String timestamp;

    public RidePauseRequestDto() {}

    public String getCurrentAddress() { return currentAddress; }
    public void setCurrentAddress(String currentAddress) { this.currentAddress = currentAddress; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
}
