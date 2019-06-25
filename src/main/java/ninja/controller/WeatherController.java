package ninja.controller;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collector;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.fluent.Request;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ObjectArrays;
import com.google.common.collect.Range;
import com.google.common.primitives.Ints;

import magic.util.Utils;
import net.gpedro.integrations.slack.SlackAttachment;
import net.gpedro.integrations.slack.SlackField;
import net.gpedro.integrations.slack.SlackMessage;
import ninja.util.Gson;
import ninja.util.Heroku;
import ninja.util.Slack;

@RestController
@RequestMapping( "/weather" )
public class WeatherController extends BaseController {
	private static final String API_URL = "https://opendata.cwb.gov.tw/api/v1/rest/datastore/F-D0047-061";

	private static final String WEB_URL = "https://www.cwb.gov.tw/V8/C/W/Town/Town.html?TID=", TITLE = "台北市%s天氣預報", DELIMITER = "。";

	private static final String QUERY = "?Authorization=%s&locationName=%s&timeFrom=%s&timeTo=%s&elementName=Wx,AT,WeatherDescription";

	private static final String DEFAULT_DIST = "內湖區", DEFAULT_HOURS = "0", DIALOG_TEMPLATE = "/template/dialog/weather.json";

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

	@Value( "${slack.user.token:}" )
	private String token;

	@PostMapping
	public String weather( @RequestParam String command, @RequestParam String text ) {
		try {
			String[] params = ( params = StringUtils.split( text ) ).length == 0 ? new String[] { DEFAULT_DIST, DEFAULT_HOURS } : params;

			Assert.isTrue( params.length <= 2, "參數個數有誤: " + text );

			params = params.length == 1 ? Ints.tryParse( params[ 0 ] ) != null ? ObjectArrays.concat( DEFAULT_DIST, params ) : ArrayUtils.add( params, DEFAULT_HOURS ) : params;

			String district = StringUtils.appendIfMissing( params[ 0 ], "區" );

			ZonedDateTime time = ZonedDateTime.now( ZoneId.of( "Asia/Taipei" ) );

			Integer hour = time.getHour(), plus = Arrays.asList( 5, 11, 17, 23 ).contains( hour ) ? 9 : 6, town, hours;

			Assert.isTrue( ObjectUtils.allNotNull( town = DISTRICTS.get( district ), hours = Ints.tryParse( params[ 1 ] ) ) && Math.abs( hours ) <= 48, "參數有誤: " + text );

			// 氣象局於5, 11, 17, 23時左右會刷新資料, 但詳細時間不確定; 所以在這些時間多往後抓一個區間再設定limit
			String from = time( time = time.plusHours( hours ).with( LocalTime.of( hour / 3 * 3, 0 ) ) ), to = time( time.plusHours( plus ) );

			log.info( "From: {}, to: {}", from, to );

			Map<?, ?> result = Gson.from( Utils.getEntityAsString( Request.Get( API_URL + String.format( QUERY, key, district, from, to ) ) ), Map.class );

			SlackAttachment attachment = Slack.attachment().setTitle( String.format( TITLE, district ) ).setTitleLink( WEB_URL + town );

			SlackMessage message = Slack.message( attachment, command, text );

			List<?> elements = list( first( first( map( result, "records" ), "locations" ), "location" ), "weatherElement" );

			Map<String, String> image = new HashMap<>(), at = new HashMap<>();

			each( elements, "Wx", j -> {
				String start = string( j, "startTime" ), when = Range.closedOpen( 6, 18 ).contains( hour( start ) ) ? "day" : "night";

				image.put( start, String.format( url, when, string( map( list( j, "elementValue" ).get( 1 ) ), "value" ) ) );
			} );

			each( elements, "AT", j -> at.put( string( j, "dataTime" ), string( first( j, "elementValue" ), "value" ) ) );

			each( elements, "WeatherDescription", j -> {
				String[] data = string( first( j, "elementValue" ), "value" ).split( DELIMITER );

				String weather = data[ 0 ], color = weather.contains( "晴" ) ? "good" : weather.contains( "雨" ) ? "danger" : "warning";

				String wind = StringUtils.remove( RegExUtils.replaceFirst( data[ 4 ], StringUtils.SPACE, "，" ), StringUtils.SPACE ), start;

				int hr = hour( start = string( j, "startTime" ) );

				String period = hr == 12 ? "中午" : hr >= 0 && hr < 6 ? "凌晨" : hr >= 6 && hr < 12 ? "早上" : hr >= 13 && hr < 18 ? "下午" : "晚上";

				String title = start.substring( 0, 11 ) + period + ( hr > 12 ? hr - 12 : hr ) + "點";

				SlackAttachment attach = Slack.attachment().setAuthorName( title ).setAuthorIcon( image.get( start ) );

				attach.addFields( super.field( "溫度 / 體感", data[ 2 ].substring( 4, 6 ) + " / " + at.get( start ) + "˚C" ) );

				attach.addFields( super.field( "舒適度", data[ 3 ] ) ).addFields( field( data[ 1 ], 4 ) ).addFields( field( data[ 5 ], 4 ) );

				message.addAttachments( attach.setText( weather + DELIMITER + wind ).setColor( color ) );
			} );

			return message.prepare().toString();

		} catch ( RuntimeException e ) {
			log.error( "", e );

			return e.getMessage();

		}
	}

	@PostMapping( "/dialog" )
	public void dialog( @RequestParam( "trigger_id" ) String id ) {
		String district = json( DISTRICTS.entrySet().stream().map( i -> option( i.getKey(), i.getValue() ) ) );

		String hours = json( IntStream.rangeClosed( 0, 48 ).filter( i -> i % 6 == 0 ).mapToObj( i -> option( i == 0 ? "現在" : i + "小時後", i ) ) );

		String dialog = String.format( Utils.getResourceAsString( DIALOG_TEMPLATE ), Heroku.TASK_ID, district, hours );

		log.info( post( "dialog.open", token, ImmutableMap.of( "trigger_id", id, "dialog", dialog ) ) );
	}

	private void each( List<?> elements, String name, Consumer<? super Map<?, ?>> action ) {
		elements.stream().map( this::map ).filter( i -> name.equals( i.get( "elementName" ) ) ).forEach( i -> {
			list( i, "time" ).stream().limit( 2 ).map( this::map ).forEach( action );
		} );
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

	private Map<String, String> option( String label, Integer value ) {
		return ImmutableMap.of( "label", label, "value", value.toString() );
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

	private String json( Stream<Map<String, String>> stream ) {
		return stream.collect( Collector.of( JSONArray::new, JSONArray::put, JSONArray::put ) ).toString();
	}

	private int hour( String time ) {
		return LocalDateTime.parse( time, DATE_TIME_FORMATTER ).getHour();
	}

	private SlackField field( String data, int index ) {
		return super.field( data.substring( 0, index ), data.substring( index ).trim() );
	}
}