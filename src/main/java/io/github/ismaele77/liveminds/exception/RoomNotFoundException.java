package io.github.ismaele77.liveminds.exception;

public class RoomNotFoundException extends RuntimeException {

    public RoomNotFoundException(String name) {
        super("Could not find room : " + name);
    }
}