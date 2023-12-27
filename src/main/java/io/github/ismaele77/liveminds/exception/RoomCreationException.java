package io.github.ismaele77.liveminds.exception;

public class RoomCreationException extends RuntimeException {

    public RoomCreationException() {
        super("Room creation failed");
    }
}