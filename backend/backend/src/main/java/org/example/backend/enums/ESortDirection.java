package org.example.backend.enums;

public enum ESortDirection {
    ASC("asc"),
    DESC("desc");

    private final String keyword;

    ESortDirection(String keyword) {
        this.keyword = keyword;
    }

    public String getKeyword() {
        return keyword;
    }
}
