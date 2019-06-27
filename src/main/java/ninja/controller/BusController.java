package ninja.controller;

import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.stream.Collectors;

import org.apache.commons.codec.digest.HmacAlgorithms;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.fluent.Request;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.reflect.TypeToken;

import magic.util.Utils;
import ninja.util.Gson;

@RestController
public class BusController extends BaseController {
	private static final String AUTH_HEADER = "hmac username=\"%s\", algorithm=\"hmac-sha1\", headers=\"x-date\", signature=\"%s\"";

	private static final String API_URL = "https://ptx.transportdata.tw/MOTC/v2/Bus/%s/City/Taipei/%s?$format=JSON&%s";

	private static final String WEB_URL = "http://www.e-bus.gov.taipei/newmap/Tw/Map?rid=%d&sec=0";

	@Value( "${ptx.app.id:}" )
	private String id;

	@Value( "${ptx.app.key:}" )
	private String key;

	@PostMapping( "/bus" )
	public String bus( @RequestParam String command, @RequestParam String text ) {
		try {
			String[] params = StringUtils.split( text );

			Assert.isTrue( params.length == 2, "路線及站牌關鍵字皆須輸入" );

			String route = params[ 0 ], keyword = params[ 1 ];

			Map<String, ?> bus = call( "Route", route ).get( 0 );

			call( "EstimatedTimeOfArrival", route, "$orderby=Direction" ).stream().filter( i -> {
				log.info( i.get( "Direction" ).getClass().getName() );
				return stop( i ).contains( keyword );

			} ).collect( Collectors.groupingBy( i -> string( map( i, "StopName" ), "Zh_tw" ), Collectors.toList() ) ).forEach( ( k, v ) -> {

			} );

			return null;

		} catch ( RuntimeException e ) {
			log.error( "", e );

			return e.getMessage();

		}
	}

	private List<Map<String, ?>> call( String method, String route, String... query ) {
		Request request = Request.Get( String.format( API_URL, method, route, String.join( "&", query ) ) );

		String xdate = xdate(), signature = Base64.getEncoder().encodeToString( signature( "x-date: " + xdate, key, HmacAlgorithms.HMAC_SHA_1 ) );

		request.addHeader( "Authorization", String.format( AUTH_HEADER, id, signature ) ).addHeader( "x-date", xdate );

		return Gson.from( Utils.getEntityAsString( request.addHeader( "Accept-Encoding", "gzip" ) ), new TypeToken<List<Map<Integer, ?>>>() {
		}.getType() );
	}

	private String stop( Map<String, ?> map ) {
		return string( map( map, "StopName" ), "Zh_tw" );
	}

	private String xdate() {
		SimpleDateFormat sdf = new SimpleDateFormat( "EEE, dd MMM yyyy HH:mm:ss z", Locale.US );

		sdf.setTimeZone( TimeZone.getTimeZone( "GMT" ) );

		return sdf.format( new Date() );
	}
}