package com.example.taximobile.feature.admin.data.dto.request;

public class AdminPricingUpdateRequestDto {
    private String standard;
    private String luxury;
    private String van;

    public AdminPricingUpdateRequestDto() {}

    public AdminPricingUpdateRequestDto(String standard, String luxury, String van) {
        this.standard = standard;
        this.luxury = luxury;
        this.van = van;
    }

    public String getStandard() { return standard; }
    public void setStandard(String standard) { this.standard = standard; }

    public String getLuxury() { return luxury; }
    public void setLuxury(String luxury) { this.luxury = luxury; }

    public String getVan() { return van; }
    public void setVan(String van) { this.van = van; }
}
