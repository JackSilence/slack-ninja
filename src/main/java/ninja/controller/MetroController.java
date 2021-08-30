package ninja.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ninja.service.Metro;
import ninja.util.Check;
import ninja.util.Slack;

@RestController
public class MetroController extends DialogController {
	private static final String WEB_URL = "https://web.metro.taipei/pages/tw/ticketroutetime/%s/%s", TITLE = "捷運票價及乘車時間";

	@Autowired
	private Metro metro;

	@PostMapping( "/mrt" )
	@Async
	public void mrt( @RequestParam String command, @RequestParam String text, @RequestParam( RESPONSE_URL ) String url ) {
		String[] params = Check.station( Check.params( text ) );

		String start = id( params[ 0 ] ), end = id( params[ 1 ] );

		log.info( "Start: {}, end: {}", start, end );

		message( Slack.attachment( TITLE, String.format( WEB_URL, start, end ) ), command, text, url );
	}

	private String id( String station ) {
		return Check.station( metro, station );
	}
}