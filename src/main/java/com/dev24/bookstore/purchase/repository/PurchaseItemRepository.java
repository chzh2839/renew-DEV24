package com.dev24.bookstore.purchase.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dev24.bookstore.purchase.domain.PurchaseItem;

public interface PurchaseItemRepository extends JpaRepository<PurchaseItem, Long> {
}
