package ru.practicum.shareit.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice("ru.practicum.shareit")
public class ErrorHandler {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleEntityAlreadyExistsException(final EntityAlreadyExistsException exception) {
        log.debug("500 {}", exception.getMessage(), exception);
        return new ErrorResponse("Сущность с такими данными уже существует!", exception.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidationException(final ValidationException exception) {
        log.debug("400 {}", exception.getMessage(), exception);
        return new ErrorResponse("Неверный запрос!", exception.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleEntityNotFoundException(final EntityNotFoundException exception) {
        log.debug("404 {}", exception.getMessage(), exception);
        return new ErrorResponse("Сущность не найдена!", exception.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleIllegalArgumentException(final IllegalArgumentException exception) {
        log.debug("400 {}", exception.getMessage(), exception);
        return new ErrorResponse(exception.getMessage(), "Неверный запрос!");
    }
}
