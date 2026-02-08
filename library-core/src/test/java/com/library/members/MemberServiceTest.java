package com.library.members;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private MemberService memberService;

    private Member sampleMember;
    private UUID memberId;

    @BeforeEach
    void setUp() {
        memberId = UUID.randomUUID();

        sampleMember = new Member();
        sampleMember.setId(memberId);
        sampleMember.setFirstName("John");
        sampleMember.setLastName("Doe");
        sampleMember.setEmail("john.doe@example.com");
    }

    @Test
    @DisplayName("Should return member when ID exists")
    void getMemberById_Success() {
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(sampleMember));

        Member result = memberService.getMemberById(memberId);

        assertThat(result).isSameAs(sampleMember);
        assertThat(result.getEmail()).isEqualTo("john.doe@example.com");

        verify(memberRepository).findById(memberId);
        verifyNoMoreInteractions(memberRepository);
    }

    @Test
    @DisplayName("Should throw IllegalStateException when member not found")
    void getMemberById_NotFound() {
        when(memberRepository.findById(memberId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> memberService.getMemberById(memberId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Member not found");

        verify(memberRepository).findById(memberId);
        verifyNoMoreInteractions(memberRepository);
    }

    @Test
    @DisplayName("Should update member details correctly")
    void updateMember_Success() {
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(sampleMember));
        when(memberRepository.save(any(Member.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Member updates = new Member();
        updates.setFirstName("Jane");
        updates.setLastName("Smith");
        updates.setEmail("jane.smith@example.com");

        Member result = memberService.updateMember(memberId, updates);

        assertThat(result.getId()).isEqualTo(memberId);
        assertThat(result.getFirstName()).isEqualTo("Jane");
        assertThat(result.getLastName()).isEqualTo("Smith");
        assertThat(result.getEmail()).isEqualTo("jane.smith@example.com");

        verify(memberRepository).findById(memberId);
        verify(memberRepository).save(sampleMember);
        verifyNoMoreInteractions(memberRepository);
    }

    @Test
    @DisplayName("Should call delete when no active loans exist")
    void deleteMember_Success() {
        memberService.deleteMember(memberId, false);

        verify(memberRepository).deleteById(memberId);
        verifyNoMoreInteractions(memberRepository);
    }

    @Test
    @DisplayName("Should throw and not call repository when member has active loans")
    void deleteMember_Failure_ActiveLoans() {
        assertThatThrownBy(() -> memberService.deleteMember(memberId, true))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot delete member while they have active loans");

        verify(memberRepository, never()).deleteById(any());
        verifyNoInteractions(memberRepository);
    }
}
