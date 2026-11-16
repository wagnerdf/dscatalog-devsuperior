package com.devsuperior.dscatalog.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.devsuperior.dscatalog.services.exceptions.EmailException;

/**
 * Serviço responsável pelo envio de mensagens de e-mail.
 * Utiliza o JavaMailSender para realizar o envio através
 * das configurações SMTP definidas na aplicação.
 */
@Service
public class EmailService {
	
	/**
     * Endereço de e-mail utilizado como remetente das mensagens.
     */
	@Value("${spring.mail.username}")
	private String emailFrom;
	
	 /**
     * Componente responsável pelo envio das mensagens de e-mail.
     */
    @Autowired
    private JavaMailSender emailSender;

    /**
     * Envia uma mensagem de e-mail para o destinatário informado.
     *
     * @param to      endereço de e-mail do destinatário
     * @param subject assunto da mensagem
     * @param body    conteúdo da mensagem
     * @throws EmailException caso ocorra uma falha durante o envio
     */
    public void sendEmail(String to, String subject, String body) {
        try{
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(emailFrom);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            emailSender.send(message);
        } 
        catch (MailException e){
        	throw new EmailException("Failed to send email");
        } 
    }
}
