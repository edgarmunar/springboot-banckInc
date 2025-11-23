package com.edgar.bank_tarjetas.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import com.edgar.bank_tarjetas.entities.Card;

public interface CardRepository extends JpaRepository<Card, Long> {
    
    // Buscar tarjeta por número
    Optional<Card> findByCardId(String cardId);
}