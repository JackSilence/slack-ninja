package ninja.controller;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalUnit;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import net.gpedro.integrations.slack.SlackAttachment;
import net.gpedro.integrations.slack.SlackField;
import net.gpedro.integrations.slack.SlackMessage;
import ninja.service.THSR;
import ninja.util.Cast;
import ninja.util.Slack;

@RestController
public class THSRController extends DialogController {
	private static final String PATH = "DailyTimetable/OD/%s/to/%s/%s", TITLE = "高鐵時刻表與票價查詢";

	private static final String URL = "https://m.thsrc.com.tw/tw/TimeTable/%s/", SEARCH = "SearchResult", INFO = "TrainInfo";

	private static final Map<String, String> STATIONS = new LinkedHashMap<>();

	private enum Way {
		DEPARTURE( "出發", "OriginStopTime/DepartureTime", "ge", "asc" ), ARRIVAL( "抵達", "DestinationStopTime/ArrivalTime", "le", "desc" );

		private final String text, field, operator, order;

		private Way( String text, String field, String operator, String order ) {
			this.text = text;
			this.field = field;
			this.operator = operator;
			this.order = order;
		}
	}

	@Autowired
	private THSR thsr;

	@Override
	protected Object[] args() {
		String way = json( Arrays.stream( Way.values() ).map( i -> option( i.text, i.name() ) ) );

		return ArrayUtils.toArray( TITLE, options( STATIONS.keySet() ), options( dates() ), options( times() ), way );
	}

	@PostMapping( "/thsr" )
	public String thsr( @RequestParam String command, @RequestParam String text ) {
		try {
			String[] params = StringUtils.split( text );

			check( params.length == 5, "參數個數有誤: " + text );

			String start = id( params[ 0 ] ), end = id( params[ 1 ] ), date = params[ 2 ], time = params[ 3 ];

			check( !start.equals( end ), "起訖站不得重複: " + text );

			check( dates().contains( date ) && times().contains( time ), "時間有誤: " + text );

			Way way = check( Way.class, params[ 4 ], "方向有誤: " + text );

			SlackMessage message = Slack.message( Slack.attachment().setTitle( TITLE ).setTitleLink( String.format( URL, SEARCH ) ), command, text );

			String filter = join( StringUtils.SPACE, way.field, way.operator, time ), order = join( way.field, way.order );

			return message( message.setAttachments( thsr.call( String.format( PATH, start, end, date ), filter, order, "$top=4" ).stream().map( i -> {
				SlackAttachment attach = attachment( Cast.string( Cast.map( i, "DailyTrainInfo" ), "TrainNo" ) );

				return attach.setColor( "good" ).addFields( field( i, Way.DEPARTURE ) ).addFields( field( i, Way.ARRIVAL ) );

			} ).collect( Collectors.toList() ) ) );

		} catch ( RuntimeException e ) {
			log.error( "", e );

			return e.getMessage();

		}
	}

	private String id( String station ) {
		return checkNull( STATIONS.get( station ), "查無此站: " + station );
	}

	private String join( String... elements ) {
		return String.join( StringUtils.SPACE, elements );
	}

	private SlackAttachment attachment( String train ) {
		return Slack.attachment().setTitle( ":bullettrain_front:" + train ).setTitleLink( String.format( URL, INFO ) + train );
	}

	private SlackField field( Map<String, ?> map, Way way ) {
		String[] fields = way.field.split( "/" );

		return super.field( way.text, Cast.string( Cast.map( map, fields[ 0 ] ), fields[ 1 ] ) );
	}

	private List<String> dates() {
		return iterate( LocalDate.now(), 1, ChronoUnit.DAYS, 14 );
	}

	private List<String> times() {
		return iterate( LocalTime.of( 5, 0 ), 30, ChronoUnit.MINUTES, 38 );
	}

	private List<String> iterate( Temporal temporal, long amount, TemporalUnit unit, long size ) {
		return iterate( temporal, i -> i.plus( amount, unit ), size ).map( Temporal::toString ).collect( Collectors.toList() );
	}

	@PostConstruct
	private void init() {
		thsr.call( "Station", StringUtils.EMPTY ).forEach( i -> STATIONS.put( thsr.station( i ), Cast.string( i, "StationID" ) ) );
	}
}