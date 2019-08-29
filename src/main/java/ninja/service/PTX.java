package ninja.service;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.codec.digest.HmacAlgorithms;
import org.apache.http.client.fluent.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import com.google.common.net.HttpHeaders;
import com.google.common.net.UrlEscapers;

import ninja.util.Cast;
import ninja.util.Gson;
import ninja.util.Signature;
import ninja.util.Utils;

abstract class PTX extends Data<String> {
	private final Logger log = LoggerFactory.getLogger( this.getClass() );

	private static final String AUTH_HEADER = "hmac username=\"%s\", algorithm=\"hmac-sha1\", headers=\"x-date\", signature=\"%s\"";

	private static final String API_URL = "https://ptx.transportdata.tw/MOTC/v2/%s?$format=JSON&$filter=%s&%s";

	private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern( "EEE, dd MMM yyyy HH:mm:ss z", Locale.US );

	@Value( "${ptx.app.id:}" )
	private String id;

	@Value( "${ptx.app.key:}" )
	private String key;

	public List<Map<String, ?>> call( String path, String filter, String... query ) {
		String xdate = ZonedDateTime.now( ZoneId.of( "GMT" ) ).format( DATE_TIME_FORMATTER ), uri;

		String signature = Base64.getEncoder().encodeToString( Signature.hmac( "x-date: " + xdate, key, HmacAlgorithms.HMAC_SHA_1 ) );

		log.info( "Uri: {}", uri = UrlEscapers.urlFragmentEscaper().escape( String.format( API_URL, path, filter, String.join( "&", query ) ) ) );

		Request request = Request.Get( uri ).addHeader( HttpHeaders.AUTHORIZATION, String.format( AUTH_HEADER, id, signature ) ).addHeader( "x-date", xdate );

		return Gson.list( Utils.call( request.addHeader( HttpHeaders.ACCEPT_ENCODING, "gzip" ) ) );
	}

	public String station( Map<?, ?> map ) {
		return name( map, "StationName" );
	}

	public String name( Map<?, ?> map, String key ) {
		return Cast.string( Cast.map( map, key ), "Zh_tw" );
	}
}