package com.thousandhyehyang.blog.service.email;

import com.thousandhyehyang.blog.entity.Post;
import com.thousandhyehyang.blog.entity.Subscriber;
import com.thousandhyehyang.blog.enums.SubscriptionStatus;
import com.thousandhyehyang.blog.exception.EmailSendException;
import com.thousandhyehyang.blog.repository.SubscriberRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final SubscriberRepository subscriberRepository;

    @Async
    public void sendNewPostNotification(Post post) {
        List<Subscriber> subscribers = subscriberRepository.findAllByStatus(SubscriptionStatus.SUBSCRIBED);
        
        for (Subscriber subscriber : subscribers) {
            try {
                Context context = new Context();
                context.setVariable("postTitle", post.getTitle());
                context.setVariable("postUrl", "https://1000hyehyang.vercel.app/" + post.getId());
                context.setVariable("email", subscriber.getEmail());
                
                String emailContent = templateEngine.process("new-post-notification", context);
                sendEmail(subscriber.getEmail(), "[새로운 게시글] " + post.getTitle(), emailContent);
                
                log.info("New post notification email sent to: {} for post: {}", 
                    subscriber.getEmail(), post.getTitle());
            } catch (Exception e) {
                log.error("Failed to send new post notification email to: {} for post: {}", 
                    subscriber.getEmail(), post.getTitle(), e);
            }
        }
    }

    @Async
    public void sendSubscriptionConfirmation(String email) {
        try {
            Context context = new Context();
            String emailContent = templateEngine.process("subscription-confirmation", context);
            sendEmail(email, "[블로그 구독] 구독 상태 알림", emailContent);
            log.info("Subscription confirmation email sent to: {}", email);
        } catch (Exception e) {
            log.error("Failed to send subscription confirmation email to: {}", email, e);
            throw new EmailSendException("이메일 발송에 실패했습니다.", e);
        }
    }

    private void sendEmail(String to, String subject, String content) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        
        helper.setFrom("ducogus12@gmail.com");
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(content, true);
        
        mailSender.send(message);
    }
} 