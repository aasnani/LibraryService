package com.library.loans;

/**
 * Projection interface providing aggregated loan statistics for a library member.
 *
 * <p>This projection is intended for read-only use in borrowing and policy
 * enforcement workflows, allowing multiple loan-related facts to be retrieved
 * in a single database query without loading full {@link Loan} entities.
 *
 * <p>Typical use cases include:
 * <ul>
 *   <li>Enforcing maximum active loan limits</li>
 *   <li>Blocking borrowing when overdue loans exist</li>
 *   <li>Preventing duplicate checkouts of the same book</li>
 * </ul>
 */
public interface LoanStatsProjection {

    /**
     * @return the number of active (not yet returned) loans for the member
     */
    long getActiveLoans();

    /**
     * @return the number of active loans that are overdue based on calendar date
     */
    long getOverdueLoans();

    /**
     * Indicates whether the member currently has an active loan
     * for the specified book.
     *
     * @return {@code true} if an active loan for the book exists, {@code false} otherwise
     */
    boolean getHasThisBook();
}
