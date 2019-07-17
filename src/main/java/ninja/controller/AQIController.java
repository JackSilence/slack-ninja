package ninja.controller;

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
import ninja.util.Cast;
import ninja.util.Gson;
import ninja.util.Slack;

@RestController
public class AQIController extends BaseController {
	private static final String URL = "http://opendata.epa.gov.tw/ws/Data/AQI/?$format=json&$filter=";

	private static final String FILTER = "County eq '臺北市' and SiteName eq '%s'", DEFAULT_SITE = "松山";

	private static final Map<String, String> TITLES = new LinkedHashMap<>();

	static {
		TITLES.put( "PM2.5", "細懸浮微粒" );
		TITLES.put( "PM2.5_AVG", "平均 (μg/m3)" );
		TITLES.put( "PM10", "懸浮微粒" );
		TITLES.put( "PM10_AVG", "平均 (μg/m3)" );
		TITLES.put( "O3", "臭氧" );
		TITLES.put( "O3_8hr", "平均 (ppb)" );
		TITLES.put( "CO", "一氧化碳" );
		TITLES.put( "CO_8hr", "平均 (ppm)" );
	}

	@PostMapping( "/aqi" )
	public String aqi( @RequestParam String command, @RequestParam String text ) {
		String url = URL + String.format( FILTER, StringUtils.defaultIfEmpty( text, DEFAULT_SITE ) );

		try {
			String json = Utils.getEntityAsString( Request.Get( UrlEscapers.urlFragmentEscaper().escape( url ) ) );

			Map<?, ?> info = checkNull( Cast.map( Gson.list( json ).stream().findFirst().orElse( null ) ), "測站有誤: " + text );

			SlackAttachment attach = Slack.attachment( "good" );

			TITLES.keySet().forEach( i -> attach.addFields( field( TITLES.get( i ), Cast.string( info, i ) ) ) );

			return message( Slack.message( attach, command, text ) );

		} catch ( RuntimeException e ) {
			log.error( "", e );

			return e.getMessage();

		}
	}
}