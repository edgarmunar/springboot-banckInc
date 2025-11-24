package com.edgar.bank_tarjetas.service;

import com.edgar.bank_tarjetas.entities.Card;
import com.edgar.bank_tarjetas.entities.CardStatus;
import com.edgar.bank_tarjetas.entities.Transaction;
import com.edgar.bank_tarjetas.exceptions.TransactionException;
import com.edgar.bank_tarjetas.repositories.CardRepository;
import com.edgar.bank_tarjetas.repositories.TransactionRepository;
import com.edgar.bank_tarjetas.dto.TransactionAnulationRequest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class TransactionServiceTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private TransactionService transactionService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // ============================================================
    //                     makePurchase()
    // ============================================================

    @Test
    void testMakePurchase_Success() {
        String cardId = "1234567890123456";
        BigDecimal price = new BigDecimal("50");

        Card activeCard = Card.builder()
                .cardId(cardId)
                .status(CardStatus.ACTIVE)
                .expirationDate(LocalDate.now().plusYears(1))
                .balance(new BigDecimal("200"))
                .build();

        when(cardRepository.findByCardId(cardId)).thenReturn(Optional.of(activeCard));
        when(cardRepository.save(any(Card.class))).thenReturn(activeCard);

        Transaction savedTx = new Transaction();
        savedTx.setId(1L);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(savedTx);

        Transaction result = transactionService.makePurchase(cardId, price);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(new BigDecimal("150"), activeCard.getBalance());  // 200 - 50
        verify(cardRepository).save(activeCard);
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void testMakePurchase_CardBlocked() {
        String cardId = "12345";

        Card blocked = Card.builder()
                .cardId(cardId)
                .status(CardStatus.BLOCKED)
                .build();

        when(cardRepository.findByCardId(cardId)).thenReturn(Optional.of(blocked));

        TransactionException ex = assertThrows(
                TransactionException.class,
                () -> transactionService.makePurchase(cardId, new BigDecimal("10"))
        );

        assertEquals("La tarjeta está bloqueada", ex.getMessage());
    }

    @Test
    void testMakePurchase_CardInactive() {
        String cardId = "12345";

        Card inactive = Card.builder()
                .cardId(cardId)
                .status(CardStatus.INACTIVE)
                .build();

        when(cardRepository.findByCardId(cardId)).thenReturn(Optional.of(inactive));

        TransactionException ex = assertThrows(
                TransactionException.class,
                () -> transactionService.makePurchase(cardId, new BigDecimal("10"))
        );

        assertEquals("La tarjeta no está activa", ex.getMessage());
    }

    @Test
    void testMakePurchase_ExpiredCard() {
        String cardId = "12345";

        Card expired = Card.builder()
                .cardId(cardId)
                .status(CardStatus.ACTIVE)
                .expirationDate(LocalDate.now().minusDays(1))
                .build();

        when(cardRepository.findByCardId(cardId)).thenReturn(Optional.of(expired));

        TransactionException ex = assertThrows(
                TransactionException.class,
                () -> transactionService.makePurchase(cardId, new BigDecimal("10"))
        );

        assertEquals("La tarjeta está vencida", ex.getMessage());
    }

    @Test
    void testMakePurchase_InsufficientBalance() {
        String cardId = "12345";

        Card card = Card.builder()
                .cardId(cardId)
                .status(CardStatus.ACTIVE)
                .expirationDate(LocalDate.now().plusYears(1))
                .balance(new BigDecimal("5"))
                .build();

        when(cardRepository.findByCardId(cardId)).thenReturn(Optional.of(card));

        TransactionException ex = assertThrows(
                TransactionException.class,
                () -> transactionService.makePurchase(cardId, new BigDecimal("10"))
        );

        assertEquals("Saldo insuficiente", ex.getMessage());
    }



    // ============================================================
    //                     getTransaction()
    // ============================================================

    @Test
    void testGetTransaction_Success() {
        Transaction tx = new Transaction();
        tx.setId(10L);

        when(transactionRepository.findById(10L)).thenReturn(Optional.of(tx));

        Transaction result = transactionService.getTransaction(10L);

        assertNotNull(result);
        assertEquals(10L, result.getId());
    }

    @Test
    void testGetTransaction_NotFound() {
        when(transactionRepository.findById(1L)).thenReturn(Optional.empty());

        TransactionException ex = assertThrows(
                TransactionException.class,
                () -> transactionService.getTransaction(1L)
        );

        assertEquals("La transacción con ID 1 no existe", ex.getMessage());
    }



    // ============================================================
    //                  anulateTransaction()
    // ============================================================

    @Test
    void testAnulateTransaction_Success() {
        TransactionAnulationRequest req = new TransactionAnulationRequest();
        req.setTransactionId("1");
        req.setCardId("ABC123");

        Card card = Card.builder()
                .cardId("ABC123")
                .balance(new BigDecimal("100"))
                .build();

        Transaction tx = new Transaction();
        tx.setId(1L);
        tx.setCard(card);
        tx.setPrice(new BigDecimal("30"));
        tx.setTransactionDate(LocalDateTime.now().minusHours(1));
        tx.setAnulated(false);

        when(cardRepository.findByCardId("ABC123")).thenReturn(Optional.of(card));
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(tx));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(tx);
        when(cardRepository.save(any(Card.class))).thenReturn(card);

        Long result = transactionService.anulateTransaction(req);

        assertEquals(1L, result);
        assertTrue(tx.isAnulated());
        assertEquals(new BigDecimal("130"), card.getBalance());
    }

    @Test
    void testAnulateTransaction_NotFound() {
        TransactionAnulationRequest req = new TransactionAnulationRequest();
        req.setTransactionId("1");
        req.setCardId("ABC");

        when(cardRepository.findByCardId("ABC")).thenReturn(Optional.of(new Card()));
        when(transactionRepository.findById(1L)).thenReturn(Optional.empty());

        TransactionException ex = assertThrows(
                TransactionException.class,
                () -> transactionService.anulateTransaction(req)
        );

        assertEquals("La transacción no existe", ex.getMessage());
    }

    @Test
    void testAnulateTransaction_OtherCard() {
        TransactionAnulationRequest req = new TransactionAnulationRequest();
        req.setTransactionId("1");
        req.setCardId("AAA");

        Card card = Card.builder().cardId("AAA").build();

        Card cardOther = Card.builder().cardId("BBB").build();

        Transaction tx = new Transaction();
        tx.setId(1L);
        tx.setCard(cardOther);

        when(cardRepository.findByCardId("AAA")).thenReturn(Optional.of(card));
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(tx));

        TransactionException ex = assertThrows(
                TransactionException.class,
                () -> transactionService.anulateTransaction(req)
        );

        assertEquals("La transacción no pertenece a esta tarjeta", ex.getMessage());
    }

    @Test
    void testAnulateTransaction_AlreadyAnulated() {
        TransactionAnulationRequest req = new TransactionAnulationRequest();
        req.setTransactionId("1");
        req.setCardId("AAA");

        Card card = Card.builder().cardId("AAA").build();

        Transaction tx = new Transaction();
        tx.setId(1L);
        tx.setCard(card);
        tx.setAnulated(true);

        when(cardRepository.findByCardId("AAA")).thenReturn(Optional.of(card));
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(tx));

        TransactionException ex = assertThrows(
                TransactionException.class,
                () -> transactionService.anulateTransaction(req)
        );

        assertEquals("La transacción ya está anulada", ex.getMessage());
    }

    @Test
    void testAnulateTransaction_Exceeded24Hours() {
        TransactionAnulationRequest req = new TransactionAnulationRequest();
        req.setTransactionId("1");
        req.setCardId("AAA");

        Card card = Card.builder().cardId("AAA").build();

        Transaction tx = new Transaction();
        tx.setId(1L);
        tx.setCard(card);
        tx.setTransactionDate(LocalDateTime.now().minusHours(30));

        when(cardRepository.findByCardId("AAA")).thenReturn(Optional.of(card));
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(tx));

        TransactionException ex = assertThrows(
                TransactionException.class,
                () -> transactionService.anulateTransaction(req)
        );

        assertEquals("La transacción supera las 24 horas y no puede ser anulada", ex.getMessage());
    }
}
