package com.edgar.bank_tarjetas.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class EnrollRequest {

    @NotBlank(message = "cardId es requerido")
    @Pattern(regexp = "\\d{16}", message = "cardId debe ser un número de 16 dígitos")
    private String cardId;
}
