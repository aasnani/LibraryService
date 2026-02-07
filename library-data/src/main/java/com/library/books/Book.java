package com.library.books;

import com.library.common.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "books", indexes = {
    @Index(name = "idx_book_isbn", columnList = "isbn", unique = true),
    @Index(name = "idx_book_title", columnList = "title"),
    @Index(name = "idx_book_author", columnList = "author")
})
@Getter
@Setter
public class Book extends BaseEntity {
    @Column(nullable = false)
    private String title;
    @Column(nullable = false)
    private String author;

    @Column(unique = true, nullable = false)
    private String isbn;

    private int totalCopies;
    private int availableCopies;

    @Column(unique = true, nullable = false)
    private Long bookNumber;
}