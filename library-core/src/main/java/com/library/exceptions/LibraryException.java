package com.library.exceptions;

/**
 * Root exception for all library domain errors.
 *
 * <p>
 * This exception centralizes all application-specific errors across books,
 * members, and library operations. Use the {@link LibraryExceptionType} enum
 * to categorize specific failure reasons for consistent handling in a
 * controller advice or service layer.
 */
public class LibraryException extends RuntimeException {

    private final LibraryExceptionType type;

    public LibraryException(LibraryExceptionType type, String message) {
        super(message);
        this.type = type;
    }

    public LibraryException(LibraryExceptionType type, String message, Throwable cause) {
        super(message, cause);
        this.type = type;
    }

    public LibraryExceptionType getType() {
        return type;
    }

    /**
     * Enum defining all library-specific error types.
     *
     * <p>
     * Can be used by ControllerAdvice to map exceptions to HTTP responses.
     */
    public enum LibraryExceptionType {

        /**
         * Thrown when a book is requested but does not exist in the system.
         */
        BOOK_NOT_FOUND,

        /**
         * Thrown when attempting to reduce the total copies of a book below
         * the number of currently active loans.
         */
        BOOK_CANNOT_REDUCE_COPIES,

        /**
         * Thrown when there are no available copies of a book for checkout.
         */
        BOOK_NO_AVAILABLE_COPIES,

        /**
         * Thrown when a member is requested but does not exist in the system.
         */
        MEMBER_NOT_FOUND,

        /**
         * Thrown when attempting to return a book that has no active loan
         * record for the member.
         */
        LIBRARY_NO_ACTIVE_LOAN,

        /**
         * Thrown when attempting to delete a book that still has copies
         * on loan.
         */
        LIBRARY_BOOK_CANNOT_DELETE_ACTIVE_LOANS,

        /**
         * Thrown when a library member has reached the maximum number
         * of active loans allowed by policy.
         */
        LIBRARY_MEMBER_MAX_ACTIVE_LOANS_EXCEEDED,

        /**
         * Thrown when attempting to delete a member who currently
         * has active loans.
         */
        LIBRARY_MEMBER_CANNOT_DELETE_ACTIVE_LOANS,

        /**
         * Thrown when a member tries to borrow a book they already have
         * checked out.
         */
        LIBRARY_MEMBER_ALREADY_HAS_BOOK,

        /**
         * Thrown when a member is blocked from borrowing due to
         * exceeding the overdue loan threshold.
         */
        LIBRARY_MEMBER_BLOCKED_DUE_TO_OVERDUE
    }
}
