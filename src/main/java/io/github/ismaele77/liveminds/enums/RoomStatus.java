package io.github.ismaele77.liveminds.enums;

public enum RoomStatus {
    NOT_STARTED("Not Started"),
    STREAMING("Streaming"),
    FINISHED("Finished");

    private final String value;

    RoomStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
