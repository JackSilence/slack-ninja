package ninja.controller;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.stream.Collectors;

import org.apache.commons.codec.digest.HmacAlgorithms;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.fluent.Request;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.reflect.TypeToken;

import magic.util.Utils;
import net.gpedro.integrations.slack.SlackAttachment;
import net.gpedro.integrations.slack.SlackMessage;
import ninja.util.Gson;
import ninja.util.Slack;

@RestController
public class BusController extends BaseController {
	private static final String AUTH_HEADER = "hmac username=\"%s\", algorithm=\"hmac-sha1\", headers=\"x-date\", signature=\"%s\"";

	private static final String API_URL = "https://ptx.transportdata.tw/MOTC/v2/Bus/%s/City/Taipei/%s?$format=JSON&%s";

	private static final String WEB_URL = "http://www.e-bus.gov.taipei/newmap/Tw/Map?rid=%s&sec=0";

	@Value( "${ptx.app.id:}" )
	private String id;

	@Value( "${ptx.app.key:}" )
	private String key;

	@PostMapping( "/bus" )
	public String bus( @RequestParam String command, @RequestParam String text ) {
		try {
			String[] params = ( params = StringUtils.split( text ) ).length == 1 ? ArrayUtils.add( params, StringUtils.EMPTY ) : params;

			Assert.isTrue( params.length == 2, "路線及站牌關鍵字皆須輸入" );

			String route = params[ 0 ], keyword = params[ 1 ];

			Map<String, ?> bus = call( "Route", route ).stream().findFirst().orElseThrow( () -> new IllegalArgumentException( "查無路線: " + route ) );

			String departure = string( bus, "DepartureStopNameZh" ), destination = string( bus, "DestinationStopNameZh" );

			SlackAttachment attachment = Slack.attachment().setTitle( route + "公車動態" ).setTitleLink( String.format( WEB_URL, bus.get( "RouteID" ) ) );

			SlackMessage message = Slack.message( attachment, command, text );

			if ( keyword.isEmpty() ) {
				return message( message );
			}

			call( "EstimatedTimeOfArrival", route, "$orderby=Direction" ).stream().filter( i -> {
				return station( i ).contains( keyword ) && Arrays.asList( 0d, 1d ).contains( direction( i ) ); // 0: 去程, 1: 返程

			} ).collect( Collectors.groupingBy( i -> station( i ), Collectors.toList() ) ).forEach( ( k, v ) -> {
				message.addAttachments( Slack.attachment().setText( ":busstop:" + k ).setColor( "#3AA3E3" ).setFields( v.stream().map( i -> {
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

	private List<Map<String, ?>> call( String method, String route, String... query ) {
		Request request = Request.Get( String.format( API_URL, method, route, String.join( "&", query ) ) );

		String xdate = xdate(), signature = Base64.getEncoder().encodeToString( signature( "x-date: " + xdate, key, HmacAlgorithms.HMAC_SHA_1 ) );

		request.addHeader( "Authorization", String.format( AUTH_HEADER, id, signature ) ).addHeader( "x-date", xdate );

		return Gson.from( Utils.getEntityAsString( request.addHeader( "Accept-Encoding", "gzip" ) ), new TypeToken<List<?>>() {
		}.getType() );
	}

	private String station( Map<String, ?> map ) {
		return string( map( map, "StopName" ), "Zh_tw" );
	}

	private String xdate() {
		SimpleDateFormat sdf = new SimpleDateFormat( "EEE, dd MMM yyyy HH:mm:ss z", Locale.US );

		sdf.setTimeZone( TimeZone.getTimeZone( "GMT" ) );

		return sdf.format( new Date() );
	}

	private Double direction( Map<String, ?> map ) {
		return ( Double ) map.get( "Direction" );
	}
}