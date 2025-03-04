package com.DVMS.service;

import com.DVMS.config.MeetWithProperties;
import com.DVMS.entity.Visitor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

	@Autowired
	private JavaMailSender mailSender;
	
	@Autowired
    private MeetWithProperties meetWithProperties;
	
	@Async
    public void sendEmailToMeetWith(Visitor visitor) {
        String meetWith = visitor.getMeetWith();
        String recipientEmail = null;

        switch (meetWith.toLowerCase()) {
            case "director":
                recipientEmail = meetWithProperties.getDirectorEmail();
                break;
            case "manager":
                recipientEmail = meetWithProperties.getManagerEmail();
                break;
            case "faculty":
                recipientEmail = meetWithProperties.getFacultyEmail();
                break;
            case "staff":
                recipientEmail = meetWithProperties.getStaffEmail();
                break;
            default:
                System.err.println("Unknown meetWith value: " + meetWith);
                return;
        }

        if (recipientEmail != null) {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(recipientEmail);
            message.setSubject("New Visitor Request");
            message.setText("A new visitor has requested to meet with you:\n\n" +
                    "Name: " + visitor.getName() + "\n" +
                    "Reason: " + visitor.getReason());
            mailSender.send(message);
        }
    }

	public void sendEmailToVisitor(Visitor visitor, String messageContent) {
	    String visitorEmail = visitor.getContact();  // Assuming contact is email
	    String subject = "Visitor Request Status";
	    String body = "Hello " + visitor.getName() + ",\n\n" + messageContent;

	    SimpleMailMessage message = new SimpleMailMessage();
	    message.setTo(visitorEmail);
	    message.setSubject(subject);
	    message.setText(body);
	    mailSender.send(message);
	}

	public void sendEmailToAdmin(Visitor visitor) {
	    SimpleMailMessage message = new SimpleMailMessage();
	    message.setTo("rag13hav@gmail.com");  // Admin's email
	    message.setSubject("New Visitor Registration Request");
	    message.setText("A new visitor has registered: \n" +
	                    "Name: " + visitor.getName() + "\n" +
	                    "Reason: " + visitor.getReason());
	    mailSender.send(message);
	}
}
