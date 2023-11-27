package io.github.ismaele77.LiveMinds.Exception;

public class RoomCreationException extends RuntimeException {

    public RoomCreationException() {
        super("Room creation failed");
    }
}