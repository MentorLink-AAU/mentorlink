// src/main/java/com/mentorlink/modules/users/UserRepository.java
package com.mentorlink.modules.users;

import com.mentorlink.modules.users.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    Optional<User> findByEmailIgnoreCase(String email);

    Optional<User> findByFullNameIgnoreCase(String fullName);
}
