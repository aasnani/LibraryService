package com.library.auth;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class InMemoryUserDetailsService implements UserDetailsService {

    private final List<UserDetails> users;

    public InMemoryUserDetailsService(ObjectMapper mapper) {
        try (InputStream is = new ClassPathResource("users.json").getInputStream()) {

            List<JsonUser> jsonUsers =
                    mapper.readValue(is, new TypeReference<>() {});

            this.users = jsonUsers.stream()
                    .map(u -> User.builder()
                            .username(u.username)
                            .password(u.password)
                            .roles(u.roles.toArray(new String[0]))
                            .build())
                    .collect(Collectors.toList());

        } catch (Exception e) {
            throw new RuntimeException("Failed to load users.json", e);
        }
    }

    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

        return users.stream()
                .filter(u -> u.getUsername().equals(username))
                .findFirst()
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found"));
    }

    @Data
    static class JsonUser {
        public String username;
        public String password;
        public List<String> roles;
    }
}
