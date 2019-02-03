package net.offbeatpioneer.intellij.plugins.grav.reporting;

import com.intellij.openapi.diagnostic.ErrorReportSubmitter;
import com.intellij.openapi.diagnostic.IdeaLoggingEvent;
import com.intellij.openapi.diagnostic.SubmittedReportInfo;
import com.intellij.util.Consumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;

import java.io.*;
import java.util.Arrays;
import java.util.Properties;
import java.util.Date;

import javax.mail.*;
import javax.activation.*;
import javax.mail.internet.*;
import javax.mail.util.*;

import static com.intellij.openapi.diagnostic.SubmittedReportInfo.SubmissionStatus.NEW_ISSUE;

public class GravExceptionTracker extends ErrorReportSubmitter {

    public GravExceptionTracker() {
    }

//    @Override
//    public String getPrivacyNoticeText() {
//        return "My privacy notice";
//    }

    @Override
    public boolean submit(@NotNull IdeaLoggingEvent[] events, @Nullable String additionalInfo, @NotNull Component parentComponent, @NotNull Consumer<SubmittedReportInfo> consumer) {
        System.out.println(Arrays.toString(events));
        System.out.println(additionalInfo);
        System.out.println(parentComponent);
        System.out.println(consumer.toString());
        try {
            sendhtml();
            consumer.consume(new SubmittedReportInfo(NEW_ISSUE));
        } catch (MessagingException e) {
            e.printStackTrace();
        }
        return true;
//        return super.submit(events, additionalInfo, parentComponent, consumer);
    }

    @NotNull
    @Override
    public String getReportActionText() {
        return "GRaAAV";
    }

    public void sendhtml() throws MessagingException {
        Properties prop = new Properties();
        prop.put("mail.smtp.auth", true);
        prop.put("mail.smtp.starttls.enable", "true");
        prop.put("mail.smtp.host", "smtp.mailgun.org");
        prop.put("mail.smtp.port", "25");
        prop.put("mail.smtp.ssl.trust", "smtp.mailgun.org");


        Session session = Session.getInstance(prop, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("postmaster@sandbox78b2b6c15bb949139075fcd4f32b302c.mailgun.org", "6aacef51afd350f66c9c87ec3320db3d-c8c889c9-56dda8bc");
            }
        });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress("dominik@offbeat-pioneer.net"));
        message.setRecipients(
                Message.RecipientType.TO, InternetAddress.parse("dominik@offbeat-pioneer.net"));
        message.setSubject("Mail Subject");

        String msg = "This is my first email using JavaMailer";

        MimeBodyPart mimeBodyPart = new MimeBodyPart();
        mimeBodyPart.setContent(msg, "text/plain");


        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(mimeBodyPart);

        message.setContent(multipart);

        Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
        MailcapCommandMap mc = (MailcapCommandMap) CommandMap.getDefaultCommandMap();
        mc.addMailcap("text/html;; x-java-content-handler=com.sun.mail.handlers.text_html");
        mc.addMailcap("text/xml;; x-java-content-handler=com.sun.mail.handlers.text_xml");
        mc.addMailcap("text/plain;; x-java-content-handler=com.sun.mail.handlers.text_plain");
        mc.addMailcap("multipart/*;; x-java-content-handler=com.sun.mail.handlers.multipart_mixed");
        mc.addMailcap("message/rfc822;; x-java-content- handler=com.sun.mail.handlers.message_rfc822");

        // Additional elements to make DSN work
        mc.addMailcap("multipart/report;;  x-java-content-handler=com.sun.mail.dsn.multipart_report");
        mc.addMailcap("message/delivery-status;; x-java-content-handler=com.sun.mail.dsn.message_deliverystatus");
        mc.addMailcap("message/disposition-notification;; x-java-content-handler=com.sun.mail.dsn.message_dispositionnotification");
        mc.addMailcap("text/rfc822-headers;;   x-java-content-handler=com.sun.mail.dsn.text_rfc822headers");
        mc.addMailcap("multipart/*;; x-java-content-handler=com.sun.mail.handlers.multipart_mixed; x-java-fallback-entry=true");
        CommandMap.setDefaultCommandMap(mc);

        Transport.send(message);
    }


}
