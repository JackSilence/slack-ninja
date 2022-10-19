package ninja.controller;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import magic.util.Utils;
import net.gpedro.integrations.slack.SlackAttachment;
import ninja.slack.Event;
import ninja.slack.History;
import ninja.util.Gson;

@RestController
public class DelController extends BaseController {
	private static final String HISTORY_METHOD = "conversations.history", DEL_METHOD = "chat.delete";

	private static final String QUERY = "&oldest=%d&latest=%d", TEXT = "總共有 %d 則訊息\n已刪除 %d 則訊息";

	private static final Map<String, Long> DAYS_AGO = Map.of( StringUtils.EMPTY, 0L, "今天", 0L, "昨天", 1L, "前天", 2L );

	@PostMapping( "/delete" )
	@Async
	public void delete( @RequestParam( CHANNEL_ID ) String channel, @RequestParam String command, @RequestParam String text, @RequestParam( RESPONSE_URL ) String url ) {
		Long days = DAYS_AGO.get( text ), success = 0L;

		var date = days == null ? LocalDate.parse( text ) : LocalDate.now( ZONE_ID ).minusDays( days );

		String title = date.toString(), query = String.format( QUERY, epochSecond( date ), epochSecond( date.plusDays( 1 ) ) ), response;

		log.info( "Date: {}, query: {}", date, query );

		var history = Gson.from( get( HISTORY_METHOD, channel, query ), History.class );

		List<Event> message = ObjectUtils.defaultIfNull( history.getMessages(), new ArrayList<>() );

		for ( int i = 0, size = message.size(); i < size; i++ ) {
			var event = message.get( i );

			event.setChannel( channel );

			try {
				log.info( response = post( DEL_METHOD, event ) );

				success += response.contains( "\"ok\":true" ) ? 1 : 0;

			} catch ( IllegalStateException e ) {
				if ( !e.getMessage().contains( String.valueOf( HttpStatus.TOO_MANY_REQUESTS.value() ) ) ) {
					throw e;
				}

				i--;

				Utils.sleep( 5000 );
			}
		}

		var txt = String.format( TEXT, message.size(), success );

		message( new SlackAttachment( title + "\n" + txt ).setTitle( title ).setText( txt ), command, text, url );
	}

	private long epochSecond( LocalDate date ) {
		return date.atStartOfDay( ZONE_ID ).toEpochSecond();
	}
}