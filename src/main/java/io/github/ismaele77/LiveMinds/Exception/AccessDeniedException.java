package io.github.ismaele77.LiveMinds.Exception;

public class AccessDeniedException extends RuntimeException {

    public AccessDeniedException(String command) {
        super("You do not have permission for : " + command);
    }
}