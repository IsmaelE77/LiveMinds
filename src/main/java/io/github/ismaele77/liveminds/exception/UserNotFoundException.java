package io.github.ismaele77.liveminds.exception;

public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(Long id) {
        super("Could not find user : " + id);
    }

    public UserNotFoundException(String name) {
        super("Could not find user : " + name);
    }
}