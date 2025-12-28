package org.example.backend.dto.response;

public class UserRegisterResponseDto {
    private Long id;
    private String email;
    private String name;
    private String surname;
    private boolean active;
    private String avatarUrl;

    public UserRegisterResponseDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSurname() { return surname; }
    public void setSurname(String surname) { this.surname = surname; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public void setUserId(long l) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setMessage(String user_registered_successfully_Activation_e) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

   
}