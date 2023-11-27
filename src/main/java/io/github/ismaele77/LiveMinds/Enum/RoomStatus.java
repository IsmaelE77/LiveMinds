package io.github.ismaele77.LiveMinds.Enum;

public enum RoomStatus {
    NOT_STARTED("NotStarted"),
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
