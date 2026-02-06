package org.example.backend.dto.request;

import jakarta.validation.constraints.NotNull;

public class AdminSetUserBlockRequestDto {

    @NotNull
    private Boolean blocked;

    private String blockReason;

    public Boolean getBlocked() { return blocked; }
    public void setBlocked(Boolean blocked) { this.blocked = blocked; }

    public String getBlockReason() { return blockReason; }
    public void setBlockReason(String blockReason) { this.blockReason = blockReason; }
}


