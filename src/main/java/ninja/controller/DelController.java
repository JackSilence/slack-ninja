package ninja.controller;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ninja.slack.History;
import ninja.util.Gson;

@RestController
public class DelController extends BaseController {
	private static final String HISTORY_METHOD = "channels.history", DEL_METHOD = "chat.delete";

	private static final String QUERY = "&oldest=%s&latest=%s";

	@Value( "${slack.user.token:}" )
	private String token;

	@PostMapping( value = "/delete" )
	public void delete( @RequestParam( "channel_id" ) String channel, @RequestParam String text ) {
		if ( text.isEmpty() ) {
			text = LocalDate.now().toString();
		}

		try {
			LocalDate date = LocalDate.parse( text );

			long start = epochSecond( date ), end = epochSecond( date.plusDays( 1 ) );

			History history = Gson.from( get( HISTORY_METHOD, token + "a", channel, String.format( QUERY, start, end ) ), History.class );

			log.info( history.toString() );

		} catch ( DateTimeParseException e ) {
			throw new RuntimeException( e );

		}
	}

	private long epochSecond( LocalDate date ) {
		return date.atStartOfDay( ZoneOffset.UTC ).toEpochSecond();
	}
}