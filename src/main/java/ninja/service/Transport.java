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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.gson.reflect.TypeToken;

import magic.util.Utils;
import ninja.util.Gson;
import ninja.util.Signature;

@Service
public class Transport {
	private static final String AUTH_HEADER = "hmac username=\"%s\", algorithm=\"hmac-sha1\", headers=\"x-date\", signature=\"%s\"";

	private static final String API_URL = "https://ptx.transportdata.tw/MOTC/v2/Bus/%s/City/Taipei/%s?$format=JSON&%s";

	private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern( "EEE, dd MMM yyyy HH:mm:ss z", Locale.US );

	@Value( "${ptx.app.id:}" )
	private String id;

	@Value( "${ptx.app.key:}" )
	private String key;

	public List<Map<String, ?>> call( String method, String route, String... query ) {
		Request request = Request.Get( String.format( API_URL, method, route, String.join( "&", query ) ) );

		String xdate = ZonedDateTime.now( ZoneId.of( "GMT" ) ).format( DATE_TIME_FORMATTER );

		String signature = Base64.getEncoder().encodeToString( Signature.hmac( "x-date: " + xdate, key, HmacAlgorithms.HMAC_SHA_1 ) );

		request.addHeader( "Authorization", String.format( AUTH_HEADER, id, signature ) ).addHeader( "x-date", xdate );

		return Gson.from( Utils.getEntityAsString( request.addHeader( "Accept-Encoding", "gzip" ) ), new TypeToken<List<?>>() {
		}.getType() );
	}
}
