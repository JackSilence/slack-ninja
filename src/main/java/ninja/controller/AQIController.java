package ninja.controller;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.fluent.Request;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.net.UrlEscapers;

import magic.util.Utils;
import net.gpedro.integrations.slack.SlackAttachment;
import net.gpedro.integrations.slack.SlackMessage;
import ninja.util.Cast;
import ninja.util.Gson;
import ninja.util.Slack;

@RestController
public class AQIController extends BaseController {
	private static final String URL = "http://opendata.epa.gov.tw/ws/Data/AQI/?$format=json&$filter=";

	private static final String FILTER = "County eq '臺北市' and SiteName eq '%s'", DEFAULT_SITE = "松山";

	private static final String AQI = "空氣品質指標AQI: %s (%s)", TITLE = "空氣品質監測網", LINK = "https://airtw.epa.gov.tw";

	private static final Map<String, String> TITLES = new LinkedHashMap<>(), UNITS = new HashMap<>();

	static {
		TITLES.put( "PM2.5", "細懸浮微粒" );
		TITLES.put( "PM10", "懸浮微粒" );
		TITLES.put( "O3", "臭氧" );
		TITLES.put( "CO", "一氧化碳" );
		TITLES.put( "SO2", "一氧化碳" );
		TITLES.put( "NO2", "二氧化氮" );

		UNITS.put( "PM2.5", " μg/m3" );
		UNITS.put( "PM10", " μg/m3" );
		UNITS.put( "O3", " ppb" );
		UNITS.put( "CO", " ppm" );
		UNITS.put( "SO2", " ppb" );
		UNITS.put( "NO2", " ppb" );
	}

	@PostMapping( "/aqi" )
	public String aqi( @RequestParam String command, @RequestParam String text ) {
		String url = URL + String.format( FILTER, StringUtils.defaultIfEmpty( text, DEFAULT_SITE ) );

		try {
			String json = Utils.getEntityAsString( Request.Get( UrlEscapers.urlFragmentEscaper().escape( url ) ) );

			Map<?, ?> info = checkNull( Cast.map( Gson.list( json ).stream().findFirst().orElse( null ) ), "測站有誤: " + text );

			String aqi = String.format( AQI, info.get( "AQI" ), info.get( "Status" ) );

			SlackMessage message = Slack.message( Slack.attachment().setText( aqi ).setTitle( TITLE ).setTitleLink( LINK ), command, text );

			SlackAttachment attach = Slack.attachment( "good" );

			TITLES.keySet().forEach( i -> attach.addFields( field( TITLES.get( i ), Cast.string( info, i ) + UNITS.get( i ) ) ) );

			return message( message.addAttachments( attach ) );

		} catch ( RuntimeException e ) {
			log.error( "", e );

			return e.getMessage();

		}
	}
}