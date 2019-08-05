package ninja.controller;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.fluent.Request;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.ImmutableMap;
import com.google.common.net.UrlEscapers;

import magic.util.Utils;
import net.gpedro.integrations.slack.SlackAttachment;
import ninja.consts.Filter;
import ninja.util.Check;
import ninja.util.Gson;
import ninja.util.Slack;

@RestController
public class AQIController extends DialogController {
	private static final String SITE_URL = "https://airtw.epa.gov.tw/ajax.aspx?Target=Get%s&%s=", COUNTY = "County";

	private static final String NAME = "Name", API_URL = "http://opendata.epa.gov.tw/ws/Data/AQI/?$format=json&$filter=";

	private static final String DEFAULT = "松山", TITLE = "空氣品質監測網", LINK = "https://airtw.epa.gov.tw", NA = "N/A";

	private static final Map<String, List<String>> SITES = new LinkedHashMap<>();

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

	@Override
	protected Object[] args() {
		return ArrayUtils.toArray( DEFAULT, json( SITES.entrySet().stream().map( i -> {
			return ImmutableMap.of( LABEL, i.getKey(), OPTIONS, list( i.getValue().stream().map( super::option ) ) );
		} ) ) );
	}

	@PostMapping( "/aqi" )
	public String aqi( @RequestParam String command, @RequestParam String text ) {
		String site = StringUtils.defaultIfEmpty( text, DEFAULT ), county;

		county = Check.first( SITES.entrySet().stream().filter( i -> i.getValue().contains( site ) ), "查無測站: " + site ).getKey();

		Map<String, String> info = call( API_URL + UrlEscapers.urlFragmentEscaper().escape( Filter.SITE_NAME.eq( site ) ) ).get( 0 );

		String aqi = StringUtils.defaultIfEmpty( info.get( "AQI" ), NA ), status = info.get( "Status" ), color;

		color = "良好".equals( status ) ? "good" : "普通".equals( status ) ? "warning" : "設備維護".equals( status ) ? "#3AA3E3" : "danger";

		SlackAttachment attach = Slack.attachment( color ).setTitle( TITLE ).setTitleLink( LINK ).setText( tag( county, site ) );

		attach.addFields( field( "AQI指標", aqi ) ).addFields( field( "狀態", status ) );

		TITLES.keySet().forEach( i -> attach.addFields( field( TITLES.get( i ), value( info.get( i ), UNITS.get( i ) ) ) ) );

		return message( attach.setFallback( String.format( "%s%sAQI: %s", county, site, aqi ) ), command, text );
	}

	private List<Map<String, String>> call( String uri ) {
		return Gson.list( Utils.getEntityAsString( Request.Get( uri ) ) );
	}

	private String value( String value, String unit ) {
		return StringUtils.isEmpty( StringUtils.remove( value, "-" ) ) ? NA : ninja.util.Utils.spacer( value, unit );
	}

	private String url( String target, String field ) {
		return String.format( SITE_URL, target, field );
	}

	@PostConstruct
	private void init() {
		call( url( COUNTY, "AreaID" ) ).forEach( i -> {
			SITES.put( i.get( NAME ), list( call( url( "Site", COUNTY ) + i.get( "Value" ) ).stream().map( j -> j.get( NAME ) ) ) );
		} );
	}
}