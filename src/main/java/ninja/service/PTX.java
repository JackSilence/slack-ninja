package ninja.service;

import java.util.List;
import java.util.Map;

import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import com.google.common.net.HttpHeaders;
import com.google.common.net.UrlEscapers;

import ninja.util.Cast;
import ninja.util.Gson;
import ninja.util.Utils;

public abstract class PTX extends Data<String> {
	private final Logger log = LoggerFactory.getLogger( this.getClass() );

	private static final String TOKEN_URL = "https://tdx.transportdata.tw/auth/realms/TDXConnect/protocol/openid-connect/token";

	private static final String TOKEN_DATA = "grant_type=client_credentials&client_id=%s&client_secret=%s";

	private static final String API_URL = "https://tdx.transportdata.tw/api/basic/v2/%s?$format=JSON&$filter=%s&%s";

	@Value( "${tdx.client.id:}" )
	private String id;

	@Value( "${tdx.client.secret:}" )
	private String secret;

	public List<Map<String, ?>> call( String path, String filter, String... query ) {
		var uri = UrlEscapers.urlFragmentEscaper().escape( String.format( API_URL, path, filter, String.join( "&", query ) ) );

		log.info( "Uri: {}", uri );

		var request = Request.Get( uri ).setHeader( HttpHeaders.AUTHORIZATION, token() );

		return Gson.listOfMaps( Utils.call( request.addHeader( HttpHeaders.ACCEPT_ENCODING, "gzip" ) ) );
	}

	public String station( Map<?, ?> map ) {
		return name( map, "StationName" );
	}

	public String name( Map<?, ?> map, String key ) {
		return Cast.string( Cast.map( map, key ), "Zh_tw" );
	}

	private String token() {
		var token = Utils.call( Request.Post( TOKEN_URL ).bodyString( String.format( TOKEN_DATA, id, secret ), ContentType.APPLICATION_FORM_URLENCODED ) );

		return String.format( "Bearer %s", Cast.string( Gson.from( token, Map.class ), "access_token" ) );
	}
}
