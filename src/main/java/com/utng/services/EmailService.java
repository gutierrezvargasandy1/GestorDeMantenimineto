package com.utng.services;

import java.util.Properties;

import org.eclipse.angus.mail.smtp.SMTPTransport;

import com.utng.util.AppException;

import io.github.cdimascio.dotenv.Dotenv;
import jakarta.mail.Message;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

public class EmailService {

    private final Dotenv dotenv;

    private final String host;
    private final int puerto;
    private final String usuario;
    private final String password;

    public EmailService() {

        dotenv = Dotenv.load();

        host = dotenv.get("SMTP_HOST");
        puerto = Integer.parseInt(dotenv.get("SMTP_PORT"));
        usuario = dotenv.get("SMTP_USER");
        password = dotenv.get("SMTP_PASSWORD");

    }

    public void enviarCorreo(
            String destinatario,
            String asunto,
            String contenido) {

        try {

            Properties propiedades = new Properties();

            propiedades.put("mail.smtp.host", host);
            propiedades.put("mail.smtp.port", puerto);

            propiedades.put("mail.smtp.auth", "true");
            propiedades.put("mail.smtp.starttls.enable", "true");
            propiedades.put("mail.smtp.ssl.trust", host);

            Session session = Session.getInstance(propiedades);

            MimeMessage mensaje = new MimeMessage(session);

            mensaje.setFrom(new InternetAddress(usuario));

            mensaje.setRecipient(
                    Message.RecipientType.TO,
                    new InternetAddress(destinatario));

            mensaje.setSubject(asunto);

            mensaje.setSubject(asunto);

            mensaje.setContent(
                    contenido,
                    "text/html; charset=UTF-8");

            SMTPTransport transport = (SMTPTransport) session.getTransport("smtp");

            transport.connect(
                    host,
                    puerto,
                    usuario,
                    password);

            transport.sendMessage(
                    mensaje,
                    mensaje.getAllRecipients());

            transport.close();

            System.out.println(
                    "Correo enviado correctamente.");

        } catch (Exception e) {

            throw new AppException(
                    "Error al enviar el correo electrónico.",
                    e);

        }

    }

    public static String codigoRecuperacion(String codigo) {

        return """
                <!DOCTYPE html>
                <html lang="es">

                <head>
                    <meta charset="UTF-8">
                    <title>Recuperación de contraseña</title>
                </head>

                <body style="
                    margin:0;
                    padding:0;
                    background:#f4f6f9;
                    font-family:Arial, Helvetica, sans-serif;
                ">

                    <table width="100%%" cellpadding="0" cellspacing="0" style="padding:40px 0;">
                        <tr>
                            <td align="center">

                                <table width="600" cellpadding="0" cellspacing="0"
                                    style="
                                        background:#ffffff;
                                        border-radius:12px;
                                        overflow:hidden;
                                        box-shadow:0 4px 15px rgba(0,0,0,.15);
                                    ">

                                    <tr>
                                        <td
                                            style="
                                                background:#0F172A;
                                                color:white;
                                                text-align:center;
                                                padding:30px;
                                            ">

                                            <h1 style="margin:0;">
                                                Sistema de Gestión de Mantenimiento
                                            </h1>

                                        </td>
                                    </tr>

                                    <tr>
                                        <td style="padding:40px; color:#333333;">

                                            <h2 style="margin-top:0;">
                                                Recuperación de contraseña
                                            </h2>

                                            <p style="font-size:16px; line-height:1.6;">
                                                Hemos recibido una solicitud para recuperar el acceso a tu cuenta.
                                            </p>

                                            <p style="font-size:16px; line-height:1.6;">
                                                Utiliza el siguiente código de verificación:
                                            </p>

                                            <div
                                                style="
                                                    margin:30px auto;
                                                    width:220px;
                                                    text-align:center;
                                                    background:#0F172A;
                                                    color:white;
                                                    padding:18px;
                                                    border-radius:10px;
                                                    font-size:34px;
                                                    font-weight:bold;
                                                    letter-spacing:8px;
                                                ">

                                                %s

                                            </div>

                                            <p
                                                style="
                                                    color:#D32F2F;
                                                    font-size:15px;
                                                    font-weight:bold;
                                                ">

                                                Este código expirará en 5 minutos.

                                            </p>

                                            <p style="font-size:15px; line-height:1.6;">
                                                Si tú no solicitaste la recuperación de tu contraseña,
                                                puedes ignorar este correo de forma segura.
                                            </p>

                                            <hr
                                                style="
                                                    border:none;
                                                    border-top:1px solid #E5E7EB;
                                                    margin:35px 0;
                                                ">

                                            <p
                                                style="
                                                    color:#6B7280;
                                                    font-size:13px;
                                                    text-align:center;
                                                ">

                                                Este es un mensaje automático.<br>
                                                Por favor, no respondas a este correo.

                                            </p>

                                        </td>
                                    </tr>

                                </table>

                            </td>
                        </tr>
                    </table>

                </body>

                </html>
                """.formatted(codigo);

    }

}
