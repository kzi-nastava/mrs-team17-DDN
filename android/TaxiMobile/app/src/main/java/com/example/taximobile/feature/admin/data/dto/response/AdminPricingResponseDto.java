package com.example.taximobile.feature.admin.data.dto.response;

public class AdminPricingResponseDto {
    private String standard;
    private String luxury;
    private String van;

    public String getStandard() { return standard; }
    public void setStandard(String standard) { this.standard = standard; }

    public String getLuxury() { return luxury; }
    public void setLuxury(String luxury) { this.luxury = luxury; }

    public String getVan() { return van; }
    public void setVan(String van) { this.van = van; }
}
