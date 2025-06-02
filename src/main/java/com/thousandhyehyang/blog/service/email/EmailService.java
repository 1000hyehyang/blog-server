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
        log.info("📩 [START] 새 게시글 알림 이메일 발송 시작: 제목={}, 작성자={}", post.getTitle(), post.getAuthor());

        try {
            List<Subscriber> subscribers = subscriberRepository.findAllByStatus(SubscriptionStatus.SUBSCRIBED);
            log.info("✅ 구독자 수: {}", subscribers.size());

            String domain = "https://1000hyehyang.vercel.app";

            for (Subscriber subscriber : subscribers) {
                String to = subscriber.getEmail();
                String subject = "[블로그] 새 글이 올라왔어요: " + post.getTitle();

                String postLink = domain + "/posts/" + post.getId();
                String unsubscribeLink = domain + "/api/subscriptions/" + subscriber.getEmail();
                String blogLink = domain;

                Context context = new Context();
                context.setVariable("postTitle", post.getTitle());
                context.setVariable("postLink", postLink);
                context.setVariable("unsubscribeLink", unsubscribeLink);
                context.setVariable("blogLink", blogLink);

                String content = templateEngine.process("new-post-notification", context);

                log.info("📤 이메일 전송 시도: to={}", to);

                try {
                    sendEmail(to, subject, content);
                    log.info("✅ 이메일 전송 성공: {}", to);
                } catch (Exception e) {
                    log.error("❌ 이메일 전송 실패: {} - {}", to, e.getMessage(), e);
                }
            }

            log.info("📩 [END] 새 게시글 알림 이메일 발송 완료");

        } catch (Exception e) {
            log.error("🚨 이메일 알림 전체 실패: {}", e.getMessage(), e);
        }
    }

    @Async
    public void sendSubscriptionConfirmation(String email) {
        try {
            Context context = new Context();
            context.setVariable("message", "블로그 구독이 정상적으로 완료되었습니다.");
            context.setVariable("blogLink", "https://1000hyehyang.vercel.app"); 

            String emailContent = templateEngine.process("subscription-confirmation", context);

            sendEmail(email, "[블로그 구독] 구독 상태 알림", emailContent);
            log.info("Subscription confirmation email sent to: {}", email);
        } catch (Exception e) {
            log.error("Failed to send subscription confirmation email to: {}", email, e);
            throw new EmailSendException("이메일 발송에 실패했습니다.", e);
        }
    }

    private void sendEmail(String to, String subject, String content) throws MessagingException {
        log.info("📤 Sending email to {}", to);
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        
        helper.setFrom("ducogus12@gmail.com");
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(content, true);
        
        mailSender.send(message);
    }
} 