package ninja.controller;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.Range;
import com.google.common.primitives.Ints;

import net.gpedro.integrations.slack.SlackField;
import ninja.consts.Color;
import ninja.util.Cast;
import ninja.util.Check;
import ninja.util.Gson;
import ninja.util.Slack;
import ninja.util.Utils;

@RestController
public class WeatherController extends DialogController {
	private static final String API_URL = "https://opendata.cwa.gov.tw/api/v1/rest/datastore/F-D0047-061";

	private static final String WEB_URL = "https://www.cwa.gov.tw/V8/C/W/Town/Town.html?TID=", TITLE = "台北市%s天氣預報", DELIMITER = "。";

	private static final String QUERY = "?Authorization=%s&LocationName=%s&timeFrom=%s&timeTo=%s&ElementName=天氣現象,體感溫度,天氣預報綜合描述";

	private static final String START_TIME = "StartTime", ELEMENT_VALUE = "ElementValue", DEFAULT_DIST = "內湖區", DEFAULT_HOURS = "0";

	private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern( "yyyy-MM-dd'T'HH:mm:ssXXX" );

	private static final Map<String, Integer> DISTRICTS = new LinkedHashMap<>();

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
		var hours = json( iterate( 0, i -> i + 6, 9 ).map( i -> option( i == 0 ? "現在" : i + "小時後", i ) ) );

		return ArrayUtils.toArray( DEFAULT_DIST, options( DISTRICTS.keySet() ), DEFAULT_HOURS, hours );
	}

	@PostMapping( "/weather" )
	@Async
	public void weather( @RequestParam String command, @RequestParam String text, @RequestParam( RESPONSE_URL ) String url ) {
		var params = Check.params( StringUtils.defaultIfEmpty( text, Utils.spacer( DEFAULT_DIST, DEFAULT_HOURS ) ) );

		var district = StringUtils.appendIfMissing( params[ 0 ], "區" );

		var time = ZonedDateTime.now( ZONE_ID );

		Integer hour = time.getHour(), plus = List.of( 5, 11, 17, 23 ).contains( hour ) ? 9 : 6, town, hours;

		Check.expr( ObjectUtils.allNotNull( town = DISTRICTS.get( district ), hours = Ints.tryParse( params[ 1 ] ) ) && hours >= -12 && hours <= 48, "參數有誤: " + text );

		// 氣象局於5, 11, 17, 23時左右會刷新資料, 但詳細時間不確定; 所以在這些時間多往後抓一個區間再設定limit
		String from = time( time = time.with( LocalTime.of( hour / 3 * 3, 0 ) ).plusHours( hours ) ), to = time( time.plusHours( plus ) );

		log.info( "From: {}, to: {}", from, to );

		var result = Gson.from( Utils.call( API_URL + String.format( QUERY, key, district, from, to ) ), Map.class );

		var message = Slack.message( Slack.attachment( String.format( TITLE, district ), WEB_URL + town ), command, text );

		var elements = Cast.list( first( first( Cast.map( result, "records" ), "Locations" ), "Location" ), "WeatherElement" );

		Map<String, String> image = new HashMap<>(), at = new HashMap<>();

		each( elements, "天氣現象", j -> {
			String start = Cast.string( j, START_TIME ), when = Range.closedOpen( 6, 18 ).contains( hour( start ) ) ? "day" : "night";

			image.put( start, String.format( this.url, when, Cast.string( first( j, ELEMENT_VALUE ), "WeatherCode" ) ) );
		} );

		each( elements, "體感溫度", j -> at.put( Cast.string( j, "DataTime" ), Cast.string( first( j, ELEMENT_VALUE ), "ApparentTemperature" ) ), 4 );

		each( elements, "天氣預報綜合描述", j -> {
			var data = Cast.string( first( j, ELEMENT_VALUE ), "WeatherDescription" ).split( DELIMITER );

			String ci = data[ 3 ], wind = StringUtils.remove( RegExUtils.replaceFirst( data[ 4 ], StringUtils.SPACE, "，" ), StringUtils.SPACE ), start;

			var color = switch ( StringUtils.defaultString( ci ) ) {
				case "舒適" -> Color.G;
				case "悶熱" -> Color.Y;
				case "易中暑" -> Color.R;
				default -> Color.B;
			};

			var hr = hour( start = Cast.string( j, START_TIME ) );

			var period = switch ( hr ) {
				case 12 -> "中午";
				case 0, 1, 2, 3, 4, 5 -> "凌晨";
				case 6, 7, 8, 9, 10, 11 -> "早上";
				case 13, 14, 15, 16, 17 -> "下午";
				default -> "晚上";
			};

			var title = start.substring( 0, 11 ) + period + ( hr > 12 ? hr - 12 : hr ) + "點";

			var attach = Slack.author( Slack.attachment( color ), title, null, image.get( start ) );

			attach.addFields( super.field( "溫度 / 體感", data[ 2 ].substring( 4, 6 ) + " / " + at.get( start ) + "˚C" ) );

			attach.addFields( super.field( "舒適度", ci ) ).addFields( field( data[ 1 ], 4 ) ).addFields( field( data[ 5 ], 4 ) );

			message.addAttachments( attach.setText( data[ 0 ] + DELIMITER + wind ) );
		} );

		message( message, url );
	}

	private void each( List<?> elements, String name, Consumer<? super Map<?, ?>> action ) {
		each( elements, name, action, 2 );
	}

	private void each( List<?> elements, String name, Consumer<? super Map<?, ?>> action, long limit ) {
		elements.stream().map( Cast::map ).filter( i -> name.equals( i.get( "ElementName" ) ) ).forEach( i -> {
			Cast.list( i, "Time" ).stream().limit( limit ).map( Cast::map ).forEach( action );
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

	private SlackField field( String data, int index ) {
		return super.field( data.substring( 0, index ), data.substring( index ).trim() );
	}
}
