package com.sandzaksell.sandzaksell.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import java.util.Properties;

@Configuration
public class MailConfig {

    @Bean
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();

        // 1. Brevo Host
        mailSender.setHost("smtp-relay.brevo.com");

        // 2. Port 2525 zaobilazi Railway firewall
        mailSender.setPort(2525);

        // 3. Tvoj Brevo Login (obično tvoj email)
        mailSender.setUsername("a1f64f001@smtp-brevo.com");

        // 4. Tvoj Brevo SMTP KEY (dugačak niz karaktera)
        mailSender.setPassword("xsmtpsib-012170f3ad8e802f0ab39a6964a746391d41bbbf0a2cbb0126a4186c720676e1-2BBkbRvCISBJ41Vd");

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");

        // Za port 2525 na Brevo-u koristimo STARTTLS
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.starttls.required", "true");

        // Debug ostavi na true da vidiš u Railway logovima ako krene po zlu
        props.put("mail.debug", "true");

        return mailSender;
    }
}