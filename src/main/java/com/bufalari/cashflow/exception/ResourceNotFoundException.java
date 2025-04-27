// Path: src/main/java/com/bufalari/cashflow/exception/ResourceNotFoundException.java
package com.bufalari.cashflow.exception; // Ensure correct package

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Custom exception thrown when a requested resource (like a ManualCashEntry) is not found.
 * Maps to HTTP 404 Not Found status code.
 * Exceção customizada lançada quando um recurso solicitado (como um Lançamento Manual) não é encontrado.
 * Mapeia para o código de status HTTP 404 Not Found.
 */
@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}