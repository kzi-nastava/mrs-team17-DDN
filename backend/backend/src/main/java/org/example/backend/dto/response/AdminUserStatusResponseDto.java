package org.example.backend.dto.response;

public class AdminUserStatusResponseDto {

    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String role;
    private boolean blocked;
    private String blockReason;

    public AdminUserStatusResponseDto() {}

    public AdminUserStatusResponseDto(Long id, String email, String firstName, String lastName, String role, boolean blocked, String blockReason) {
        this.id = id;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
        this.blocked = blocked;
        this.blockReason = blockReason;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public boolean isBlocked() { return blocked; }
    public void setBlocked(boolean blocked) { this.blocked = blocked; }

    public String getBlockReason() { return blockReason; }
    public void setBlockReason(String blockReason) { this.blockReason = blockReason; }
}
