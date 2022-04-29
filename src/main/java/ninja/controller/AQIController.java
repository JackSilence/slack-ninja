package ninja.controller;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import net.gpedro.integrations.slack.SlackAttachment;
import ninja.consts.Color;
import ninja.service.AQI;
import ninja.util.Cast;
import ninja.util.Check;
import ninja.util.Gson;
import ninja.util.Slack;
import ninja.util.Utils;

@RestController
public class AQIController extends GroupController<List<String>> {
	private static final String API_URL = "https://data.epa.gov.tw/api/v2/aqx_p_432?format=json&limit=100&filters=sitename,EQ,%s&api_key=%s";

	private static final String DEFAULT = "松山", TITLE = "空氣品質監測網", LINK = "https://airtw.epa.gov.tw", NA = "N/A";

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

	@Value( "${epa.api.key:}" )
	private String key;

	@Override
	protected Object[] args() {
		return ArrayUtils.toArray( DEFAULT, groups( aqi ) );
	}

	@Override
	protected Stream<Map<String, String>> group( Entry<String, List<String>> entry ) {
		return entry.getValue().stream().map( super::option );
	}

	@PostMapping( "/aqi" )
	@Async
	public void aqi( @RequestParam String command, @RequestParam String text, @RequestParam( RESPONSE_URL ) String url ) {
		String site = StringUtils.defaultIfEmpty( text, DEFAULT ), county;

		county = Check.first( aqi.data().entrySet().stream().filter( i -> i.getValue().contains( site ) ), "查無測站: " + site ).getKey();

		Map<?, ?> result = Gson.from( Utils.call( String.format( API_URL, site, key ) ), Map.class );

		Map<?, ?> info = Cast.map( Check.first( Cast.list( result, "records" ).stream(), "查無資料: " + site ) );

		String aqi = StringUtils.defaultIfEmpty( Cast.string( info, "aqi" ), NA ), status = Cast.string( info, "status" );

		Color color = "良好".equals( status ) ? Color.G : "普通".equals( status ) ? Color.Y : "設備維護".equals( status ) ? Color.B : Color.R;

		SlackAttachment attach = Slack.attachment( color ).setTitle( TITLE ).setTitleLink( LINK ).setText( tag( county, site ) );

		attach.addFields( field( "AQI指標", aqi ) ).addFields( field( "狀態", status ) );

		TITLES.keySet().forEach( i -> attach.addFields( field( TITLES.get( i ), value( Cast.string( info, i.toLowerCase() ), UNITS.get( i ) ) ) ) );

		message( attach.setFallback( String.format( "%s%sAQI: %s", county, site, aqi ) ), command, text, url );
	}

	public void aqi( String command, String url ) {
		aqi( command, StringUtils.EMPTY, url ); // 利用Proxy模式下內部呼叫非同步不會作用的機制提供此方法給Task @Retryable
	}

	private String value( String value, String unit ) {
		return StringUtils.isEmpty( StringUtils.remove( value, "-" ) ) ? NA : Utils.spacer( value, unit );
	}
}
