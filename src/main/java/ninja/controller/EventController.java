package ninja.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EventController {
	private final Logger log = LoggerFactory.getLogger( this.getClass() );

	@PostMapping( "/event" )
	public String event( @RequestHeader( "X-Slack-Request-Timestamp" ) String timestamp, @RequestHeader( "X-Slack-Signature" ) String signature, @RequestBody String body, String challenge ) {
		log.info( "Body: {}, timestamp: {}, signature", body, timestamp, signature );

		return challenge;
	}
}