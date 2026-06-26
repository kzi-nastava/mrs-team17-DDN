package org.example.backend.enums;

public enum ESortBy {
    STARTED_AT("r.started_at"),
    ENDED_AT("r.ended_at");

    private final String column;

    ESortBy(String column) {
        this.column = column;
    }

    public String getColumn() {
        return column;
    }
}
