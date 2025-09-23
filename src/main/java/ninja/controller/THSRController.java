package ninja.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalUnit;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import net.gpedro.integrations.slack.SlackAttachment;
import ninja.consts.Color;
import ninja.service.THSR;
import ninja.util.Cast;
import ninja.util.Check;
import ninja.util.Slack;
import ninja.util.Utils;

@RestController
public class THSRController extends DialogController {
	private static final String TIME = "DailyTimetable/OD/%s/to/%s/%s", FARE = "ODFare/%s/to/%s";

	private static final String TITLE = "高鐵時刻表與票價查詢", LINK = "https://www.thsrc.com.tw/";

	private enum Way {
		出發( "OriginStopTime/DepartureTime", "ge", "asc" ), 抵達( "DestinationStopTime/ArrivalTime", "le", "desc" );

		private final String field, operator, order;

		private Way( String field, String operator, String order ) {
			this.field = field;
			this.operator = operator;
			this.order = order;
		}
	}

	@Autowired
	private THSR thsr;

	@Override
	protected Object[] args() {
		var way = options( EnumUtils.getEnumMap( Way.class ).keySet() );

		LocalDateTime time = ( time = LocalDateTime.now( ZONE_ID ) ).truncatedTo( ChronoUnit.HOURS ).plusMinutes( 30 * ( int ) Math.ceil( time.getMinute() / 30d ) );

		return ArrayUtils.toArray( TITLE, options( thsr.data().keySet() ), time.toLocalDate(), options( dates() ), time.toLocalTime(), options( times() ), Way.出發, way );
	}

	@PostMapping( "/thsr" )
	@Async
	public void thsr( @RequestParam String command, @RequestParam String text, @RequestParam( RESPONSE_URL ) String url ) {
		var params = Check.station( Check.params( text, 5 ) );

		String start = id( params[ 0 ] ), end = id( params[ 1 ] ), date = params[ 2 ], time = params[ 3 ];

		Check.expr( dates().contains( date ) && times().contains( time ), "時間有誤: " + text );

		var way = Check.name( Way.class, params[ 4 ], "行程有誤: " + text );

		SlackAttachment attach1 = Slack.attachment( TITLE, LINK ), attach2 = Slack.attachment( Color.G );

		Cast.list( thsr.call( String.format( FARE, start, end ) ).get( 0 ), "Fares" ).stream().map( Cast::map ).filter( i -> {
			return Cast.dble( i, "TicketType" ).equals( 1d ) && Cast.dble( i, "FareClass" ).equals( 1d ); // 一般票, 成人
		} ).sorted( ( i, j ) -> price( i ).compareTo( price( j ) ) ).limit( 2 ).forEach( i -> {
			attach1.addFields( field( cabin( i ), "$" + price( i ).intValue() ) );
		} );

		String filter = Utils.spacer( way.field, way.operator, StringUtils.wrap( time, "'" ) ), order = "$orderby=" + Utils.spacer( way.field, way.order );

		var info = thsr.call( String.format( TIME, start, end, date ), filter, order, "$top=10" );

		info.forEach( i -> {
			attach2.addFields( field( "車次", Cast.string( Cast.map( i, "DailyTrainInfo" ), "TrainNo" ) ) );

			attach2.addFields( field( "出發 - 抵達", String.join( " - ", time( i, Way.出發 ), time( i, Way.抵達 ) ) ) );
		} );

		var message = Slack.message( attach1, command, text );

		message( info.size() > 0 ? message.addAttachments( attach2 ) : message, url );
	}

	private String id( String station ) {
		return Check.station( thsr, station );
	}

	private String time( Map<String, ?> map, Way way ) {
		var fields = way.field.split( "/" );

		return Cast.string( Cast.map( map, fields[ 0 ] ), fields[ 1 ] );
	}

	private String cabin( Map<?, ?> map ) {
		var cabin = Cast.dble( map, "CabinClass" );

		return switch ( cabin.intValue() ) {
			case 1 -> "標準";
			case 3 -> "自由";
			default -> "商務";
		};
	}

	private Double price( Map<?, ?> map ) {
		return Cast.dble( map, "Price" );
	}

	private List<String> dates() {
		return iterate( LocalDate.now(), 1, ChronoUnit.DAYS, 14 );
	}

	private List<String> times() {
		return iterate( LocalTime.of( 5, 0 ), 30, ChronoUnit.MINUTES, 38 );
	}

	private List<String> iterate( Temporal temporal, long amount, TemporalUnit unit, long size ) {
		return list( iterate( temporal, i -> i.plus( amount, unit ), size ).map( Temporal::toString ) );
	}
}
