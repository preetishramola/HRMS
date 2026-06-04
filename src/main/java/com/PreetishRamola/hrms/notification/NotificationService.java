package com.PreetishRamola.hrms.notification;

import com.PreetishRamola.hrms.recruitment.Candidate;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String fromAddress;

    @Value("${app.mail.from-name}")
    private String fromName;

    @Async
    public void notifyApplicationReceived(Candidate candidate) {
        String subject = "✅ Application Received – " + candidate.getJobPosting().getTitle();
        String body = buildHtml(
                candidate.getName(),
                "Application Received",
                "#4f46e5",
                "Your application for <strong>" + candidate.getJobPosting().getTitle() + "</strong> has been received.",
                "Our team will review your resume and get back to you shortly. " +
                "You'll be notified at each step of the process.",
                null, null
        );
        send(candidate.getEmail(), subject, body);
    }

    @Async
    public void notifyScreeningPassed(Candidate candidate) {
        String subject = "🎉 Resume Shortlisted – " + candidate.getJobPosting().getTitle();
        String body = buildHtml(
                candidate.getName(),
                "You've Been Shortlisted!",
                "#16a34a",
                "Congratulations! Your resume for <strong>" + candidate.getJobPosting().getTitle() +
                "</strong> has passed our AI-powered screening.",
                "Your profile scored <strong>" + candidate.getSkillMatchPercent() + "% skill match</strong>. " +
                "Our HR team will schedule an interview with you soon. Keep an eye on your inbox!",
                null, null
        );
        send(candidate.getEmail(), subject, body);
    }

    @Async
    public void notifyScreeningRejected(Candidate candidate) {
        String subject = "Application Update – " + candidate.getJobPosting().getTitle();
        String body = buildHtml(
                candidate.getName(),
                "Application Status Update",
                "#dc2626",
                "Thank you for applying for <strong>" + candidate.getJobPosting().getTitle() + "</strong>.",
                "After careful review, we've decided not to move forward with your application at this time. " +
                "We encourage you to apply for future openings that match your profile.",
                null, null
        );
        send(candidate.getEmail(), subject, body);
    }

    @Async
    public void notifyInterviewScheduled(Candidate candidate) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("EEEE, MMMM d yyyy 'at' h:mm a");
        String slot = candidate.getScheduledInterviewAt() != null
                ? candidate.getScheduledInterviewAt().format(fmt) : "TBD";

        String linkHtml = candidate.getInterviewMeetingLink() != null
                ? "<p style='margin-top:16px;'><a href='" + candidate.getInterviewMeetingLink() +
                  "' style='background:#4f46e5;color:#fff;padding:10px 24px;border-radius:6px;" +
                  "text-decoration:none;font-weight:600;'>Join Interview</a></p>"
                : "";

        String subject = "📅 Interview Scheduled – " + candidate.getJobPosting().getTitle();
        String body = buildHtml(
                candidate.getName(),
                "Interview Scheduled",
                "#4f46e5",
                "Your interview for <strong>" + candidate.getJobPosting().getTitle() + "</strong> has been scheduled.",
                "📅 <strong>" + slot + "</strong><br><br>" +
                "Please be prepared to discuss your experience and technical background. " +
                "The interview will be conducted via our AI-assisted interview platform." + linkHtml,
                null, null
        );
        send(candidate.getEmail(), subject, body);
    }

    @Async
    public void notifyInterviewReminder(Candidate candidate) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("EEEE, MMMM d yyyy 'at' h:mm a");
        String slot = candidate.getScheduledInterviewAt() != null
                ? candidate.getScheduledInterviewAt().format(fmt) : "soon";

        String subject = "⏰ Interview Reminder – Tomorrow at " +
                (candidate.getScheduledInterviewAt() != null
                        ? candidate.getScheduledInterviewAt().format(DateTimeFormatter.ofPattern("h:mm a"))
                        : "scheduled time");
        String body = buildHtml(
                candidate.getName(),
                "Interview Reminder",
                "#0891b2",
                "This is a reminder that your interview for <strong>" + candidate.getJobPosting().getTitle() +
                "</strong> is scheduled for <strong>" + slot + "</strong>.",
                "Please ensure you have a stable internet connection and a quiet environment. " +
                "Good luck — you've got this! 💪",
                null, null
        );
        send(candidate.getEmail(), subject, body);
    }

    @Async
    public void notifyOfferExtended(Candidate candidate) {
        String subject = "🎊 Offer Extended – " + candidate.getJobPosting().getTitle();
        String body = buildHtml(
                candidate.getName(),
                "Congratulations — You've Got an Offer!",
                "#16a34a",
                "We're thrilled to extend you an offer for the <strong>" +
                candidate.getJobPosting().getTitle() + "</strong> position!",
                "Our HR team will reach out shortly with the formal offer letter and next steps. " +
                "Welcome to the team! 🎉",
                null, null
        );
        send(candidate.getEmail(), subject, body);
    }

    @Async
    public void notifyOfferLetter(Candidate candidate, String acceptUrl, String declineUrl) {
        String subject = "🎊 Offer Letter – " + candidate.getJobPosting().getTitle();
        String body = buildOfferHtml(candidate, acceptUrl, declineUrl);
        send(candidate.getEmail(), subject, body);
    }

    @Async
    public void notifyOfferAccepted(Candidate candidate) {
        String subject = "✅ Offer Accepted – Welcome aboard, " + candidate.getName().split(" ")[0] + "!";
        String body = buildHtml(
                candidate.getName(),
                "Welcome to the Team! 🎉",
                "#16a34a",
                "You have officially accepted the offer for <strong>" +
                candidate.getJobPosting().getTitle() + "</strong>.",
                "Your employee account has been created. You will receive your login credentials shortly. " +
                "We're excited to have you on board!",
                null, null
        );
        send(candidate.getEmail(), subject, body);
    }

    @Async
    public void notifyOfferDeclined(Candidate candidate) {
        String subject = "Offer Update – " + candidate.getJobPosting().getTitle();
        String body = buildHtml(
                candidate.getName(),
                "Thank You for Your Response",
                "#6b7280",
                "We've received your decision regarding the offer for <strong>" +
                candidate.getJobPosting().getTitle() + "</strong>.",
                "We understand and respect your decision. We wish you all the best in your career journey. " +
                "You're always welcome to apply for future openings.",
                null, null
        );
        send(candidate.getEmail(), subject, body);
    }

    @Async
    public void notifyRejected(Candidate candidate) {
        String subject = "Application Update – " + candidate.getJobPosting().getTitle();
        String body = buildHtml(
                candidate.getName(),
                "Application Status Update",
                "#dc2626",
                "Thank you for going through our interview process for <strong>" +
                candidate.getJobPosting().getTitle() + "</strong>.",
                "After careful consideration, we've decided not to move forward at this time. " +
                "We truly appreciate the time and effort you invested. We will keep your profile " +
                "on file for future opportunities.",
                null, null
        );
        send(candidate.getEmail(), subject, body);
    }

    // ──────────────────────────────────────────────
    // Helpers
    // ──────────────────────────────────────────────

    private void send(String to, String subject, String html) {
        try {
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");
            helper.setFrom(fromAddress, fromName);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(msg);
            log.info("Email sent to {} | {}", to, subject);
        } catch (Exception e) {
            log.warn("Failed to send email to {}: {}", to, e.getMessage());
            // Non-fatal — notification failure should never break the main flow
        }
    }

    private String buildOfferHtml(Candidate candidate, String acceptUrl, String declineUrl) {
        String firstName = candidate.getName().split(" ")[0];
        String jobTitle  = candidate.getJobPosting().getTitle();
        return """
                <!DOCTYPE html>
                <html>
                <head><meta charset="UTF-8"/></head>
                <body style="margin:0;padding:0;background:#f1f5f9;font-family:'Segoe UI',Arial,sans-serif;">
                  <table width="100%%" cellpadding="0" cellspacing="0">
                    <tr><td align="center" style="padding:40px 16px;">
                      <table width="580" cellpadding="0" cellspacing="0"
                             style="background:#ffffff;border-radius:16px;overflow:hidden;
                                    box-shadow:0 4px 24px rgba(0,0,0,0.09);">
                        <!-- Header -->
                        <tr>
                          <td style="background:linear-gradient(135deg,#0f172a 0%%,#1e1b4b 50%%,#1e3a5f 100%%);padding:36px;">
                            <p style="margin:0 0 6px;color:rgba(255,255,255,0.6);font-size:12px;font-weight:600;letter-spacing:1.5px;text-transform:uppercase;">HRMS Platform</p>
                            <h1 style="margin:0;color:#ffffff;font-size:26px;font-weight:800;">🎊 You've Got an Offer!</h1>
                          </td>
                        </tr>
                        <!-- Body -->
                        <tr>
                          <td style="padding:36px;">
                            <p style="margin:0 0 16px;color:#374151;font-size:16px;">Hi <strong>%s</strong>,</p>
                            <p style="margin:0 0 20px;color:#374151;font-size:15px;line-height:1.7;">
                              After a thorough review process, we are delighted to offer you the position of
                              <strong style="color:#4f46e5;">%s</strong>.
                              Your skills and experience impressed us throughout the process.
                            </p>
                            <!-- Offer card -->
                            <table width="100%%" cellpadding="0" cellspacing="0"
                                   style="background:#f8faff;border:1px solid #e0e7ff;border-radius:12px;margin-bottom:28px;">
                              <tr><td style="padding:24px;">
                                <p style="margin:0 0 6px;font-size:12px;color:#6366f1;font-weight:700;letter-spacing:1px;text-transform:uppercase;">Position Offered</p>
                                <p style="margin:0 0 18px;font-size:22px;font-weight:800;color:#1e1b4b;">%s</p>
                                <p style="margin:0;font-size:13px;color:#6b7280;line-height:1.6;">
                                  Please review this offer and respond at your earliest convenience.
                                  This offer is valid for <strong>5 business days</strong>.
                                </p>
                              </td></tr>
                            </table>
                            <!-- CTA buttons -->
                            <table cellpadding="0" cellspacing="0" style="margin:0 auto 24px;">
                              <tr>
                                <td style="padding-right:12px;">
                                  <a href="%s" style="display:inline-block;background:#16a34a;color:#ffffff;
                                     padding:14px 32px;border-radius:8px;text-decoration:none;font-weight:700;
                                     font-size:15px;">✅ Accept Offer</a>
                                </td>
                                <td>
                                  <a href="%s" style="display:inline-block;background:#ffffff;color:#6b7280;
                                     padding:14px 32px;border-radius:8px;text-decoration:none;font-weight:600;
                                     font-size:15px;border:1px solid #e5e7eb;">Decline</a>
                                </td>
                              </tr>
                            </table>
                            <p style="margin:0;color:#9ca3af;font-size:12px;text-align:center;">
                              Clicking Accept will confirm your acceptance of this offer.
                            </p>
                          </td>
                        </tr>
                        <!-- Footer -->
                        <tr>
                          <td style="background:#f8fafc;padding:20px 36px;border-top:1px solid #e5e7eb;">
                            <p style="margin:0;color:#9ca3af;font-size:12px;">
                              This is an automated message from HRMS Platform. Please do not reply to this email.</p>
                          </td>
                        </tr>
                      </table>
                    </td></tr>
                  </table>
                </body>
                </html>
                """.formatted(firstName, jobTitle, jobTitle, acceptUrl, declineUrl);
    }

    private String buildHtml(String name, String heading, String accentColor,
                              String lead, String body, String ctaLabel, String ctaUrl) {
        String cta = (ctaLabel != null && ctaUrl != null)
                ? "<p style='margin-top:24px;'><a href='" + ctaUrl + "' style='background:" + accentColor +
                  ";color:#fff;padding:12px 28px;border-radius:6px;text-decoration:none;" +
                  "font-weight:600;font-size:15px;'>" + ctaLabel + "</a></p>"
                : "";

        return """
                <!DOCTYPE html>
                <html>
                <head><meta charset="UTF-8"/></head>
                <body style="margin:0;padding:0;background:#f1f5f9;font-family:'Segoe UI',Arial,sans-serif;">
                  <table width="100%%" cellpadding="0" cellspacing="0">
                    <tr><td align="center" style="padding:40px 16px;">
                      <table width="560" cellpadding="0" cellspacing="0"
                             style="background:#ffffff;border-radius:12px;overflow:hidden;
                                    box-shadow:0 4px 20px rgba(0,0,0,0.08);">
                        <!-- Header -->
                        <tr>
                          <td style="background:%s;padding:28px 36px;">
                            <p style="margin:0;color:rgba(255,255,255,0.85);font-size:13px;
                                      font-weight:600;letter-spacing:1px;text-transform:uppercase;">
                              HRMS Platform</p>
                            <h1 style="margin:6px 0 0;color:#ffffff;font-size:24px;font-weight:700;">
                              %s</h1>
                          </td>
                        </tr>
                        <!-- Body -->
                        <tr>
                          <td style="padding:32px 36px;">
                            <p style="margin:0 0 12px;color:#374151;font-size:16px;">
                              Hi <strong>%s</strong>,</p>
                            <p style="margin:0 0 16px;color:#374151;font-size:15px;line-height:1.6;">
                              %s</p>
                            <p style="margin:0;color:#6b7280;font-size:14px;line-height:1.7;">
                              %s</p>
                            %s
                          </td>
                        </tr>
                        <!-- Footer -->
                        <tr>
                          <td style="background:#f8fafc;padding:20px 36px;border-top:1px solid #e5e7eb;">
                            <p style="margin:0;color:#9ca3af;font-size:12px;">
                              This is an automated message from HRMS Platform. Please do not reply to this email.</p>
                          </td>
                        </tr>
                      </table>
                    </td></tr>
                  </table>
                </body>
                </html>
                """.formatted(accentColor, heading, name, lead, body, cta);
    }
}
