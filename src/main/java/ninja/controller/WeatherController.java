package ninja.controller;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.fluent.Request;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.ObjectArrays;
import com.google.common.collect.Range;
import com.google.common.primitives.Ints;

import magic.util.Utils;
import net.gpedro.integrations.slack.SlackAttachment;
import net.gpedro.integrations.slack.SlackField;
import net.gpedro.integrations.slack.SlackMessage;
import ninja.util.Cast;
import ninja.util.Gson;
import ninja.util.Slack;

@RestController
@RequestMapping( "/weather" )
public class WeatherController extends DialogController {
	private static final String API_URL = "https://opendata.cwb.gov.tw/api/v1/rest/datastore/F-D0047-061";

	private static final String WEB_URL = "https://www.cwb.gov.tw/V8/C/W/Town/Town.html?TID=", TITLE = "台北市%s天氣預報", DELIMITER = "。";

	private static final String QUERY = "?Authorization=%s&locationName=%s&timeFrom=%s&timeTo=%s&elementName=Wx,AT,WeatherDescription";

	private static final String DEFAULT_DIST = "內湖區", DEFAULT_HOURS = "0";

	private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern( "yyyy-MM-dd HH:mm:ss" );

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

	@Value( "${cwb.icon.url:}" )
	private String url;

	@Override
	protected Object[] args() {
		String hours = json( iterate( 0, i -> i + 6, 9 ).map( i -> option( i == 0 ? "現在" : i + "小時後", i ) ) );

		return ArrayUtils.toArray( DEFAULT_DIST, options( DISTRICTS.keySet() ), hours );
	}

	@PostMapping
	public String weather( @RequestParam String command, @RequestParam String text ) {
		try {
			String[] params = ( params = StringUtils.split( text ) ).length == 0 ? new String[] { DEFAULT_DIST, DEFAULT_HOURS } : params;

			check( params.length <= 2, "參數個數有誤: " + text );

			params = params.length == 1 ? Ints.tryParse( params[ 0 ] ) != null ? ObjectArrays.concat( DEFAULT_DIST, params ) : ArrayUtils.add( params, DEFAULT_HOURS ) : params;

			String district = StringUtils.appendIfMissing( params[ 0 ], "區" );

			ZonedDateTime time = ZonedDateTime.now( ZONE_ID );

			Integer hour = time.getHour(), plus = Arrays.asList( 5, 11, 17, 23 ).contains( hour ) ? 9 : 6, town, hours;

			check( ObjectUtils.allNotNull( town = DISTRICTS.get( district ), hours = Ints.tryParse( params[ 1 ] ) ) && hours >= -12 && hours <= 48, "參數有誤: " + text );

			// 氣象局於5, 11, 17, 23時左右會刷新資料, 但詳細時間不確定; 所以在這些時間多往後抓一個區間再設定limit
			String from = time( time = time.with( LocalTime.of( hour / 3 * 3, 0 ) ).plusHours( hours ) ), to = time( time.plusHours( plus ) );

			log.info( "From: {}, to: {}", from, to );

			Map<?, ?> result = Gson.from( Utils.getEntityAsString( Request.Get( API_URL + String.format( QUERY, key, district, from, to ) ) ), Map.class );

			SlackMessage message = Slack.message( attachment( String.format( TITLE, district ), WEB_URL + town ), command, text );

			List<?> elements = Cast.list( first( first( Cast.map( result, "records" ), "locations" ), "location" ), "weatherElement" );

			Map<String, String> image = new HashMap<>(), at = new HashMap<>();

			each( elements, "Wx", j -> {
				String start = Cast.string( j, "startTime" ), when = Range.closedOpen( 6, 18 ).contains( hour( start ) ) ? "day" : "night";

				image.put( start, String.format( url, when, Cast.string( Cast.map( Cast.list( j, "elementValue" ).get( 1 ) ), "value" ) ) );
			} );

			each( elements, "AT", j -> at.put( Cast.string( j, "dataTime" ), Cast.string( first( j, "elementValue" ), "value" ) ) );

			each( elements, "WeatherDescription", j -> {
				String[] data = Cast.string( first( j, "elementValue" ), "value" ).split( DELIMITER );

				String ci = data[ 3 ], color = "舒適".equals( ci ) ? "good" : "悶熱".equals( ci ) ? "warning" : "易中暑".equals( ci ) ? "danger" : "#3AA3E3";

				String wind = StringUtils.remove( RegExUtils.replaceFirst( data[ 4 ], StringUtils.SPACE, "，" ), StringUtils.SPACE ), start;

				int hr = hour( start = Cast.string( j, "startTime" ) );

				String period = hr == 12 ? "中午" : hr >= 0 && hr < 6 ? "凌晨" : hr >= 6 && hr < 12 ? "早上" : hr >= 13 && hr < 18 ? "下午" : "晚上";

				String title = start.substring( 0, 11 ) + period + ( hr > 12 ? hr - 12 : hr ) + "點";

				SlackAttachment attach = Slack.attachment( color ).setAuthorName( title ).setAuthorIcon( image.get( start ) );

				attach.addFields( super.field( "溫度 / 體感", data[ 2 ].substring( 4, 6 ) + " / " + at.get( start ) + "˚C" ) );

				attach.addFields( super.field( "舒適度", ci ) ).addFields( field( data[ 1 ], 4 ) ).addFields( field( data[ 5 ], 4 ) );

				message.addAttachments( attach.setText( data[ 0 ] + DELIMITER + wind ) );
			} );

			return message( message );

		} catch ( RuntimeException e ) {
			log.error( "", e );

			return e.getMessage();

		}
	}

	private void each( List<?> elements, String name, Consumer<? super Map<?, ?>> action ) {
		elements.stream().map( Cast::map ).filter( i -> name.equals( i.get( "elementName" ) ) ).forEach( i -> {
			Cast.list( i, "time" ).stream().limit( 2 ).map( Cast::map ).forEach( action );
		} );
	}

	private Map<?, ?> first( Map<?, ?> map, String key ) {
		return Cast.map( Cast.list( map, key ).get( 0 ) );
	}

	private String time( ZonedDateTime time ) {
		return time.toLocalDateTime() + ":00";
	}

	private int hour( String time ) {
		return LocalDateTime.parse( time, DATE_TIME_FORMATTER ).getHour();
	}

	private SlackAttachment attachment( String title, String link ) {
		return Slack.attachment().setTitle( title ).setFallback( title ).setTitleLink( link );
	}

	private SlackField field( String data, int index ) {
		return super.field( data.substring( 0, index ), data.substring( index ).trim() );
	}
}