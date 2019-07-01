package ninja.controller;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.ImmutableMap;

import net.gpedro.integrations.slack.SlackAttachment;
import net.gpedro.integrations.slack.SlackMessage;
import ninja.consts.Dialog;
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
	public String bus( @RequestParam String command, @RequestParam String text, @RequestParam( "trigger_id" ) String id ) {
		if ( text.isEmpty() ) {
			dialog( id, Dialog.BUS );

			return StringUtils.EMPTY;
		}

		try {
			String[] params = ( params = StringUtils.split( text ) ).length == 1 ? ArrayUtils.add( params, StringUtils.EMPTY ) : params;

			Assert.isTrue( params.length == 2, "參數個數有誤: " + text );

			String route = params[ 0 ], keyword = params[ 1 ];

			Assert.isTrue( bus.check( route ), "查無路線: " + route );

			Map<String, ?> info = bus.call( "Route", route ).get( 0 ); // 原則上不可能拿不到

			String departure = Cast.string( info, "DepartureStopNameZh" ), destination = Cast.string( info, "DestinationStopNameZh" );

			SlackAttachment attachment = Slack.attachment().setTitle( route + "公車動態" ).setTitleLink( String.format( WEB_URL, bus.id( route ) ) );

			SlackMessage message = Slack.message( attachment, command, text );

			if ( keyword.isEmpty() ) {
				return message( message );
			}

			bus.call( "EstimatedTimeOfArrival", route, "$orderby=Direction" ).stream().filter( i -> {
				return bus.stop( i ).contains( keyword ) && Arrays.asList( 0d, 1d ).contains( direction( i ) ); // 0: 去程, 1: 返程

			} ).collect( Collectors.groupingBy( i -> bus.stop( i ), Collectors.toList() ) ).forEach( ( k, v ) -> {
				message.addAttachments( Slack.attachment().setText( ":busstop:" + k ).setColor( "good" ).setFields( v.stream().map( i -> {
					Double time = ( Double ) i.get( "EstimateTime" ), status = ( Double ) i.get( "StopStatus" );

					return field( "往".concat( direction( i ).equals( 0d ) ? destination : departure ), time == null ? STATUS.get( status ) : time( time ) );

				} ).collect( Collectors.toList() ) ) );
			} );

			return message( message );

		} catch ( RuntimeException e ) {
			log.error( "", e );

			return e.getMessage();

		}
	}

	private String time( Double time ) {
		int seconds = time.intValue(), minutes = seconds / 60;

		return ( minutes > 0 ? minutes + "分" : StringUtils.EMPTY ) + seconds % 60 + "秒";
	}

	private Double direction( Map<String, ?> map ) {
		return ( Double ) map.get( "Direction" );
	}
}