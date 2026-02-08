package org.example.backend.dto.response;

import java.sql.Timestamp;

public class RideReportDto {
        private String description;
        private Timestamp created_at;

        public RideReportDto() {}

        public RideReportDto(String description, Timestamp created_at) {
            this.description = description;
            this.created_at = created_at;
        }


    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Timestamp getCreated_at() {
        return created_at;
    }

    public void setCreated_at(Timestamp created_at) {
        this.created_at = created_at;
    }
}
