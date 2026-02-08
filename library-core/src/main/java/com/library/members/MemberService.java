package com.library.members;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Nonnull;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Application service responsible for managing {@link Member} entities.
 *
 * <p>
 * This service provides read and write operations for library members,
 * enforces basic business rules, and acts as the transactional boundary
 * for member-related use cases.
 * </p>
 *
 * <p>
 * Method-level validation is enabled via {@link Validated}, allowing
 * Jakarta Bean Validation constraints on method parameters to be enforced.
 * </p>
 */
@Service
@Validated
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    /**
     * Retrieves all registered members.
     *
     * @return a list of all members in the system
     */
    @Transactional(readOnly = true)
    public List<Member> getAllMembers() {
        return memberRepository.findAll();
    }

    /**
     * Retrieves a member by their unique identifier.
     *
     * @param id the member's UUID
     * @return the matching member
     * @throws IllegalStateException if no member exists with the given id
     */
    @Transactional(readOnly = true)
    public Optional<Member> getMemberById(@Nonnull UUID id) {
        return memberRepository.findById(id);
    }

    /**
     * Retrieves a member by their email address.
     *
     * @param email the member's email address (must be non-blank and valid)
     * @return the matching member
     * @throws IllegalStateException if no member exists with the given email
     */
    @Transactional(readOnly = true)
    public Optional<Member> getMemberByEmail(@Nonnull @NotBlank @Email String email) {
        return memberRepository.findByEmail(email);
    }

    /**
     * Retrieves members matching the provided first and last name, ignoring case.
     *
     * @param firstName the member's first name
     * @param lastName  the member's last name
     * @return a list of matching members (may be empty)
     */
    @Transactional(readOnly = true)
    public List<Member> getMembersByName(
            @Nonnull @NotBlank String firstName,
            @Nonnull @NotBlank String lastName) {
        return memberRepository.findByLastNameIgnoreCaseAndFirstNameIgnoreCase(lastName, firstName);
    }

    /**
     * Creates and persists a new member.
     *
     * @param member the member to create
     * @return the persisted member instance
     */
    @Transactional
    public Member createMember(@Nonnull Member member) {
        return memberRepository.save(member);
    }

    /**
     * Updates mutable fields of an existing member.
     *
     * <p>
     * This method performs a read-modify-write cycle to ensure updates
     * are applied to a managed entity.
     * </p>
     *
     * @param id      the id of the member to update
     * @param details the new member details
     * @return the updated member
     * @throws IllegalStateException if the member does not exist
     */
    @Transactional
    public Member updateMember(@Nonnull Member existingMember, Member updatedMember) {

        existingMember.setFirstName(updatedMember.getFirstName());
        existingMember.setLastName(updatedMember.getLastName());
        existingMember.setEmail(updatedMember.getEmail());

        return memberRepository.save(existingMember);
    }

    /**
     * Deletes a member if they have no active loans.
     *
     * @param id             the id of the member to delete
     * @param hasActiveLoans whether the member currently has active loans
     * @throws IllegalStateException if the member has active loans
     */
    @Transactional
    public void deleteMember(@Nonnull UUID id, boolean hasActiveLoans) {
        if (hasActiveLoans) {
            throw new IllegalStateException("Cannot delete member while they have active loans");
        }

        memberRepository.deleteById(id);
    }

    /**
     * Retrieves a paginated list of all members.
     *
     * @param pageable pagination and sorting information
     * @return a page of {@link Member} entities
     */
    @Transactional(readOnly = true)
    public Page<Member> getMembers(@Nonnull Pageable pageable) {
        return memberRepository.findAll(pageable);
    }
}
