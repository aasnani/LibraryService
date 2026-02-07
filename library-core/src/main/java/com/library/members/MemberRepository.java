package com.library.members;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberRepository extends JpaRepository<Member, UUID> {

    Optional<Member> findByEmail(String email);

    Optional<Member> findByMembershipNumber(Long membershipNumber);

    List<Member> findByLastNameIgnoreCaseAndFirstNameIgnoreCase(String lastName, String firstName);
}