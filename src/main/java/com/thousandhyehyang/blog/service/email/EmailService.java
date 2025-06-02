package com.thousandhyehyang.blog.service.email;

import com.thousandhyehyang.blog.entity.Post;
import com.thousandhyehyang.blog.entity.Subscriber;
import com.thousandhyehyang.blog.enums.SubscriptionStatus;
import com.thousandhyehyang.blog.exception.EmailSendException;
import com.thousandhyehyang.blog.repository.SubscriberRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.UnsupportedEncodingException;
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
        log.info("[START] 새 게시글 알림 이메일 발송 시작: 제목={}, 작성자={}", post.getTitle(), post.getAuthor());

        try {
            List<Subscriber> subscribers = subscriberRepository.findAllByStatus(SubscriptionStatus.SUBSCRIBED);
            log.info("구독자 수: {}", subscribers.size());

            String domain = "https://1000hyehyang.vercel.app";

            for (Subscriber subscriber : subscribers) {
                String to = subscriber.getEmail();
                String subject = post.getTitle();

                String html = post.getHtml();
                Document doc = Jsoup.parse(html);

                Context context = new Context();
                context.setVariable("postThumbnail", post.getThumbnailUrl());
                context.setVariable("postTitle", post.getTitle());

                // 앞부분에 해당하는 블록 요소 최대 3개만 추출
                Elements blocks = doc.body().children();
                StringBuilder excerptBuilder = new StringBuilder();
                int maxBlocks = 3;
                for (int i = 0; i < Math.min(maxBlocks, blocks.size()); i++) {
                    Element block = blocks.get(i);
                    excerptBuilder.append(block.outerHtml());
                }
                context.setVariable("postExcerpt", excerptBuilder.toString());
                context.setVariable("postLink", "https://1000hyehyang.vercel.app/post/" + post.getId());

                String content = templateEngine.process("new-post-notification", context);

                log.info("이메일 전송 시도: to={}", to);

                try {
                    sendEmail(to, subject, content);
                    log.info("이메일 전송 성공: {}", to);
                } catch (Exception e) {
                    log.error("이메일 전송 실패: {} - {}", to, e.getMessage(), e);
                }
            }

            log.info("[END] 새 게시글 알림 이메일 발송 완료");

        } catch (Exception e) {
            log.error("이메일 알림 전체 실패: {}", e.getMessage(), e);
        }
    }

    @Async
    public void sendSubscriptionConfirmation(String email) {
        try {
            Context context = new Context();
            context.setVariable("message", "블로그 구독이 정상적으로 완료되었습니다.");
            context.setVariable("blogLink", "https://1000hyehyang.vercel.app");

            String emailContent = templateEngine.process("subscription-confirmation", context);

            sendEmail(email, "[천혜향 블로그] 구독 감사합니다 :)", emailContent);
            log.info("Subscription confirmation email sent to: {}", email);
        } catch (Exception e) {
            log.error("Failed to send subscription confirmation email to: {}", email, e);
            throw new EmailSendException("이메일 발송에 실패했습니다.", e);
        }
    }

    private void sendEmail(String to, String subject, String content) throws MessagingException, UnsupportedEncodingException {
        log.info("📤 Sending email to {}", to);
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(new InternetAddress("ducogus12@gmail.com", "천혜향", "UTF-8"));
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(content, true);

        mailSender.send(message);
    }
} 