package com.library.books;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BookRepository extends JpaRepository<Book, UUID> {

       Optional<Book> findByIsbn(String isbn);

       List<Book> findByTitleContainingIgnoreCase(String title);

       List<Book> findByAuthorContainingIgnoreCase(String author);

       // atomic queries, prevent issues with concurrency
       @Modifying
       @Transactional
       @Query("""
                UPDATE Book b
                SET b.availableCopies = b.availableCopies - 1
                WHERE b.id = :bookId AND b.availableCopies > 0
              """)
       int decrementAvailableCopies(@Param("bookId") UUID bookId);

       @Modifying
       @Transactional
       @Query("""
                UPDATE Book b
                SET b.availableCopies = b.availableCopies + 1
                WHERE b.id = :bookId
                """)
       int incrementAvailableCopies(@Param("bookId") UUID bookId);
}