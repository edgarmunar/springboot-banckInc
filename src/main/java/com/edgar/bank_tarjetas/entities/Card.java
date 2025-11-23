package com.edgar.bank_tarjetas.entities;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Card {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Primeros 6 dígitos del número de la tarjeta
    @Column(nullable = false, length = 6)
    private String productId;

    // Número completo: 6 productId + 10 generados
    @Column(nullable = false, length = 16, unique = true)
    private String cardId;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, length = 50)
    private String lastName;

    // Fecha de creación
    @Column(nullable = false)
    private LocalDate createdAt;

    // Expira 3 años después de createdAt
    @Column(nullable = false)
    private LocalDate expirationDate;

    // USD solamente
    @Column(nullable = false)
    private BigDecimal balance;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CardStatus status;

}