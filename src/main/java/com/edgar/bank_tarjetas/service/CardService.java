package com.edgar.bank_tarjetas.service;

import com.edgar.bank_tarjetas.entities.Card;
import com.edgar.bank_tarjetas.entities.CardStatus;
import com.edgar.bank_tarjetas.exceptions.CardException;
import com.edgar.bank_tarjetas.repositories.CardRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class CardService {

    private final CardRepository cardRepository;

    private final Random random = new Random();

    private Card findCardOrThrow(String cardId) {
        return cardRepository.findByCardId(cardId)
                .orElseThrow(() -> new CardException("La tarjeta con ID " + cardId + " no existe en la base de datos"));
    }
    
    // Generar número de tarjeta: 6 dígitos de productId + 10 aleatorios
    public String generateCardNumber(String productId) {
        StringBuilder sb = new StringBuilder(productId);
        while (sb.length() < 16) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

    // Crear nueva tarjeta
    public Card createCard(String productId, String cardId, String name, String lastName) {
        Card card = Card.builder()
                .productId(productId)
                .cardId(cardId)
                .name(name)
                .lastName(lastName)
                .createdAt(LocalDate.now())
                .expirationDate(LocalDate.now().plusYears(3))
                .balance(BigDecimal.ZERO)
                .status(CardStatus.INACTIVE)
                .build();
        return cardRepository.save(card);
    }    

    // Activar tarjeta
    public Card enrollCard(String cardId) {
        Card card = findCardOrThrow(cardId);

        if (card.getStatus() == CardStatus.ACTIVE) {
            throw new CardException("La tarjeta ya está activa");
        }
        card.setStatus(CardStatus.ACTIVE);
        return cardRepository.save(card);
    }

    // Bloquear tarjeta
    public Card blockCard(String cardId) {
        Card card = findCardOrThrow(cardId);

        if (card.getStatus() == CardStatus.BLOCKED) {
            throw new CardException("La tarjeta ya está bloqueada");
        }
        card.setStatus(CardStatus.BLOCKED);
        return cardRepository.save(card);
    }

    // Recargar saldo
    public Card rechargeBalance(String cardId, BigDecimal amount) {
        Card card = findCardOrThrow(cardId);

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new CardException("El monto debe ser mayor a 0");
        }
        card.setBalance(card.getBalance().add(amount));
        return cardRepository.save(card);
    }

    // Consultar saldo
    public BigDecimal getBalance(String cardId) {
        return findCardOrThrow(cardId).getBalance();
    }

    // Consultar tarjeta
    public Card getCard(String cardId) {
        return findCardOrThrow(cardId);
    }
}