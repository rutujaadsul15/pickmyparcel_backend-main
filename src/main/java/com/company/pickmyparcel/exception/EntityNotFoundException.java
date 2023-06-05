package com.company.pickmyparcel.exception;


import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class EntityNotFoundException extends RuntimeException {

    private String entityName;
    private String messageString;

    public EntityNotFoundException(String entity, String message) {
        super(entity + " " + message);
        entityName = entity;
        messageString = message;
    }


}
