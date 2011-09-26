package com.rectang.xsm.util;

import com.rectang.xsm.Config;
import com.rectang.xsm.XSM;

import java.util.Properties;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * Some util methods for handling emailing through the configured SMTP server
 * 
 * @author aje
 */
public class EmailUtils {

  /**
   * Send an email using the configured SMTP server.
   * 
   * @param subject The subject of the email to send
   * @param body    The message body of the email to send
   * @param to      The email address to send the message to
   * @return true if the email sent successfully, false otherwise
   */
  public static boolean emailTo(String subject, String body, String to) {
    Config config = XSM.getConfig();
    String from = config.getEmailFrom();
    String host = config.getSmtpHost();

    Properties mailProps = new Properties();
    mailProps.put("mail.smtp.host", host);
    Session session = Session.getDefaultInstance(mailProps, null);
    Message message = new MimeMessage(session);

    try {
      message.setFrom(new InternetAddress(from));
      message.setSubject(subject);
      message.setText(body);
      Transport trans = session.getTransport("smtp");
      trans.connect(host, config.getSmtpUser(), config.getSmtpPass());
      trans.sendMessage(message, new InternetAddress[] {new InternetAddress(to)} );
    } catch (MessagingException e) {
      e.printStackTrace();
      return false;
    }
    return true;
  }
}
