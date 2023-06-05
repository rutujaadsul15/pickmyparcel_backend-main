package com.company.pickmyparcel.advice;

import com.company.pickmyparcel.exception.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.validation.ConstraintViolationException;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class ApplicationExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleException(MethodArgumentNotValidException ex) {
        Map<String, String> resultMap = new HashMap<>();
        ex.getFieldErrors().forEach(fieldError -> {
            resultMap.put(fieldError.getField(), fieldError.getDefaultMessage());
        });
        return resultMap;
    }


    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, String> handleException(ConstraintViolationException ex) {
        Map<String, String> resultMap = new HashMap<>();
        ex.getConstraintViolations().forEach(violation -> {
            resultMap.put(violation.getPropertyPath().toString(), violation.getMessage());
        });
        return resultMap;
    }


    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, String> handleException (EntityNotFoundException ex) {
        Map<String, String> resultMap = new HashMap<>();
        resultMap.put(ex.getEntityName(), ex.getMessageString());
        return resultMap;
    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public  Map<String, String>  handleException (RuntimeException ex) {
        Map<String, String> resultMap = new HashMap<>();
        resultMap.put("Exception", ex.getMessage());
        return resultMap;
    }

}
