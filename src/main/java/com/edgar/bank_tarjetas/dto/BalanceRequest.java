package com.edgar.bank_tarjetas.dto;

import lombok.Data;
import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

@Data
public class BalanceRequest {
    
    @NotBlank(message = "El numero de tarjeta no puede estar vacío")
    @Pattern(regexp = "\\d{16}", message = "cardId debe ser un número de 16 dígitos")
    private String cardId;

    @NotNull(message = "El saldo es requerido")
    @DecimalMin(value = "0.0", inclusive = false, message = "El saldo debe ser mayor que 0")
    private BigDecimal balance;
}