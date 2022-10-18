package ninja;

import java.util.Properties;
import java.util.concurrent.Executor;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import magic.controller.ExecuteController;
import magic.service.AsyncExecutor;
import magic.service.Slack;
import magic.util.Utils;
import ninja.ex.EXHandler;

@SpringBootApplication
@EnableRetry
@EnableScheduling
@Import( { ExecuteController.class, AsyncExecutor.class, Slack.class } )
public class App implements AsyncConfigurer {
	@Value( "${email.username:}" )
	private String username;

	@Value( "${email.password:}" )
	private String password;

	public static void main( String[] args ) {
		SpringApplication.run( App.class, args );
	}

	@Bean
	public JavaMailSender sender() {
		JavaMailSenderImpl sender = new JavaMailSenderImpl();

		sender.setHost( "smtp.gmail.com" );
		sender.setPort( 465 );

		sender.setUsername( Utils.decode( username ) );
		sender.setPassword( Utils.decode( password ) );

		Properties props = sender.getJavaMailProperties();

		props.put( "mail.transport.protocol", "smtp" );
		props.put( "mail.smtp.auth", "true" );
		props.put( "mail.smtp.starttls.enable", "true" );
		props.put( "mail.debug", "false" );
		props.put( "mail.smtp.ssl.enable", "true" );

		return sender;
	}

	@Bean
	@Override
	public Executor getAsyncExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

		executor.setCorePoolSize( 10 );

		return executor;
	}

	@Override
	public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
		return new EXHandler();
	}
}