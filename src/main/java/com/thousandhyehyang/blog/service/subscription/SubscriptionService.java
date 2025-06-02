package com.thousandhyehyang.blog.service.subscription;

import com.thousandhyehyang.blog.entity.Subscriber;
import com.thousandhyehyang.blog.enums.SubscriptionStatus;
import com.thousandhyehyang.blog.exception.DuplicateSubscriptionException;
import com.thousandhyehyang.blog.exception.SubscriberNotFoundException;
import com.thousandhyehyang.blog.repository.SubscriberRepository;
import com.thousandhyehyang.blog.service.email.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final SubscriberRepository subscriberRepository;
    private final EmailService emailService;

    @Transactional
    public void subscribe(String email) {
        // 이미 구독 중인 경우 예외 처리
        boolean isSubscribed = subscriberRepository.existsByEmailAndStatus(email, SubscriptionStatus.SUBSCRIBED);
        if (isSubscribed) {
            throw new DuplicateSubscriptionException("이미 구독 중인 이메일입니다: " + email);
        }

        // 이전에 구독했다가 해지한 경우 상태만 변경
        subscriberRepository.findByEmail(email)
                .ifPresentOrElse(subscriber -> {
                    subscriber.updateStatus(SubscriptionStatus.SUBSCRIBED);
                }, () -> {
                    // 최초 구독일 경우 새로 생성
                    Subscriber newSubscriber = Subscriber.builder()
                            .email(email)
                            .status(SubscriptionStatus.SUBSCRIBED)
                            .build();
                    subscriberRepository.save(newSubscriber);
                });

        // 확인 이메일 발송
        emailService.sendSubscriptionConfirmation(email);
    }

    @Transactional
    public void unsubscribe(String email) {
        Subscriber subscriber = subscriberRepository.findByEmail(email)
                .orElseThrow(() -> new SubscriberNotFoundException("구독자를 찾을 수 없습니다: " + email));
        subscriber.updateStatus(SubscriptionStatus.UNSUBSCRIBED);
    }

    @Transactional(readOnly = true)
    public List<Subscriber> getActiveSubscribers() {
        return subscriberRepository.findAllByStatus(SubscriptionStatus.SUBSCRIBED);
    }
}