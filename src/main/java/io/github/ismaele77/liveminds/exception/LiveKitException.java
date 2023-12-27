package io.github.ismaele77.liveminds.exception;

public class LiveKitException extends RuntimeException {
    public LiveKitException(String command) {
        super("Failed to execute : " + command);
    }
}