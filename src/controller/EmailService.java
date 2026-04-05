package controller;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;

public class EmailService {

    private static final String FROM_EMAIL = "aburaselrh2021@gmail.com";
    private static final String APP_PASSWORD = "qyes vidr kffw psjx"; // App Password

    // Email পাঠানো
    public static boolean sendEmail(String toEmail, String subject, String body) {
        if (toEmail == null || toEmail.isEmpty()) return false;

        Properties props = new Properties();
        props.put("mail.smtp.auth",            "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host",            "smtp.gmail.com");
        props.put("mail.smtp.port",            "587");
        props.put("mail.smtp.ssl.trust",       "smtp.gmail.com");

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(FROM_EMAIL, APP_PASSWORD);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(FROM_EMAIL));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject(subject);

            // HTML email
            message.setContent(body, "text/html; charset=utf-8");
            Transport.send(message);
            return true;
        } catch (MessagingException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ── Email Templates ──

    // Deposit notification
    public static boolean sendDepositNotification(String toEmail, String customerName,
                                                  String accountNumber, String amount,
                                                  String balance) {
        String subject = "Deposit Confirmation - Bangladesh Bank";
        String body = buildEmailTemplate(
                "Deposit Successful ✅",
                customerName,
                "<table style='width:100%;border-collapse:collapse;'>" +
                        "<tr><td style='padding:8px;background:#f5f8ff;'><b>Transaction Type</b></td>" +
                        "<td style='padding:8px;'>Deposit</td></tr>" +
                        "<tr><td style='padding:8px;background:#f5f8ff;'><b>Account Number</b></td>" +
                        "<td style='padding:8px;'>" + accountNumber + "</td></tr>" +
                        "<tr><td style='padding:8px;background:#f5f8ff;'><b>Amount</b></td>" +
                        "<td style='padding:8px;color:#009966;'><b>+ TK " + amount + "</b></td></tr>" +
                        "<tr><td style='padding:8px;background:#f5f8ff;'><b>Current Balance</b></td>" +
                        "<td style='padding:8px;'><b>TK " + balance + "</b></td></tr>" +
                        "</table>",
                "যদি এই transaction আপনি করেননি, অনুগ্রহ করে আমাদের সাথে যোগাযোগ করুন।"
        );
        return sendEmail(toEmail, subject, body);
    }

    // Withdrawal notification
    public static boolean sendWithdrawalNotification(String toEmail, String customerName,
                                                     String accountNumber, String amount,
                                                     String balance) {
        String subject = "Withdrawal Confirmation - Bangladesh Bank";
        String body = buildEmailTemplate(
                "Withdrawal Successful 💸",
                customerName,
                "<table style='width:100%;border-collapse:collapse;'>" +
                        "<tr><td style='padding:8px;background:#f5f8ff;'><b>Transaction Type</b></td>" +
                        "<td style='padding:8px;'>Withdrawal</td></tr>" +
                        "<tr><td style='padding:8px;background:#f5f8ff;'><b>Account Number</b></td>" +
                        "<td style='padding:8px;'>" + accountNumber + "</td></tr>" +
                        "<tr><td style='padding:8px;background:#f5f8ff;'><b>Amount</b></td>" +
                        "<td style='padding:8px;color:#cc3333;'><b>- TK " + amount + "</b></td></tr>" +
                        "<tr><td style='padding:8px;background:#f5f8ff;'><b>Current Balance</b></td>" +
                        "<td style='padding:8px;'><b>TK " + balance + "</b></td></tr>" +
                        "</table>",
                "যদি এই transaction আপনি করেননি, অনুগ্রহ করে আমাদের সাথে যোগাযোগ করুন।"
        );
        return sendEmail(toEmail, subject, body);
    }

    // Transfer notification
    public static boolean sendTransferNotification(String toEmail, String customerName,
                                                   String fromAccount, String toAccount,
                                                   String amount, String balance) {
        String subject = "Fund Transfer Confirmation - Bangladesh Bank";
        String body = buildEmailTemplate(
                "Fund Transfer Successful 🔄",
                customerName,
                "<table style='width:100%;border-collapse:collapse;'>" +
                        "<tr><td style='padding:8px;background:#f5f8ff;'><b>Transaction Type</b></td>" +
                        "<td style='padding:8px;'>Fund Transfer</td></tr>" +
                        "<tr><td style='padding:8px;background:#f5f8ff;'><b>From Account</b></td>" +
                        "<td style='padding:8px;'>" + fromAccount + "</td></tr>" +
                        "<tr><td style='padding:8px;background:#f5f8ff;'><b>To Account</b></td>" +
                        "<td style='padding:8px;'>" + toAccount + "</td></tr>" +
                        "<tr><td style='padding:8px;background:#f5f8ff;'><b>Amount</b></td>" +
                        "<td style='padding:8px;color:#1e64c8;'><b>TK " + amount + "</b></td></tr>" +
                        "<tr><td style='padding:8px;background:#f5f8ff;'><b>Current Balance</b></td>" +
                        "<td style='padding:8px;'><b>TK " + balance + "</b></td></tr>" +
                        "</table>",
                "যদি এই transaction আপনি করেননি, অনুগ্রহ করে আমাদের সাথে যোগাযোগ করুন।"
        );
        return sendEmail(toEmail, subject, body);
    }

    // Loan approval notification
    public static boolean sendLoanApprovalNotification(String toEmail, String customerName,
                                                       String amount, String monthlyPayment) {
        String subject = "Loan Approved - Bangladesh Bank";
        String body = buildEmailTemplate(
                "Loan Approved ✅",
                customerName,
                "<table style='width:100%;border-collapse:collapse;'>" +
                        "<tr><td style='padding:8px;background:#f5f8ff;'><b>Loan Amount</b></td>" +
                        "<td style='padding:8px;color:#009966;'><b>TK " + amount + "</b></td></tr>" +
                        "<tr><td style='padding:8px;background:#f5f8ff;'><b>Monthly Payment</b></td>" +
                        "<td style='padding:8px;'><b>TK " + monthlyPayment + "</b></td></tr>" +
                        "</table>",
                "আপনার loan approved হয়েছে এবং টাকা আপনার account এ transfer করা হয়েছে।"
        );
        return sendEmail(toEmail, subject, body);
    }

    // Welcome email — নতুন account এ
    public static boolean sendWelcomeEmail(String toEmail, String customerName,
                                           String accountNumber, String accountType) {
        String subject = "Welcome to Bangladesh Bank!";
        String body = buildEmailTemplate(
                "Account Successfully Created 🏦",
                customerName,
                "<table style='width:100%;border-collapse:collapse;'>" +
                        "<tr><td style='padding:8px;background:#f5f8ff;'><b>Account Number</b></td>" +
                        "<td style='padding:8px;'><b>" + accountNumber + "</b></td></tr>" +
                        "<tr><td style='padding:8px;background:#f5f8ff;'><b>Account Type</b></td>" +
                        "<td style='padding:8px;'>" + accountType + "</td></tr>" +
                        "</table>",
                "Bangladesh Bank এ আপনাকে স্বাগতম! আপনার account সফলভাবে তৈরি হয়েছে।"
        );
        return sendEmail(toEmail, subject, body);
    }

    // ── HTML Email Template ──
    private static String buildEmailTemplate(String title, String customerName,
                                             String content, String footer) {
        return "<!DOCTYPE html><html><body style='font-family:Arial,sans-serif;" +
                "background:#f0f4f8;margin:0;padding:20px;'>" +
                "<div style='max-width:600px;margin:0 auto;background:white;" +
                "border-radius:10px;overflow:hidden;box-shadow:0 2px 10px rgba(0,0,0,0.1);'>" +

                // Header
                "<div style='background:#0f2850;padding:25px;text-align:center;'>" +
                "<h1 style='color:white;margin:0;font-size:22px;'>🏦 Bangladesh Bank</h1>" +
                "<p style='color:#b4c8e6;margin:5px 0 0;font-size:13px;'>Management System</p>" +
                "</div>" +

                // Title bar
                "<div style='background:#1a5276;padding:15px 25px;'>" +
                "<h2 style='color:white;margin:0;font-size:18px;'>" + title + "</h2>" +
                "</div>" +

                // Body
                "<div style='padding:25px;'>" +
                "<p style='color:#333;'>Dear <b>" + customerName + "</b>,</p>" +
                "<p style='color:#555;'>আপনার account এ নিচের transaction সম্পন্ন হয়েছে:</p>" +
                content +
                "<p style='color:#888;font-size:12px;margin-top:20px;'>" + footer + "</p>" +
                "</div>" +

                // Footer
                "<div style='background:#f5f8ff;padding:15px 25px;text-align:center;" +
                "border-top:1px solid #e0e8f0;'>" +
                "<p style='color:#999;font-size:11px;margin:0;'>" +
                "Bangladesh Bank Management System © 2026 | " +
                "এই email টি automatically generated।</p>" +
                "</div>" +
                "</div></body></html>";
    }
}