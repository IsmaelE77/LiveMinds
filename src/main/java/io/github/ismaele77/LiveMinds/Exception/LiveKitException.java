package io.github.ismaele77.LiveMinds.Exception;

import java.io.IOException;

public class LiveKitException extends RuntimeException {
    public LiveKitException(String command) {
        super("Failed to execute : " + command);
    }
}