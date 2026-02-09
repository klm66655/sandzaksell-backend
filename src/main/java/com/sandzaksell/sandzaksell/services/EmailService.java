package com.sandzaksell.sandzaksell.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;
import java.util.Properties;

@Service
public class EmailService {
    @Autowired
    private JavaMailSender mailSender;

    public void sendResetEmail(String to, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("kelimselmanovic123@gmail.com");
        message.setTo(to);
        message.setSubject("Kod za reset lozinke");
        message.setText("Vaš kod je: " + code);
        mailSender.send(message);
    }
}