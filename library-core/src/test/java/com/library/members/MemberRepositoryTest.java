package com.library.members;

import com.library.common.BaseRepositoryTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MemberRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    @Test
    @Transactional
    @DisplayName("Should persist member and verify audit timestamps")
    void shouldSaveMemberSuccessfully() {
        Member saved = saveMember("Jane", "Doe", "jane.doe@library.com");

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    @Transactional
    @DisplayName("Should find member by email")
    void shouldFindByEmail() {
        saveMember("Search", "User", "find@test.com");

        Optional<Member> found = memberRepository.findByEmail("find@test.com");

        assertThat(found).isPresent();
        assertThat(found.get().getFirstName()).isEqualTo("Search");
    }

    @Test
    @Transactional
    @DisplayName("Should find members by last name ignoring case")
    void shouldFindByLastName() {
        saveMember("Alice", "Smith", "alice@test.com");
        saveMember("Bob", "SMITH", "bob@test.com");

        List<Member> results = memberRepository.findByLastNameIgnoreCaseAndFirstNameIgnoreCase("smith", "alice");

        assertThat(results).hasSize(1);
        assertThat(results).extracting(Member::getFirstName).containsExactlyInAnyOrder("Alice");
    }

    @Test
    @Transactional
    @DisplayName("Should fail when email is duplicated")
    void shouldFailOnDuplicateEmail() {
        saveMember("User1", "Test", "conflict@test.com");

        assertThrows(DataIntegrityViolationException.class, () ->
            saveMember("User2", "Test", "conflict@test.com")
        );
    }

    private Member saveMember(String first, String last, String email) {
        Member member = new Member();
        member.setFirstName(first);
        member.setLastName(last);
        member.setEmail(email);
        return memberRepository.saveAndFlush(member);
    }
}