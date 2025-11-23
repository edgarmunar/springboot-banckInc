package com.edgar.bank_tarjetas.exceptions;

import lombok.AllArgsConstructor;
import lombok.Data;

// Clase que representa la estructura del JSON de error
@Data
@AllArgsConstructor
public class ErrorResponse {
    private String message;  // Mensaje de error
    private int statusCode;  // Código de estado HTTP (por ejemplo, 400, 404, 500)
}