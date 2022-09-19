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
        log.info("500 {}", exception.getMessage());
        return new ErrorResponse("Сущность с такими данными уже существует!", exception.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidationException(final ValidationException exception) {
        log.info("400 {}", exception.getMessage());
        return new ErrorResponse("Ошибка валидации!", exception.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleEntityNotFoundException(final EntityNotFoundException exception) {
        log.info("404 {}", exception.getMessage());
        return new ErrorResponse("Сущность не найдена!", exception.getMessage());
    }
}
