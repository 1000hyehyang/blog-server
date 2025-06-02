package com.thousandhyehyang.blog.repository;

import com.thousandhyehyang.blog.entity.Subscriber;
import com.thousandhyehyang.blog.enums.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriberRepository extends JpaRepository<Subscriber, Long> {
    Optional<Subscriber> findByEmail(String email);
    List<Subscriber> findAllByStatus(SubscriptionStatus status);
    boolean existsByEmailAndStatus(String email, SubscriptionStatus subscriptionStatus);
}