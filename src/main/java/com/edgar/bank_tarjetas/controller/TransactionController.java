package com.edgar.bank_tarjetas.controller;

import com.edgar.bank_tarjetas.dto.PurchaseRequest;
import com.edgar.bank_tarjetas.dto.TransactionAnulationRequest;
import com.edgar.bank_tarjetas.dto.TransactionAnulationResponse;
import com.edgar.bank_tarjetas.entities.Transaction;
import com.edgar.bank_tarjetas.service.TransactionService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/transaction")
@RequiredArgsConstructor
@Validated
public class TransactionController {

    private final TransactionService transactionService;

    // Realizar una compra
    @PostMapping("/purchase")
    public ResponseEntity<?> makePurchase(@Valid @RequestBody PurchaseRequest purchaseRequest) {
        Transaction transaction = transactionService.makePurchase(purchaseRequest.getCardId(), purchaseRequest.getPrice());
        return ResponseEntity.ok(transaction);
    }

    // Consultar una transacción por ID
    @GetMapping("/{transactionId}")
    public ResponseEntity<Transaction> getTransaction(
         @PathVariable 
         @NotNull @Pattern(regexp = "^[0-9]+$", message = "El valor debe ser numérico")
         String transactionId) {
            Long id = Long.parseLong(transactionId);
        Transaction transaction = transactionService.getTransaction(id);
        return ResponseEntity.ok(transaction);
    }

    @PostMapping("/anulation")
    public ResponseEntity<?> anulateTransaction(@Valid @RequestBody TransactionAnulationRequest request) {
        Long transactionId = transactionService.anulateTransaction(request);
        TransactionAnulationResponse response = new TransactionAnulationResponse(
            "Transacción anulada exitosamente",
            String.valueOf(transactionId)
        );
        return ResponseEntity.ok(response);
    }
}