package com.edgar.bank_tarjetas.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TransactionAnulationRequest {

    @NotBlank
    @Size(min = 16, max = 16, message = "cardId debe tener 16 dígitos")
    @Pattern(regexp = "\\d{16}", message = "cardId debe ser numérico de 16 dígitos")
    private String cardId;

    @NotNull(message = "transactionId es requerido")
    @Pattern(regexp = "^[0-9]+$", message = "transactionId debe ser numérico")
    private String transactionId;
}