package com.example.taximobile.feature.driver.data.dto.request;

public class UpdateDriverProfileRequestDto {

    private String firstName;
    private String lastName;
    private String address;
    private String phoneNumber;
    private String profileImageUrl;

    public UpdateDriverProfileRequestDto() {}

    public UpdateDriverProfileRequestDto(
            String firstName,
            String lastName,
            String address,
            String phoneNumber,
            String profileImageUrl
    ) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.profileImageUrl = profileImageUrl;
    }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getProfileImageUrl() { return profileImageUrl; }
    public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }
}
