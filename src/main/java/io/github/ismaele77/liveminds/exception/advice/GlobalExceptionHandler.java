package io.github.ismaele77.liveminds.exception.advice;

import io.github.ismaele77.liveminds.dto.ErrorModel;
import io.github.ismaele77.liveminds.dto.response.ErrorResponseModel;
import io.github.ismaele77.liveminds.dto.response.SecurityResponse;
import io.github.ismaele77.liveminds.exception.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@ControllerAdvice
@RestController
@Configuration
@Slf4j
public class GlobalExceptionHandler {

    @ResponseBody
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    SecurityResponse handleAccessDeniedException(AccessDeniedException ex) {
        log.error("Access Denied Exception: {}", ex.getMessage(), ex);
        return new SecurityResponse(ex.getMessage());
    }
    
    @ResponseBody
    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    SecurityResponse authSecurityResponse(AuthenticationException ex) {
        return new SecurityResponse(ex.getMessage());
    }

    @ResponseBody
    @ExceptionHandler(LiveKitException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    SecurityResponse liveKitExceptionHandler(LiveKitException ex) {
        log.error("LiveKit Exception: {}", ex.getMessage(), ex);
        return new SecurityResponse(ex.getMessage());
    }

    @ResponseBody
    @ExceptionHandler(RoomCreationException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    SecurityResponse handleRoomCreationException(RoomCreationException ex) {
        log.error("Room Creation Exception: {}", ex.getMessage(), ex);
        return new SecurityResponse(ex.getMessage());
    }

    @ResponseBody
    @ExceptionHandler(RoomNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    SecurityResponse roomNotFoundHandler(RoomNotFoundException ex) {
        log.error("Room Not Found Exception: {}", ex.getMessage(), ex);
        return new SecurityResponse(ex.getMessage());
    }

    @ResponseBody
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    SecurityResponse handleIllegalArgumentException(IllegalArgumentException ex) {
        log.error("Illegal Argument Exception: {}", ex.getMessage(), ex);
        return new SecurityResponse(ex.getMessage());
    }

    @ResponseBody
    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    SecurityResponse userNotFoundHandler(UserNotFoundException ex) {
        log.error("User Not Found Exception: {}", ex.getMessage(), ex);
        return new SecurityResponse(ex.getMessage());
    }

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public ErrorResponseModel handleException(MethodArgumentNotValidException e) {
        log.error("Method Argument Not Valid Exception: {}", e.getMessage(), e);
        List<ErrorModel> errorModels = processErrors(e);
        return ErrorResponseModel
                .builder()
                .errors(errorModels)
                .type("VALIDATION")
                .build();
    }

    private List<ErrorModel> processErrors(MethodArgumentNotValidException e) {
        List<ErrorModel> validationErrorModels = new ArrayList<>();
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
