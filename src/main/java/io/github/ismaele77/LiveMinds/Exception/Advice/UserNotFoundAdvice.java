package io.github.ismaele77.LiveMinds.Exception.Advice;

import io.github.ismaele77.LiveMinds.DTO.SecurityResponse;
import io.github.ismaele77.LiveMinds.Exception.UserNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@ControllerAdvice
public class UserNotFoundAdvice {
    @ResponseBody
    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    SecurityResponse userNotFoundHandler(UserNotFoundException ex) {
        SecurityResponse response = new SecurityResponse(ex.getMessage());
        return response;
    }

}
