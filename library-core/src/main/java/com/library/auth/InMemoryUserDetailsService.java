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

/**
 * Loads users from `auth/users.json` in the classpath.
 * Passwords are plain-text.
 */
@Service
public class InMemoryUserDetailsService implements UserDetailsService {

    private final List<JsonUser> jsonUsers;

    public InMemoryUserDetailsService(ObjectMapper mapper) {
        try (InputStream is = new ClassPathResource("auth/users.json").getInputStream()) {
            this.jsonUsers = mapper.readValue(is, new TypeReference<>() {});
        } catch (Exception e) {
            throw new RuntimeException("Failed to load users.json", e);
        }
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        JsonUser user = jsonUsers.stream()
                .filter(u -> u.username.equals(username))
                .findFirst()
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        return User.builder()
                .username(user.username)
                .password(user.password)
                .roles(user.roles.toArray(new String[0]))
                .build();
    }

    @Data
    static class JsonUser {
        public String username;
        public String password;
        public List<String> roles;
    }
}