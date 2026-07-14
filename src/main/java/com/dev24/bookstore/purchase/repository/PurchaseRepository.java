package com.dev24.bookstore.purchase.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dev24.bookstore.purchase.domain.Purchase;

public interface PurchaseRepository extends JpaRepository<Purchase, Long> {
}
