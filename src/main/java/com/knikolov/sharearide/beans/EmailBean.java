package com.knikolov.sharearide.beans;

import com.cloudinary.Cloudinary;
import com.knikolov.sharearide.security.services.UserDetailsServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
public class EmailBean {

    @Bean
    public JavaMailSender getJavaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost("smtp.gmail.com");
        mailSender.setPort(587);

        mailSender.setUsername("share.a.ride.nbu");
        mailSender.setPassword("shar3ar1de");

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.debug", "true");

        return mailSender;
    }

    @Bean
    public Cloudinary cloudinary() {
        return new Cloudinary("cloudinary://217767699556896:JVB83ZLGGFv_TiB2QUhWz8VI07E@dfbmpnqdu/");
    }

}
