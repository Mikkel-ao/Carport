package app.services;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;

public class EmailService {

    public static void sendEmail(String customerEmail) {
        //Using environment variables to store our secret email and password for security reasons!
        final String senderEmail = System.getenv("EMAIL");
        final String senderPassword = System.getenv("EMAIL_PASSWORD");
        //Hard coding the subject and message body of the email, as we only need a generic email for now!
        final String subject = "Fog Carport - Ordrestatus Opdateret";
        final String messageBody = "Kære kunde. Der er blevet oprettet et tilbud på din carport bestilling.\n\nLog ind på din Fog-konto for at se tilbuddet.\n\nMed Venlig Hilsen\n\nFog";


        // SMTP Configuration for Outlook
        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", "smtp.office365.com");
        properties.put("mail.smtp.port", "587");

        // Create session with authentication
        Session session = Session.getInstance(properties, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(senderEmail, senderPassword);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(senderEmail));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(customerEmail));
            message.setSubject(subject);
            message.setText(messageBody);

            // Send email
            Transport.send(message);

        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}
