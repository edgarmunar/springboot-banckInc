package com.edgar.bank_tarjetas.service;

import com.edgar.bank_tarjetas.dto.TransactionAnulationRequest;
import com.edgar.bank_tarjetas.entities.Card;
import com.edgar.bank_tarjetas.entities.CardStatus;
import com.edgar.bank_tarjetas.entities.Transaction;
import com.edgar.bank_tarjetas.exceptions.TransactionException;
import com.edgar.bank_tarjetas.repositories.CardRepository;
import com.edgar.bank_tarjetas.repositories.TransactionRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final CardRepository cardRepository;
    private final TransactionRepository transactionRepository;

    private Card findCardOrThrow(String cardId) {
        return cardRepository.findByCardId(cardId)
                .orElseThrow(() ->
                    new TransactionException("La tarjeta con ID " + cardId + " no existe en la base de datos"));
    }

    // Realizar una compra y registrar la transacción
    @Transactional
    public Transaction makePurchase(String cardId, BigDecimal price) {
        Card card = findCardOrThrow(cardId);

        if (card.getStatus() == CardStatus.BLOCKED) {
            throw new TransactionException("La tarjeta está bloqueada");
        }

        if (card.getStatus() != CardStatus.ACTIVE) {
            throw new TransactionException("La tarjeta no está activa");
        }

        if (card.getExpirationDate().isBefore(LocalDateTime.now().toLocalDate())) {
            throw new TransactionException("La tarjeta está vencida");
        }

        if (card.getBalance().compareTo(price) < 0) {
            throw new TransactionException("Saldo insuficiente");
        }

        // Descontar saldo
        card.setBalance(card.getBalance().subtract(price));
        cardRepository.save(card);

        // Crear transacción
        Transaction transaction = new Transaction();
        transaction.setCard(card);
        transaction.setCardId(cardId);
        transaction.setPrice(price);
        transaction.setTransactionDate(LocalDateTime.now());

        return transactionRepository.save(transaction);
    }

    // Consultar una transacción por ID
    public Transaction getTransaction(Long transactionId) {
        return transactionRepository.findById(transactionId)
                .orElseThrow(() ->
                    new TransactionException("La transacción con ID " + transactionId + " no existe"));
    }

     @Transactional
    public Long anulateTransaction(TransactionAnulationRequest request) {

        // Validación de ID transacción
        Long id = Long.parseLong(request.getTransactionId());

        // Validación de tarjeta existente
        Card card = findCardOrThrow(request.getCardId());

        // Buscar transacción
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new TransactionException("La transacción no existe"));

        // Validar que pertenece a esa tarjeta
        if (!transaction.getCard().getCardId().equals(request.getCardId())) {
            throw new TransactionException("La transacción no pertenece a esta tarjeta");
        }

        // Validar si ya fue anulada
        if (transaction.isAnulated()) {
            throw new TransactionException("La transacción ya está anulada");
        }

        // Validar límite de 24 horas
        Duration diff = Duration.between(transaction.getTransactionDate(), LocalDateTime.now());
        if (diff.toHours() > 24) {
            throw new TransactionException("La transacción supera las 24 horas y no puede ser anulada");
        }

        // Marcar como anulada
        transaction.setAnulated(true);
        transaction.setAnulatedAt(LocalDateTime.now());
        transactionRepository.save(transaction);

        // Reintegrar saldo
        card.setBalance(card.getBalance().add(transaction.getPrice()));
        cardRepository.save(card);

        return transaction.getId();
    }
}