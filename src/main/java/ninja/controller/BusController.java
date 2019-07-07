package ninja.controller;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;

import net.gpedro.integrations.slack.SlackAttachment;
import net.gpedro.integrations.slack.SlackMessage;
import ninja.consts.Dialog;
import ninja.consts.Filter;
import ninja.service.Bus;
import ninja.util.Cast;
import ninja.util.Slack;

@RestController
public class BusController extends BaseController {
	private static final String WEB_URL = "https://ebus.gov.taipei/EBus/VsSimpleMap?routeid=%s&gb=0";

	private static final Map<Double, String> STATUS = ImmutableMap.of( 1d, "尚未發車", 2d, "交管不停靠", 3d, "末班車已過", 4d, "今日未營運" );

	@Autowired
	private Bus bus;

	@PostMapping( "/bus" )
	public String bus( @RequestParam String command, @RequestParam String text, @RequestParam( TRIGGER_ID ) String id ) {
		if ( text.isEmpty() ) {
			dialog( id, Dialog.BUS );

			return StringUtils.EMPTY;
		}

		try {
			String[] params = Arrays.copyOf( params = StringUtils.split( text ), Math.max( params.length, 3 ) );

			check( params.length == 3, "參數個數有誤: " + text );

			String route = params[ 0 ], stop = params[ 1 ], source = params[ 2 ], filter;

			check( bus.check( route ) && StringUtils.equalsAny( source, null, DIALOG ), "參數有誤: " + text );

			Map<String, ?> info = bus.call( "Route", filter = Filter.ROUTE.eq( route ) ).get( 0 ); // 原則上不可能拿不到

			String departure = Cast.string( info, "DepartureStopNameZh" ), destination = Cast.string( info, "DestinationStopNameZh" );

			SlackAttachment attachment = Slack.attachment().setTitle( route + "公車動態" ).setTitleLink( String.format( WEB_URL, bus.id( route ) ) );

			SlackMessage message = Slack.message( attachment, command, text );

			if ( stop == null ) {
				return message( message );
			}

			filter = Filter.and( filter, DIALOG.equals( source ) ? Filter.STOP.eq( stop ) : Filter.STOP.contains( stop ), Filter.DIRECTION.le( "1" ) );

			bus.call( "EstimatedTimeOfArrival", filter, "$orderby=Direction" ).stream().collect( Collectors.groupingBy( bus::stop, Collectors.toList() ) ).forEach( ( k, v ) -> {
				message.addAttachments( Slack.attachment().setText( ":busstop:" + k ).setColor( "good" ).setFields( v.stream().map( i -> {
					Double direction = ( Double ) i.get( "Direction" ), time = ( Double ) i.get( "EstimateTime" ), status = ( Double ) i.get( "StopStatus" );

					return field( "往".concat( direction.equals( 0d ) ? destination : departure ), time == null ? STATUS.get( status ) : time( time ) );

				} ).collect( Collectors.toList() ) ) );
			} );

			return message( message );

		} catch ( RuntimeException e ) {
			log.error( "", e );

			return e.getMessage();

		}
	}

	@PostMapping( "/station" )
	public String station( @RequestParam( CHANNEL_ID ) String channel, @RequestParam( "user_name" ) String user, @RequestParam String command, @RequestParam String text, @RequestParam( TRIGGER_ID ) String id ) {
		if ( text.isEmpty() ) {
			dialog( id, Dialog.STATION );

			return StringUtils.EMPTY;
		}

		try {
			String[] params = StringUtils.split( text );

			check( params.length == 2, "參數個數有誤: " + text );

			String start = params[ 0 ], end = params[ 1 ], filter = Filter.or( Filter.STATION.eq( start ), Filter.STATION.eq( end ) );

			Map<String, Set<String>> info = bus.call( "Station", filter ).stream().collect( Collectors.toMap( bus::station, i -> {
				return bus.stops( i, j -> bus.name( j, "RouteName" ) ).collect( Collectors.toSet() );

			}, Sets::union ) );

			check( info.keySet().size() == 2, "查無起站或訖站: " + text );

			Sets.intersection( info.get( start ), info.get( end ) ).parallelStream().forEach( i -> command( user, channel, "bus", i + "%20" + start ) );

			return message( Slack.message( Slack.attachment(), command, text ) );

		} catch ( RuntimeException e ) {
			log.error( "", e );

			return e.getMessage();

		}
	}

	private String time( Double time ) {
		int seconds = time.intValue(), minutes = seconds / 60;

		return ( minutes > 0 ? minutes + "分" : StringUtils.EMPTY ) + seconds % 60 + "秒";
	}
}