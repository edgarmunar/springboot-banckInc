package com.edgar.bank_tarjetas.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateCardRequest {
    
    @NotBlank
    @Size(min = 6, max = 6, message = "productId debe tener exactamente 6 dígitos")
    @Pattern(regexp = "\\d{6}", message = "productId debe ser numérico de 6 dígitos")
    private String productId;
    
    @NotBlank
    @Size(min = 16, max = 16, message = "cardId debe tener 16 dígitos")
    @Pattern(regexp = "\\d{16}", message = "cardId debe ser numérico de 16 dígitos")
    private String cardId;
     
    @NotBlank
    @Size(min = 1, message = "El nombre es obligatorio")
    private String name;

    @NotBlank
    @Size(min = 1, message = "El apellido es obligatorio")
    private String lastName;
}