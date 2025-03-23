package com.scm.services;

public interface EmailService {

    // send email
    void sendEmail(String to, String subject, String body);

    // send email with html
    void sendEmailWithHtml();

    // send email with attachment
    void sendEmailWithAttachment();
}
