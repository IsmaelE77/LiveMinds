package io.github.ismaele77.liveminds.exception.advice;

import io.github.ismaele77.liveminds.dto.ErrorModel;
import io.github.ismaele77.liveminds.dto.response.ErrorResponseModel;
import io.github.ismaele77.liveminds.dto.response.SecurityResponse;
import io.github.ismaele77.liveminds.exception.*;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@ControllerAdvice
@RestController
@Configuration
public class GlobalExceptionHandler {
    @ResponseBody
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    SecurityResponse handleAccessDeniedException(AccessDeniedException ex) {
        SecurityResponse response = new SecurityResponse(ex.getMessage());
        return response;
    }

    @ResponseBody
    @ExceptionHandler(LiveKitException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    SecurityResponse liveKitExceptionHandler(LiveKitException ex) {
        SecurityResponse response = new SecurityResponse(ex.getMessage());
        return response;
    }

    @ResponseBody
    @ExceptionHandler(RoomCreationException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    SecurityResponse handleRoomCreationException(RoomCreationException ex) {
        SecurityResponse response = new SecurityResponse(ex.getMessage());
        return response;
    }

    @ResponseBody
    @ExceptionHandler(RoomNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    SecurityResponse roomNotFoundHandler(RoomNotFoundException ex) {
        SecurityResponse response = new SecurityResponse(ex.getMessage());
        return response;
    }

    @ResponseBody
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    SecurityResponse handleIllegalArgumentException(IllegalArgumentException ex) {
        SecurityResponse response = new SecurityResponse(ex.getMessage());
        return response;
    }

    @ResponseBody
    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    SecurityResponse userNotFoundHandler(UserNotFoundException ex) {
        SecurityResponse response = new SecurityResponse(ex.getMessage());
        return response;
    }

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public ErrorResponseModel handleException(MethodArgumentNotValidException e) {
        List<ErrorModel> errorModels = processErrors(e);
        return ErrorResponseModel
                .builder()
                .errors(errorModels)
                .type("VALIDATION")
                .build();
    }

    private List<ErrorModel> processErrors(MethodArgumentNotValidException e) {
        List<ErrorModel> validationErrorModels = new ArrayList<ErrorModel>();
        for (FieldError fieldError : e.getBindingResult().getFieldErrors()) {
            ErrorModel validationErrorModel = ErrorModel
                    .builder()
                    .code(fieldError.getCode())
                    .source(fieldError.getObjectName() + "/" + fieldError.getField())
                    .detail(fieldError.getField() + " " + fieldError.getDefaultMessage())
                    .build();
            validationErrorModels.add(validationErrorModel);
        }
        return validationErrorModels;
    }
}
