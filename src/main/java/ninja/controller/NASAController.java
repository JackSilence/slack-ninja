package ninja.controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ninja.util.Cast;
import ninja.util.Check;
import ninja.util.Gson;
import ninja.util.Slack;
import ninja.util.Utils;

@RestController
public class NASAController extends BaseController {
	private static final String API_URL = "https://api.nasa.gov/planetary/apod?api_key=%s&date=%s", WEB_URL = "https://apod.nasa.gov/apod/ap%s.html";

	private static final String YOUTUBE_REGEX = "https://www.youtube.com/embed/(.+?)\\?rel=0", YOUTUBE_IMG = "https://img.youtube.com/vi/%s/0.jpg";

	private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern( "yyMMdd" );

	@Value( "${nasa.api.key:}" )
	private String key;

	@PostMapping( "/apod" )
	@Async
	public void apod( @RequestParam String command, @RequestParam String text, @RequestParam( RESPONSE_URL ) String url ) {
		LocalDate date = text.isEmpty() ? LocalDate.now() : LocalDate.parse( text );

		Map<?, ?> result = Gson.from( Utils.call( String.format( API_URL, key, date ) ), Map.class );

		String link = Check.nil( Cast.string( result, "url" ), Cast.string( result, "msg" ) ), id = Utils.find( YOUTUBE_REGEX, link );

		String title = Cast.string( result, "title" ), html = String.format( WEB_URL, date.format( DATE_TIME_FORMATTER ) );

		message( Slack.attachment( title, html ).setImageUrl( id == null ? link : String.format( YOUTUBE_IMG, id ) ), command, text, url );
	}
}