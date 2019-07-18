package ninja.controller;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import net.gpedro.integrations.slack.SlackAttachment;
import ninja.consts.Filter;
import ninja.service.AQI;
import ninja.util.Cast;
import ninja.util.Slack;

@RestController
public class AQIController extends BaseController {
	private static final String DEFAULT = "臺北市 松山", TITLE = "空氣品質監測網", LINK = "https://airtw.epa.gov.tw", NA = "N/A";

	private static final Map<String, String> TITLES = new LinkedHashMap<>(), UNITS = new HashMap<>();

	static {
		TITLES.put( "PM2.5", "細懸浮微粒" );
		TITLES.put( "PM10", "懸浮微粒" );
		TITLES.put( "O3", "臭氧" );
		TITLES.put( "CO", "一氧化碳" );
		TITLES.put( "SO2", "二氧化硫" );
		TITLES.put( "NO2", "二氧化氮" );

		UNITS.put( "PM2.5", "μg/m3" );
		UNITS.put( "PM10", "μg/m3" );
		UNITS.put( "O3", "ppb" );
		UNITS.put( "CO", "ppm" );
		UNITS.put( "SO2", "ppb" );
		UNITS.put( "NO2", "ppb" );
	}

	@Autowired
	private AQI aqi;

	@PostMapping( "/aqi" )
	public String aqi( @RequestParam String command, @RequestParam String text ) {
		try {
			String[] params = StringUtils.split( StringUtils.defaultIfEmpty( text, DEFAULT ) );

			check( params.length == 2, "參數個數有誤: " + text );

			String filter = Filter.and( Filter.COUNTY.eq( params[ 0 ] ), Filter.SITE_NAME.eq( params[ 1 ] ) );

			Map<String, ?> info = checkNull( aqi.call( filter ).stream().findFirst().orElse( null ), "測站有誤: " + text );

			String aqi = StringUtils.defaultIfEmpty( Cast.string( info, "AQI" ), NA ), status = Cast.string( info, "Status" ), color;

			color = "良好".equals( status ) ? "good" : "普通".equals( status ) ? "warning" : "設備維護".equals( status ) ? "#3AA3E3" : "danger";

			SlackAttachment attach = Slack.attachment( color ).setTitle( TITLE ).setTitleLink( LINK );

			attach.addFields( field( "AQI指標", aqi ) ).addFields( field( "狀態", Cast.string( info, "Status" ) ) );

			TITLES.keySet().forEach( i -> attach.addFields( field( TITLES.get( i ), value( Cast.string( info, i ), UNITS.get( i ) ) ) ) );

			return message( Slack.message( attach.setFallback( String.format( "%sAQI - %s", text, aqi ) ), command, text ) );

		} catch ( RuntimeException e ) {
			log.error( "", e );

			return e.getMessage();

		}
	}

	private String value( String value, String unit ) {
		return StringUtils.isEmpty( StringUtils.remove( value, "-" ) ) ? NA : String.join( StringUtils.SPACE, value, unit );
	}
}