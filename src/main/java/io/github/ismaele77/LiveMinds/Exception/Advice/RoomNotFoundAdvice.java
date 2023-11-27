package io.github.ismaele77.LiveMinds.Exception.Advice;

import io.github.ismaele77.LiveMinds.DTO.SecurityResponse;
import io.github.ismaele77.LiveMinds.Exception.RoomNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@ControllerAdvice
public class RoomNotFoundAdvice {

    @ResponseBody
    @ExceptionHandler(RoomNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    SecurityResponse roomNotFoundHandler(RoomNotFoundException ex) {
        SecurityResponse response = new SecurityResponse(ex.getMessage());
        return response;
    }
}
