package ninja;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

import magic.service.AsyncExecutor;
import magic.service.Slack;

@SpringBootApplication
@Import( { AsyncExecutor.class, Slack.class } )
public class App {
	public static void main( String[] args ) {
		SpringApplication.run( App.class, args );
	}
}