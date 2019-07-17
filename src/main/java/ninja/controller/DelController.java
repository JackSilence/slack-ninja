package ninja.controller;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.ImmutableMap;

import net.gpedro.integrations.slack.SlackAttachment;
import ninja.slack.Event;
import ninja.slack.History;
import ninja.util.Gson;

@RestController
public class DelController extends BaseController {
	private static final String HISTORY_METHOD = "channels.history", DEL_METHOD = "chat.delete";

	private static final String QUERY = "&oldest=%s&latest=%s", TEXT = "總共有 %d 則訊息\n已刪除 %s 則訊息";

	private static final Map<String, Long> DAYS_AGO = ImmutableMap.of( StringUtils.EMPTY, 0L, "今天", 0L, "昨天", 1L, "前天", 2L );

	@PostMapping( value = "/delete" )
	public String delete( @RequestParam( CHANNEL_ID ) String channel, @RequestParam String command, @RequestParam String text ) {
		try {
			Long days = DAYS_AGO.get( text ), success = 0L;

			LocalDate date = days == null ? LocalDate.parse( text ) : LocalDate.now( ZONE_ID ).minusDays( days );

			String title = date.toString(), query = String.format( QUERY, epochSecond( date ), epochSecond( date.plusDays( 1 ) ) ), txt;

			log.info( "Date: {}, query: {}", date, query );

			History history = Gson.from( get( HISTORY_METHOD, channel, query ), History.class );

			List<Event> message = ObjectUtils.defaultIfNull( history.getMessages(), new ArrayList<>() );

			for ( Event i : message ) {
				i.setChannel( channel );

				String response = post( DEL_METHOD, i );

				success += response.contains( "\"ok\":true" ) ? 1 : 0;

				log.info( response );
			}

			return message( new SlackAttachment( txt = String.format( TEXT, message.size(), success ) ).setTitle( title ).setText( txt ), command, text );

		} catch ( RuntimeException e ) {
			return e.getMessage();

		}
	}

	private long epochSecond( LocalDate date ) {
		return date.atStartOfDay( ZONE_ID ).toEpochSecond();
	}
}