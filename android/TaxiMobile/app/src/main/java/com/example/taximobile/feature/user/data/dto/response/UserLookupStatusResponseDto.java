package com.example.taximobile.feature.user.data.dto.response;

public class UserLookupStatusResponseDto {

    private boolean exists;
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private boolean active;
    private boolean blocked;
    private String blockReason;

    public UserLookupStatusResponseDto() {}

    public boolean isExists() { return exists; }
    public void setExists(boolean exists) { this.exists = exists; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public boolean isBlocked() { return blocked; }
    public void setBlocked(boolean blocked) { this.blocked = blocked; }

    public String getBlockReason() { return blockReason; }
    public void setBlockReason(String blockReason) { this.blockReason = blockReason; }
}
