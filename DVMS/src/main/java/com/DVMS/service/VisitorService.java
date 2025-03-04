package com.DVMS.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.DVMS.entity.Visitor;
import com.DVMS.repository.VisitorRepository;
import com.DVMS.websocket.VisitorWebSocketHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class VisitorService {

    private static final Logger logger = LoggerFactory.getLogger(VisitorService.class);

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private VisitorRepository visitorRepository;

    @Autowired
    private VisitorWebSocketHandler webSocketHandler;

    @Autowired
    private EmailService emailService;

    private static AtomicLong serialNumber = new AtomicLong(1);

    public int getTotalVisitors() {
        return (int) visitorRepository.count();
    }

    public int getPendingVisitors() {
        return (int) visitorRepository.countByStatus("Pending");
    }

    public int getApprovedVisitors() {
        return (int) visitorRepository.countByStatus("Approved");
    }

    @Transactional
    public Visitor registerVisitor(Visitor visitor) {
        visitor.setStatus("Pending");
        visitor.setVisitorId(generateVisitorId());
        visitor.setRegistrationTime(LocalDateTime.now());
        visitor.setCancellationReason(null);
        visitorRepository.save(visitor);

        emailService.sendEmailToMeetWith(visitor);

        return visitor;
    }

    public void cancelExpiredRequests() {
        LocalDateTime now = LocalDateTime.now();
        List<Visitor> pendingVisitors = visitorRepository.findByStatus("Pending");

        for (Visitor visitor : pendingVisitors) {
            LocalDateTime registrationTime = visitor.getRegistrationTime();
            if (registrationTime != null) {
                logger.info("Checking visitor ID: {}, registrationTime: {}, now: {}", visitor.getId(), registrationTime, now);
                if (registrationTime.isBefore(now.minusDays(1))) {
                    visitor.setStatus("Rejected");
                    visitor.setCancellationReason("System");
                    visitorRepository.save(visitor);
                    logger.info("Visitor request with ID {} expired and was rejected.", visitor.getId());
                }
            } else {
                logger.warn("Visitor request with ID {} has a null registration time.", visitor.getId());
            }
        }
    }

    private String generateVisitorId() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyMMdd");
        String datePart = dateFormatter.format(now);
        String serialPart = String.format("%03d", serialNumber.getAndIncrement());

        return datePart + serialPart;
    }

    private void sendEmailToAdmin(Visitor visitor) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo("rag13hav@gmail.com");
        message.setSubject("New Visitor Registration");
        message.setText("A new visitor has registered: \n" +
                "Name: " + visitor.getName() + "\n" +
                "Reason: " + visitor.getReason());
        mailSender.send(message);
    }

    public Visitor approveVisitor(Long id) {
        Visitor visitor = visitorRepository.findById(id).orElseThrow();
        visitor.setStatus("Approved");
        visitor.setApprovalTime(LocalDateTime.now());
        visitor.setHostApprovedBy("Admin");
        visitorRepository.save(visitor);
        logger.info("ApproveVisitor called for visitorId: {}", id);
        try {
            logger.info("Attempting to send approval message to visitorId: {}", id);
            webSocketHandler.sendMessageToVisitor(id, "Your visitor request has been approved!");
            logger.info("Approval message sent successfully for visitorId: {}", id);
        } catch (IOException e) {
            logger.error("Error sending approval message for visitorId: {}, error: {}", id, e.getMessage());
            logger.error("Error trace:", e);
        }

        return visitor;
    }

    public Visitor rejectVisitor(Long id) {
        Visitor visitor = visitorRepository.findById(id).orElseThrow();
        visitor.setStatus("Rejected");
        visitor.setCancellationReason("Admin");
        visitorRepository.save(visitor);
        logger.info("RejectVisitor called for visitorId: {}", id);
        try {
            logger.info("Attempting to send rejection message to visitorId: {}", id);
            webSocketHandler.sendMessageToVisitor(id, "Your visitor request has been rejected.");
            logger.info("Rejection message sent successfully for visitorId: {}", id);
        } catch (IOException e) {
            logger.error("Error sending rejection message for visitorId: {}, error: {}", id, e.getMessage());
            logger.error("Error trace:", e);
        }

        return visitor;
    }

    private void sendApprovalEmailToVisitor(Visitor visitor) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(visitor.getContact());
        message.setSubject("Visitor Request Approved");
        message.setText("Dear " + visitor.getName() + ",\nYour visitor request has been approved. Please proceed with your visit.");
        mailSender.send(message);
    }

    private void sendRejectionEmailToVisitor(Visitor visitor) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(visitor.getContact());
        message.setSubject("Visitor Request Rejected");
        message.setText("Dear " + visitor.getName() + ",\nYour visitor request has been rejected. Please contact the admin for more details.");
        mailSender.send(message);
    }

    public List<Visitor> getVisitorsByStatusAndCancellationReason(String status, String cancellationReason) {
        return visitorRepository.findByStatusAndCancellationReason(status, cancellationReason);
    }

    public List<Visitor> getVisitorsByStatus(String status) {
        return visitorRepository.findByStatus(status);
    }
}