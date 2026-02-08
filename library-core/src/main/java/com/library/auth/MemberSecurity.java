package com.library.auth;

import com.library.members.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class MemberSecurity {

    private final MemberRepository memberRepository;

    public boolean isOwner(UUID memberId, String username) {
        return memberRepository.findById(memberId)
                .map(m -> m.getEmail().equals(username))
                .orElse(false);
    }
}
