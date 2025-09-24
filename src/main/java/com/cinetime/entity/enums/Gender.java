package com.cinetime.entity.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum Gender {
    MALE, FEMALE, OTHER;

    @JsonCreator
    public static Gender from(String v) {
        return Gender.valueOf(v.trim().toUpperCase());
    }

    public String toUpperCase() {
        return null;
    }
}
