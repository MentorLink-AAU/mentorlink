package com.mentorlink.modules.notifications.repository;

import com.mentorlink.modules.notifications.entity.Notification;
import com.mentorlink.modules.users.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserOrderByIdDesc(User user, org.springframework.data.domain.Pageable pageable);
    long countByUserAndReadFalse(User user);
}
