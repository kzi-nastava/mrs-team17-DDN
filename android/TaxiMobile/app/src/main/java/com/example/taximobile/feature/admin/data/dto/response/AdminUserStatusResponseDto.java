package com.example.taximobile.feature.admin.data.dto.response;

public class AdminUserStatusResponseDto {

    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String role;
    private boolean blocked;
    private String blockReason;

    public AdminUserStatusResponseDto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isBlocked() {
        return blocked;
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }

    public String getBlockReason() {
        return blockReason;
    }

    public void setBlockReason(String blockReason) {
        this.blockReason = blockReason;
    }

    public String displayName() {
        String fn = safe(firstName);
        String ln = safe(lastName);
        String n = (fn + " " + ln).trim();
        return n.isEmpty() ? safe(email) : n;
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }
}
