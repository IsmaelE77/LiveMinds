package io.github.ismaele77.LiveMinds.Exception;

public class RoomCreationException extends RuntimeException {

    public RoomCreationException() {
        super("Error creating room");
    }
}