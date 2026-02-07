package com.library.members;

import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Nonnull;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    @Transactional(readOnly = true)
    public List<Member> getAllMembers() {
        return memberRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Member getMemberById(@Nonnull UUID id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Member not found"));
    }

    @Transactional(readOnly = true)
    public Member getMemberByEmail(@Nonnull @NotBlank @Email String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("No member found with email: " + email));
    }

    @Transactional(readOnly = true)
    public Member getMemberByNumber(long membershipNumber) {
        return memberRepository.findByMembershipNumber(membershipNumber)
                .orElseThrow(() -> new IllegalStateException("No member found with number: " + membershipNumber));
    }

    @Transactional(readOnly = true)
    public List<Member> getMembersByName(@Nonnull @NotBlank String firstName, @Nonnull @NotBlank String lastName) {
        return memberRepository.findByLastNameIgnoreCaseAndFirstNameIgnoreCase(lastName, firstName);
    }

    @Transactional
    public Member createMember(@NonNull Member member) {
        return memberRepository.save(member);
    }

    @Transactional
    public Member updateMember(@NonNull UUID id, Member details) {
        Member existing = getMemberById(id);

        existing.setFirstName(details.getFirstName());
        existing.setLastName(details.getLastName());
        existing.setEmail(details.getEmail());

        return memberRepository.save(existing);
    }

    @Transactional
    public void deleteMember(@NonNull UUID id, boolean hasActiveLoans) {
        if (hasActiveLoans) {
            throw new IllegalStateException("Cannot delete member while they have active loans");
        }

        memberRepository.deleteById(id);
    }
}