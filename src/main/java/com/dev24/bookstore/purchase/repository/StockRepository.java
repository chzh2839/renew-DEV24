package com.dev24.bookstore.purchase.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dev24.bookstore.purchase.domain.Stock;

public interface StockRepository extends JpaRepository<Stock, Long> {

    Optional<Stock> findByBookId(Long bookId);
}
