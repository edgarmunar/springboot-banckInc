package com.edgar.bank_tarjetas.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TransactionAnulationResponse {
    private String message;
    private String transactionId;
}