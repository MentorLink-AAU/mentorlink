package com.mentorlink.modules.users.service;

import com.mentorlink.modules.users.UserRepository;
import com.mentorlink.modules.users.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

/** Loads User by email for Spring Security authentication. */
@Service("customUserDetailsService")
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        // Convert User entity to Spring Security UserDetails
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())  // use email for login
                .password(user.getPassword())
                .authorities(
                        user.getRoles().stream()
                                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                                .collect(Collectors.toList())
                )
                .build();
    }
}
