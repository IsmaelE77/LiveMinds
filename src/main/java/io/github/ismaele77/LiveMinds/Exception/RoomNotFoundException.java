package io.github.ismaele77.LiveMinds.Exception;

public class RoomNotFoundException extends RuntimeException {

    public RoomNotFoundException(String name) {
        super("Could not find room : " + name);
    }
}