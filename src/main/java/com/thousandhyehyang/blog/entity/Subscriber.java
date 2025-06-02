package com.thousandhyehyang.blog.entity;

import com.thousandhyehyang.blog.enums.SubscriptionStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "subscribers")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Subscriber extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubscriptionStatus status;

    public Subscriber(String email) {
        this.email = email;
        this.status = SubscriptionStatus.SUBSCRIBED;
    }

    public void updateStatus(SubscriptionStatus status) {
        this.status = status;
    }
}