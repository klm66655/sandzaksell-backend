package com.sandzaksell.sandzaksell.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendResetEmail(String to, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("kelimselmanovic123@gmail.com");
        message.setTo(to);
        message.setSubject("Sandžak Sell - Kod za reset lozinke");
        message.setText("Vaš kod za resetovanje lozinke je: " + code + "\nKod važi 15 minuta.");
        mailSender.send(message);
    }
}