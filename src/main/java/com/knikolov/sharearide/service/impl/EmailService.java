package com.knikolov.sharearide.service.impl;

import com.knikolov.sharearide.models.Route;
import com.knikolov.sharearide.models.RouteStop;
import com.knikolov.sharearide.repository.RouteRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmailService {

    @Value("${enable.email.service}")
    private Boolean enableEmailService;

    private final JavaMailSender emailSender;
    private final RouteRepository routeRepository;

    public EmailService(JavaMailSender javaMailSender, RouteRepository routeRepository) {
        this.emailSender = javaMailSender;
        this.routeRepository = routeRepository;
    }

    @Async
    public void sendTestEmail() {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo("kiro972@gmail.com");
        message.setSubject("Test email");
        message.setText("just some text");
        if (enableEmailService) {
            emailSender.send(message);
        }
    }

    @Async
    public void sendEmailForSavingASeat(String driverEmail, String passengerUsername) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(driverEmail);
        message.setSubject("Saved a seat");
        message.setText("Passenger with username: " + passengerUsername +
                " just saved a seat for one of your routes! Log in and approve the passenger for your route!");
        if (enableEmailService) {
            emailSender.send(message);
        }
    }

    @Async
    public void sendEmailResponseForSavedSeat(String passengerEmail, String driverUsername, boolean approved) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(passengerEmail);

        if (approved) {
            message.setSubject("Approved seat");
            message.setText("Driver with username: " + driverUsername + " just approved your seat on the route!");
        } else {
            message.setSubject("Declined seat");
            message.setText("Driver with username: " + driverUsername + " just declined your seat on the route!");
        }
        if (enableEmailService) {
            emailSender.send(message);
        }
    }

    @Async
    public void sendEmailsForCanceledRoute(String routeId, String driverUsername) {
        Route route = this.routeRepository.findById(routeId).orElse(null);

        if (route != null) {
            List<RouteStop> routeStops = route.getRouteStops();
            for (RouteStop rs : routeStops) {
                sendEmailForCanceledRoute(rs.getUser().getEmail(), driverUsername);
            }
        }
    }

    private void sendEmailForCanceledRoute(String passengerEmail, String driverUsername) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(passengerEmail);
        message.setSubject("Canceled Route");
        message.setText("Driver with username: " + driverUsername + " just canceled the route!");
        if (enableEmailService) {
            emailSender.send(message);
        }
    }

    @Async
    public void sendEmailForDeletedRouteStop(String driverEmail) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(driverEmail);
        message.setSubject("Canceled Route Stop");
        message.setText("Someone just canceled their route stop in one of your routes!");
        if (enableEmailService) {
            emailSender.send(message);
        }
    }
}
