package com.edgar.bank_tarjetas.repositories;

import com.edgar.bank_tarjetas.entities.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

}