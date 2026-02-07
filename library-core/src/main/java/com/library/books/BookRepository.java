package com.library.books;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BookRepository extends JpaRepository<Book, UUID> {
    Optional<Book> findByIsbn(String isbn);

    List<Book> findByTitleContainingIgnoreCase(String title);
    List<Book> findByAuthorContainingIgnoreCase(String author);

    Optional<Book> findByBookNumber(long bookNumber);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Book b SET b.availableCopies = b.availableCopies - 1 " +
           "WHERE b.id = :id AND b.availableCopies > 0")
    int decrementAvailableCopies(UUID id);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Book b SET b.availableCopies = b.availableCopies + 1 " +
           "WHERE b.id = :id AND b.availableCopies < b.totalCopies")
    int incrementAvailableCopies(UUID id);
}