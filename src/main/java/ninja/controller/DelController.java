package ninja.controller;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ninja.slack.Event;
import ninja.slack.History;
import ninja.util.Gson;

@RestController
public class DelController extends BaseController {
	private static final String HISTORY_METHOD = "channels.history", DEL_METHOD = "chat.delete";

	private static final String QUERY = "&oldest=%s&latest=%s";

	@Value( "${slack.user.token:}" )
	private String token;

	@PostMapping( value = "/delete" )
	public String delete( @RequestParam( "channel_id" ) String channel, @RequestParam String text ) {
		if ( text.isEmpty() ) {
			text = LocalDate.now().toString();
		}

		try {
			LocalDate date = LocalDate.parse( text );

			long start = epochSecond( date ), end = epochSecond( date.plusDays( 1 ) ), success = 0;

			History history = Gson.from( get( HISTORY_METHOD, token, channel, String.format( QUERY, start, end ) ), History.class );

			List<Event> message = ObjectUtils.defaultIfNull( history.getMessages(), new ArrayList<>() );

			for ( Event i : message ) {
				i.setChannel( channel );

				String response = post( DEL_METHOD, token, i );

				success += response.contains( "\"ok\":true" ) ? 1 : 0;

				log.info( response );
			}

			return String.format( "已刪除%s的%d(%d)則訊息", text, message.size(), success );

		} catch ( RuntimeException e ) {
			return e.getMessage();

		}
	}

	private long epochSecond( LocalDate date ) {
		return date.atStartOfDay( ZoneOffset.UTC ).toEpochSecond();
	}
}