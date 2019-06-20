package ninja.controller;

import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.fluent.Request;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import magic.util.Utils;
import net.gpedro.integrations.slack.SlackAttachment;
import net.gpedro.integrations.slack.SlackField;
import net.gpedro.integrations.slack.SlackMessage;
import ninja.util.Gson;
import ninja.util.Slack;

@RestController
public class WeatherController extends BaseController {
	private static final String API_URL = "https://opendata.cwb.gov.tw/api/v1/rest/datastore/F-D0047-061";

	private static final String WEB_URL = "https://www.cwb.gov.tw/V8/C/W/Town/Town.html?TID=", TITLE = "台北市%s天氣預報";

	private static final String QUERY = "?Authorization=%s&locationName=%s&timeFrom=%s&timeTo=%s", DELIMITER = "。";

	private static final Map<String, Integer> DISTRICTS = new HashMap<>();

	static {
		DISTRICTS.put( "中正區", 6300500 );
		DISTRICTS.put( "大同區", 6300600 );
		DISTRICTS.put( "中山區", 6300400 );
		DISTRICTS.put( "松山區", 6300100 );
		DISTRICTS.put( "大安區", 6300300 );
		DISTRICTS.put( "萬華區", 6300700 );
		DISTRICTS.put( "信義區", 6300200 );
		DISTRICTS.put( "士林區", 6301100 );
		DISTRICTS.put( "北投區", 6301200 );
		DISTRICTS.put( "內湖區", 6301000 );
		DISTRICTS.put( "南港區", 6300900 );
		DISTRICTS.put( "文山區", 6300800 );
	}

	@Value( "${cwb.api.key:}" )
	private String key;

	@PostMapping( "/weather" )
	public String weather( @RequestParam String command, @RequestParam String text ) {
		String district = StringUtils.appendIfMissing( StringUtils.defaultIfEmpty( text, "內湖區" ), "區" );

		ZonedDateTime time = ZonedDateTime.now( ZoneId.of( "Asia/Taipei" ) );

		Integer hour = time.getHour() / 3 * 3, town;

		String start = time( time = time.with( LocalTime.of( hour, 0 ) ) ), end = time( time.plusHours( 6 ) );

		log.info( "Start: {}, end: {}", start, end );

		try {
			Assert.notNull( town = DISTRICTS.get( district ), "查無此行政區: " + text );

			Map<?, ?> result = Gson.from( Utils.getEntityAsString( Request.Get( API_URL + String.format( QUERY, key, district, start, end ) ) ), Map.class );

			SlackAttachment attachment = Slack.attachment().setTitle( String.format( TITLE, district ) ).setTitleLink( WEB_URL + town );

			SlackMessage message = Slack.message( attachment, command, text );

			List<?> elements = list( first( first( map( result, "records" ), "locations" ), "location" ), "weatherElement" );

			elements.stream().map( this::map ).filter( i -> "WeatherDescription".equals( i.get( "elementName" ) ) ).forEach( i -> {
				list( i, "time" ).stream().map( this::map ).forEach( j -> {
					SlackAttachment attach = Slack.attachment().setTitle( string( j, "startTime" ) );

					String[] data = string( first( j, "elementValue" ), "value" ).split( DELIMITER );

					attach.addFields( field( data[ 2 ], 2 ) ).addFields( super.field( "舒適度", data[ 3 ] ) );

					attach.addFields( field( data[ 1 ], 4 ) ).addFields( field( data[ 5 ], 4 ) );

					String weather = data[ 0 ], color = weather.contains( "晴" ) ? "good" : weather.contains( "雨" ) ? "danger" : "warning";

					String wind = StringUtils.remove( RegExUtils.replaceFirst( data[ 4 ], StringUtils.SPACE, "，" ), StringUtils.SPACE );

					message.addAttachments( attach.setText( weather + DELIMITER + wind ).setColor( color ) );
				} );
			} );

			return message.prepare().toString();

		} catch ( RuntimeException e ) {
			log.error( "", e );

			return e.getMessage();

		}
	}

	private Map<?, ?> map( Map<?, ?> map, String key ) {
		return map( map.get( key ) );
	}

	private Map<?, ?> map( Object object ) {
		return ( Map<?, ?> ) object;
	}

	private Map<?, ?> first( Map<?, ?> map, String key ) {
		return map( list( map, key ).get( 0 ) );
	}

	private List<?> list( Map<?, ?> map, String key ) {
		return ( List<?> ) map.get( key );
	}

	private String string( Map<?, ?> map, String key ) {
		return ( String ) map.get( key );
	}

	private String time( ZonedDateTime time ) {
		return time.toLocalDateTime() + ":00";
	}

	private SlackField field( String data, int index ) {
		return super.field( data.substring( 0, index ), data.substring( index ).trim() );
	}
}