package com.edgar.bank_tarjetas.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "card_fk", nullable = false)
    private Card card;

    @Column(nullable = false, length = 16)
    private String cardId;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(nullable = false)
    private LocalDateTime transactionDate;

    @Column(nullable = false)
    private boolean anulated = false;

    private LocalDateTime anulatedAt;
}