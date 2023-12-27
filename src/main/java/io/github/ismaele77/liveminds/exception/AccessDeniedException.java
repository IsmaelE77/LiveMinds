package io.github.ismaele77.liveminds.exception;

public class AccessDeniedException extends RuntimeException {

    public AccessDeniedException(String command) {
        super("You do not have permission for : " + command);
    }
}