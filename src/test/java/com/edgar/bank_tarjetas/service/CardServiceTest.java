package com.edgar.bank_tarjetas.service;

import com.edgar.bank_tarjetas.entities.Card;
import com.edgar.bank_tarjetas.entities.CardStatus;
import com.edgar.bank_tarjetas.exceptions.CardException;
import com.edgar.bank_tarjetas.repositories.CardRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.Optional;
import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class CardServiceTest {

    @Mock
    private CardRepository cardRepository; // Mockeamos el repository

    @InjectMocks
    private CardService cardService; // CardService donde vamos a probar los métodos

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this); // Inicializa los mocks
    }

    @Test
    void testGenerateCardNumber() {
        String productId = "654321";

        String cardNumber = cardService.generateCardNumber(productId);

        // Validaciones
        assertNotNull(cardNumber);
        assertEquals(16, cardNumber.length());
        assertTrue(cardNumber.startsWith(productId));

        // Validar que los dígitos restantes son numéricos
        String lastDigits = cardNumber.substring(6);
        assertTrue(lastDigits.matches("\\d{10}"), "Los últimos 10 dígitos deben ser numéricos");
    }
    
    @Test
    void testCreateCard_Success() {
        // Datos de prueba
        String productId = "102030";
        String cardId = "1020301234567890";
        String name = "Edgar";
        String lastName = "Munar";

        // Creamos un objeto Card que devolverá el repository simulado
        Card mockCard = Card.builder()
                .productId(productId)
                .cardId(cardId)
                .name(name)
                .lastName(lastName)
                .createdAt(LocalDate.now())
                .expirationDate(LocalDate.now().plusYears(3))
                .balance(BigDecimal.ZERO)
                .status(CardStatus.INACTIVE)
                .build();

        // Decimos que cuando cardRepository.save() sea llamado, devuelva nuestro mockCard
        when(cardRepository.save(any(Card.class))).thenReturn(mockCard);

        // Ejecutamos el método
        Card result = cardService.createCard(productId, cardId, name, lastName);

        // Verificaciones
        assertNotNull(result); // No debe ser null
        assertEquals(productId, result.getProductId());
        assertEquals(cardId, result.getCardId());
        assertEquals(name, result.getName());
        assertEquals(lastName, result.getLastName());
        assertEquals(CardStatus.INACTIVE, result.getStatus());
        assertEquals(BigDecimal.ZERO, result.getBalance());

        // Verificamos que save se llamó 1 vez
        verify(cardRepository, times(1)).save(any(Card.class));
    }

    @Test
    void testEnrollCard_Success() {
        String cardId = "9999991234567890";

        Card inactiveCard = Card.builder()
                .cardId(cardId)
                .status(CardStatus.INACTIVE)
                .balance(BigDecimal.ZERO)
                .build();

        // Simula que existe la tarjeta
        when(cardRepository.findByCardId(cardId)).thenReturn(Optional.of(inactiveCard));

        // Simula que se guarda
        when(cardRepository.save(any(Card.class))).thenReturn(inactiveCard);

        Card result = cardService.enrollCard(cardId);

        assertEquals(CardStatus.ACTIVE, result.getStatus());
        verify(cardRepository, times(1)).findByCardId(cardId);
        verify(cardRepository, times(1)).save(inactiveCard);
    }

    @Test
    void testEnrollCard_AlreadyActive() {
        String cardId = "9999991234567890";

        Card activeCard = Card.builder()
                .cardId(cardId)
                .status(CardStatus.ACTIVE)
                .build();

        when(cardRepository.findByCardId(cardId)).thenReturn(Optional.of(activeCard));

        CardException ex = assertThrows(
                CardException.class,
                () -> cardService.enrollCard(cardId)
        );

        assertEquals("La tarjeta ya está activa", ex.getMessage());
        verify(cardRepository, times(1)).findByCardId(cardId);
        verify(cardRepository, never()).save(any());
    }
    @Test
    void testBlockCard_Success() {
        String cardId = "5555551234567890";

        Card activeCard = Card.builder()
                .cardId(cardId)
                .status(CardStatus.ACTIVE)
                .build();

        // Simula tarjeta encontrada
        when(cardRepository.findByCardId(cardId)).thenReturn(Optional.of(activeCard));

        // Simula guardado
        when(cardRepository.save(any(Card.class))).thenReturn(activeCard);

        Card result = cardService.blockCard(cardId);

        assertEquals(CardStatus.BLOCKED, result.getStatus());
        verify(cardRepository, times(1)).findByCardId(cardId);
        verify(cardRepository, times(1)).save(activeCard);
    }

    @Test
    void testBlockCard_AlreadyBlocked() {
        String cardId = "5555551234567890";

        Card blockedCard = Card.builder()
                .cardId(cardId)
                .status(CardStatus.BLOCKED)
                .build();

        when(cardRepository.findByCardId(cardId)).thenReturn(Optional.of(blockedCard));

        CardException ex = assertThrows(
                CardException.class,
                () -> cardService.blockCard(cardId)
        );

        assertEquals("La tarjeta ya está bloqueada", ex.getMessage());
        verify(cardRepository, times(1)).findByCardId(cardId);
        verify(cardRepository, never()).save(any());
    }

    @Test
    void testRechargeBalance_Success() {
        String cardId = "7777771234567890";
        BigDecimal initialBalance = new BigDecimal("100.00");
        BigDecimal rechargeAmount = new BigDecimal("50.00");

        Card card = Card.builder()
                .cardId(cardId)
                .balance(initialBalance)
                .status(CardStatus.ACTIVE)
                .build();

        when(cardRepository.findByCardId(cardId)).thenReturn(Optional.of(card));
        when(cardRepository.save(any(Card.class))).thenReturn(card);

        Card result = cardService.rechargeBalance(cardId, rechargeAmount);

        assertNotNull(result);
        assertEquals(new BigDecimal("150.00"), result.getBalance());

        verify(cardRepository, times(1)).findByCardId(cardId);
        verify(cardRepository, times(1)).save(card);
    }
    @Test
    void testRechargeBalance_InvalidAmount() {
        String cardId = "7777771234567890";
        BigDecimal invalidAmount = BigDecimal.ZERO;

        Card card = Card.builder()
                .cardId(cardId)
                .balance(BigDecimal.ZERO)
                .status(CardStatus.ACTIVE)
                .build();

        when(cardRepository.findByCardId(cardId)).thenReturn(Optional.of(card));

        CardException ex = assertThrows(
                CardException.class,
                () -> cardService.rechargeBalance(cardId, invalidAmount)
        );

        assertEquals("El monto debe ser mayor a 0", ex.getMessage());
        verify(cardRepository, times(1)).findByCardId(cardId);
        verify(cardRepository, never()).save(any());
    }

    @Test
    void testGetBalance_Success() {
        String cardId = "1111222233334444";
        BigDecimal balance = new BigDecimal("250.00");

        Card card = Card.builder()
                .cardId(cardId)
                .balance(balance)
                .status(CardStatus.ACTIVE)
                .build();

        when(cardRepository.findByCardId(cardId)).thenReturn(Optional.of(card));

        BigDecimal result = cardService.getBalance(cardId);

        assertNotNull(result);
        assertEquals(balance, result);

        verify(cardRepository, times(1)).findByCardId(cardId);
    }

    @Test
    void testGetBalance_CardNotFound() {
        String cardId = "0000009999998888";

        when(cardRepository.findByCardId(cardId)).thenReturn(Optional.empty());

        CardException ex = assertThrows(
                CardException.class,
                () -> cardService.getBalance(cardId)
        );

        assertEquals("La tarjeta con ID " + cardId + " no existe en la base de datos", ex.getMessage());

        verify(cardRepository, times(1)).findByCardId(cardId);
    }

    @Test
    void testGetCard_Success() {
        String cardId = "1234567890123456";

        Card card = Card.builder()
                .cardId(cardId)
                .name("Edgar")
                .lastName("Munar")
                .status(CardStatus.ACTIVE)
                .balance(BigDecimal.ZERO)
                .build();

        when(cardRepository.findByCardId(cardId)).thenReturn(Optional.of(card));

        Card result = cardService.getCard(cardId);

        assertNotNull(result);
        assertEquals(cardId, result.getCardId());
        assertEquals("Edgar", result.getName());
        assertEquals("Munar", result.getLastName());

        verify(cardRepository, times(1)).findByCardId(cardId);
    }

    @Test
    void testGetCard_NotFound() {
        String cardId = "0000111122223333";

        when(cardRepository.findByCardId(cardId)).thenReturn(Optional.empty());

        CardException ex = assertThrows(
                CardException.class,
                () -> cardService.getCard(cardId)
        );

        assertEquals("La tarjeta con ID " + cardId + " no existe en la base de datos", ex.getMessage());

        verify(cardRepository, times(1)).findByCardId(cardId);
    }
}