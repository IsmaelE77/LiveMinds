package io.github.ismaele77.LiveMinds.Exception.Advice;

import io.github.ismaele77.LiveMinds.Exception.LiveKitException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class LiveKitAdvice {
    @ResponseBody
    @ExceptionHandler(LiveKitException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    String liveKitExceptionHandler(LiveKitException ex) {
        return ex.getMessage();
    }
}