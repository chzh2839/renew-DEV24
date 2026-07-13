package com.dev24.bookstore.book.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.dev24.bookstore.book.domain.Book;

public interface BookRepository extends JpaRepository<Book, Long>, BookQueryRepository {

    boolean existsByIsbn(String isbn);

    @Query("select b from Book b left join fetch b.bookImage left join fetch b.rating where b.id = :id")
    Optional<Book> findByIdWithDetails(@Param("id") Long id);
}
