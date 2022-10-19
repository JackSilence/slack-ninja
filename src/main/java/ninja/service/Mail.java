package ninja.service;

import java.nio.charset.StandardCharsets;

import javax.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import magic.service.SendGrid;

@Service
public class Mail extends SendGrid {
	@Autowired
	private JavaMailSender sender;

	@Value( "${gmail.mail.to:}" )
	private String mail;

	@Override
	public void send( String subject, String content ) {
		super.send( subject, content );

		try {
			var message = sender.createMimeMessage();

			var helper = new MimeMessageHelper( message, true, StandardCharsets.UTF_8.name() );

			helper.setSubject( subject );
			helper.setText( content, true );
			helper.setTo( mail );

			sender.send( message );

		} catch ( MessagingException e ) {
			throw new RuntimeException( "Failed to send: " + subject, e );

		}
	}
}