package ninja.controller;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DelController extends BaseController {
	private static final String HISTORY_METHOD = "channels.history", DEL_METHOD = "chat.delete";

	private static final String QUERY = "&oldest=%s&latest=%s";

	@Value( "${slack.user.token:}" )
	private String token;

	@PostMapping( value = "/delete" )
	public void delete( String channel, @RequestParam String text ) {
		if ( text.isEmpty() ) {
			text = LocalDate.now().toString();
		}

		try {
			LocalDate date = LocalDate.parse( text );

			long start = epochSecond( date ), end = epochSecond( date.plusDays( 1 ) );

			log.info( get( HISTORY_METHOD, token, channel, String.format( QUERY, start, end ) ) );

		} catch ( DateTimeParseException e ) {
			throw new RuntimeException( e );

		}
	}

	private long epochSecond( LocalDate date ) {
		return date.atStartOfDay( ZoneOffset.UTC ).toEpochSecond();
	}
}