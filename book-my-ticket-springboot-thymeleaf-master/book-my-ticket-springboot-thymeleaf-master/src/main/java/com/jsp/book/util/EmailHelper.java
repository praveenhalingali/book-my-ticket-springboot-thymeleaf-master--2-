package com.jsp.book.util;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class EmailHelper {

	private static final String FROM_EMAIL = "ganeshforcertification@gmail.com";
	private static final String FROM_NAME = "Book-My-Ticket";
	private static final String SUBJECT = "Otp for Creating Account with BookMyTicket";
	private static final String TEMPLATE = "email-template.html";

	private static final int MAX_RETRIES = 3;
	private static final long RETRY_DELAY_MS = 2000; // 2 seconds between retries

	private final JavaMailSender mailSender;
	private final TemplateEngine templateEngine;

	@Async
	public void sendOtp(int otp, String name, String email) {
		for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
			try {
				MimeMessage message = mailSender.createMimeMessage();
				MimeMessageHelper helper = new MimeMessageHelper(message, true);
				helper.setFrom(FROM_EMAIL, FROM_NAME);
				helper.setTo(email);
				helper.setSubject(SUBJECT);

				Context context = new Context();
				context.setVariable("name", name);
				context.setVariable("otp", otp);

				String body = templateEngine.process(TEMPLATE, context);
				helper.setText(body, true);

				mailSender.send(message);

				System.out.println("OTP email sent successfully to: " + email + " (attempt " + attempt + ")");
				return; // success - stop retrying

			} catch (Exception ex) {
				System.err.println("Attempt " + attempt + " failed to send OTP to: " + email + " | Error: " + ex.getMessage());

				if (attempt < MAX_RETRIES) {
					try {
						Thread.sleep(RETRY_DELAY_MS);
					} catch (InterruptedException ie) {
						Thread.currentThread().interrupt();
					}
				} else {
					System.err.println("All " + MAX_RETRIES + " attempts failed for email: " + email);
				}
			}
		}
	}
}