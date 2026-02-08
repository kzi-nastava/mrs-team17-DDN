package com.example.taximobile.feature.admin.data.dto.response;

public class AdminCreateDriverResponseDto {

    private Long driverId;
    private String email;
    private String status;
    private Integer activationLinkValidHours;

    public AdminCreateDriverResponseDto() {}

    public Long getDriverId() { return driverId; }
    public void setDriverId(Long driverId) { this.driverId = driverId; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getActivationLinkValidHours() { return activationLinkValidHours; }
    public void setActivationLinkValidHours(Integer activationLinkValidHours) { this.activationLinkValidHours = activationLinkValidHours; }
}
