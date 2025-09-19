package org.example.expert.domain.file.enums;

public enum FileDomain {
    PROFILE;

    public String getDirectory() {
        return this.name().toLowerCase();
    }
}
