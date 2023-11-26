package io.github.ismaele77.LiveMinds.Exception.Advice;

import io.github.ismaele77.LiveMinds.Exception.RoomCreationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@ControllerAdvice
public class RoomCreationAdvice {

    @ResponseBody
    @ExceptionHandler(RoomCreationException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    String handleRoomCreationException(RoomCreationException ex) {
        return ex.getMessage();
    }

}