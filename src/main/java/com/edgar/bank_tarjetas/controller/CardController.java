package com.edgar.bank_tarjetas.controller;

import com.edgar.bank_tarjetas.dto.BalanceRequest;
import com.edgar.bank_tarjetas.dto.CreateCardRequest;
import com.edgar.bank_tarjetas.dto.EnrollRequest;
import com.edgar.bank_tarjetas.entities.Card;
import com.edgar.bank_tarjetas.service.CardService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/card")
@RequiredArgsConstructor
@Validated
public class CardController {

    private final CardService cardService;

    // 1. Generar número de tarjeta
    @GetMapping("/{productId}/number")
    public ResponseEntity<String> generateCardNumber(
        @PathVariable
        @Pattern(regexp = "\\d{6}", message = "productId debe ser numérico de 6 dígitos") String productId) {
        String number = cardService.generateCardNumber(productId);
        return ResponseEntity.ok(number);
    }

    // 6. Crear nueva tarjeta (opcional para pruebas)
    @PostMapping("/create")
    public ResponseEntity<Card> createCard(@Valid @RequestBody CreateCardRequest request) {
        Card card = cardService.createCard(
            request.getProductId(),
            request.getCardId(),  
            request.getName(),
            request.getLastName()
        );
        return ResponseEntity.ok(card);
    }

    // 2. Activar tarjeta
    @PostMapping("/enroll")
    public ResponseEntity<Card> enrollCard(@Valid @RequestBody EnrollRequest request) {
        Card card = cardService.enrollCard(request.getCardId());
        return ResponseEntity.ok(card);
    }

    // 3. Bloquear tarjeta
    @DeleteMapping("/{cardId}")
    public ResponseEntity<Card> blockCard(
        @PathVariable
        @Pattern(regexp = "\\d{16}", message = "cardId debe ser un número de 16 dígitos") 
        String cardId) {
        Card card = cardService.blockCard(cardId);
        return ResponseEntity.ok(card);
    }

    // 4. Recargar saldo
    @PostMapping("/balance")
    public ResponseEntity<Card> rechargeBalance(@Valid @RequestBody BalanceRequest request) {
        Card card = cardService.rechargeBalance(request.getCardId(), request.getBalance());
        return ResponseEntity.ok(card);
    }

    // 5. Consultar saldo
    @GetMapping("/balance/{cardId}")
    public ResponseEntity<BigDecimal> getBalance(
        @PathVariable 
        @Pattern(regexp = "\\d{16}", message = "cardId debe ser un número de 16 dígitos")
        String cardId) {
        BigDecimal balance = cardService.getBalance(cardId);
        return ResponseEntity.ok(balance);
    }

    // 7. Consultar tarjeta completa
    @GetMapping("/{cardId}")
    public ResponseEntity<Card> getCard(
         @PathVariable 
         @Pattern(regexp = "\\d{16}", message = "cardId debe ser un número de 16 dígitos")
         String cardId) {
        Card card = cardService.getCard(cardId);
        return ResponseEntity.ok(card);
    }
}