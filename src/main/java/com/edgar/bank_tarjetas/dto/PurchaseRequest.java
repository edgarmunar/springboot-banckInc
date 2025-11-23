package com.edgar.bank_tarjetas.dto;

import lombok.Data;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

@Data
public class PurchaseRequest {

    @NotBlank(message = "cardId no puede estar vacío")
    @Pattern(regexp = "\\d{16}", message = "cardId debe ser un número de 16 dígitos")
    private String cardId; 

    @NotNull(message = "Price es requerido") 
    @DecimalMin(value = "0.01", message = "Price debe ser mayor que 0") 
    private BigDecimal price; 
}