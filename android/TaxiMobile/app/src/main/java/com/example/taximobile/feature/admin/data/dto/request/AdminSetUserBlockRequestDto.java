package com.example.taximobile.feature.admin.data.dto.request;

public class AdminSetUserBlockRequestDto {

    private Boolean blocked;
    private String blockReason;

    public AdminSetUserBlockRequestDto() {
    }

    public AdminSetUserBlockRequestDto(Boolean blocked, String blockReason) {
        this.blocked = blocked;
        this.blockReason = blockReason;
    }

    public Boolean getBlocked() {
        return blocked;
    }

    public void setBlocked(Boolean blocked) {
        this.blocked = blocked;
    }

    public String getBlockReason() {
        return blockReason;
    }

    public void setBlockReason(String blockReason) {
        this.blockReason = blockReason;
    }
}
