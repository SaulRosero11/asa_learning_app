package com.springboot.asa.learning.infrastructure.email;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${asa.mail.from:noreply@solidaridadyaccion.org}")
    private String from;

    @Value("${asa.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    @Async
    public void sendInvitationEmail(String toEmail, String programName, String invitationToken) {
        String link = frontendUrl + "/invite/" + invitationToken;
        String html = """
                <!DOCTYPE html><html lang="es"><head><meta charset="UTF-8"></head>
                <body style="font-family:Arial,sans-serif;max-width:600px;margin:0 auto;padding:20px;">
                  <div style="text-align:center;margin-bottom:32px;">
                    <h1 style="color:#7147D3;">ASA E-Learning</h1>
                  </div>
                  <p>¡Hola! Has sido invitado/a a participar en el programa:</p>
                  <h2 style="color:#1D1D23;">%s</h2>
                  <p>Haz clic en el botón para aceptar la invitación:</p>
                  <div style="text-align:center;margin:32px 0;">
                    <a href="%s" style="background-color:#7147D3;color:#ffffff;
                       padding:14px 28px;border-radius:8px;text-decoration:none;
                       font-weight:bold;display:inline-block;">
                      Aceptar invitación
                    </a>
                  </div>
                  <p style="color:#7B7B83;font-size:14px;">Este enlace es válido por <strong>7 días</strong>.</p>
                  <hr style="border:none;border-top:1px solid #EAEAEA;margin:24px 0;">
                  <p style="color:#7B7B83;font-size:12px;">
                    Si no esperabas esta invitación, puedes ignorar este correo.
                  </p>
                </body></html>
                """.formatted(programName, link);
        sendHtml(toEmail, "Invitación al programa: " + programName + " — ASA E-Learning", html);
    }

    @Async
    public void sendPasswordResetEmail(String toEmail, String userName, String resetToken) {
        String link = frontendUrl + "/reset-password?token=" + resetToken;
        String html = buildResetEmailHtml(userName, link);
        sendHtml(toEmail, "Restablecer tu contraseña — ASA E-Learning", html);
    }

    private void sendHtml(String to, String subject, String html) {
        try {
            var message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(message);
        } catch (Exception e) {
            log.error("Error al enviar email a {}: {}", to, e.getMessage());
        }
    }

    private String buildResetEmailHtml(String userName, String link) {
        return """
                <!DOCTYPE html>
                <html lang="es">
                <head><meta charset="UTF-8"></head>
                <body style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px;">
                  <div style="text-align: center; margin-bottom: 32px;">
                    <h1 style="color: #7147D3; font-size: 24px;">ASA E-Learning</h1>
                  </div>
                  <p>Hola %s,</p>
                  <p>Recibimos una solicitud para restablecer la contraseña de tu cuenta.</p>
                  <p>Haz clic en el siguiente botón para crear una nueva contraseña:</p>
                  <div style="text-align: center; margin: 32px 0;">
                    <a href="%s" style="background-color: #7147D3; color: #ffffff;
                       padding: 14px 28px; border-radius: 8px; text-decoration: none;
                       font-weight: bold; display: inline-block;">
                      Restablecer contraseña
                    </a>
                  </div>
                  <p style="color: #7B7B83; font-size: 14px;">
                    Este enlace es válido por <strong>1 hora</strong>.
                  </p>
                  <hr style="border: none; border-top: 1px solid #EAEAEA; margin: 24px 0;">
                  <p style="color: #7B7B83; font-size: 12px;">
                    Si no solicitaste este cambio, puedes ignorar este correo.
                    Tu contraseña no será modificada.
                  </p>
                </body>
                </html>
                """.formatted(userName != null ? userName : "usuario", link);
    }
}
