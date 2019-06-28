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

import net.gpedro.integrations.slack.SlackAttachment;
import net.gpedro.integrations.slack.SlackMessage;
import ninja.consts.Dialog;
import ninja.service.Transport;
import ninja.util.Cast;
import ninja.util.Slack;

@RestController
public class BusController extends BaseController {
	private static final String WEB_URL = "http://www.e-bus.gov.taipei/newmap/Tw/Map?rid=%s&sec=0";

	@Autowired
	private Transport transport;

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

			Map<String, ?> bus = transport.call( "Route", route ).stream().findFirst().orElseThrow( () -> new IllegalArgumentException( "查無路線: " + route ) );

			String departure = Cast.string( bus, "DepartureStopNameZh" ), destination = Cast.string( bus, "DestinationStopNameZh" );

			SlackAttachment attachment = Slack.attachment().setTitle( route + "公車動態" ).setTitleLink( String.format( WEB_URL, bus.get( "RouteID" ) ) );

			SlackMessage message = Slack.message( attachment, command, text );

			if ( keyword.isEmpty() ) {
				return message( message );
			}

			transport.call( "EstimatedTimeOfArrival", route, "$orderby=Direction" ).stream().filter( i -> {
				return transport.stop( i ).contains( keyword ) && Arrays.asList( 0d, 1d ).contains( direction( i ) ); // 0: 去程, 1: 返程

			} ).collect( Collectors.groupingBy( i -> transport.stop( i ), Collectors.toList() ) ).forEach( ( k, v ) -> {
				message.addAttachments( Slack.attachment().setText( ":busstop:" + k ).setColor( "good" ).setFields( v.stream().map( i -> {
					int time = ( ( Double ) i.get( "EstimateTime" ) ).intValue(), minutes = time / 60, seconds = time % 60;

					String value = ( minutes > 0 ? minutes + "分" : StringUtils.EMPTY ) + seconds + "秒";

					return field( "往".concat( direction( i ).equals( 0d ) ? destination : departure ), value );

				} ).collect( Collectors.toList() ) ) );
			} );

			return message( message );

		} catch ( RuntimeException e ) {
			log.error( "", e );

			return e.getMessage();

		}
	}

	private Double direction( Map<String, ?> map ) {
		return ( Double ) map.get( "Direction" );
	}
}