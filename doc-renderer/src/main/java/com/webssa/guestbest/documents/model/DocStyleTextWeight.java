package com.webssa.guestbest.documents.model;

public enum DocStyleTextWeight {
    REGULAR("normal"), BOLD("bold");

    private final String json;

    DocStyleTextWeight(String json) {
        this.json = json;
    }

    public String getJson() {
        return json;
    }

    public static DocStyleTextWeight fromJson(String json) {
        for (DocStyleTextWeight option : DocStyleTextWeight.values()) {
            if (option.json.equals(json)) return option;
        }
        return null;
    }
}
