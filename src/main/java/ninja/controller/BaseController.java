package ninja.controller;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.binary.Hex;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import magic.util.Utils;
import net.gpedro.integrations.slack.SlackAttachment;
import net.gpedro.integrations.slack.SlackField;
import ninja.util.Gson;
import ninja.util.Slack;

public abstract class BaseController {
	protected final Logger log = LoggerFactory.getLogger( this.getClass() );

	protected static final String REQ_BODY = "req_body", VERSION = "v0", ALGORITHM = "HmacSHA256";

	private static final String HEADER_TIMESTAMP = "X-Slack-Request-Timestamp", HEADER_SIGNATURE = "X-Slack-Signature";

	private static final String API_URL = "https://slack.com/api/", QUERY = "?token=%s&channel=%s";

	@Value( "${slack.signing.secret:}" )
	private String secret;

	@ModelAttribute
	public void verify( @RequestHeader( HEADER_TIMESTAMP ) String timestamp, @RequestHeader( HEADER_SIGNATURE ) String signature, @RequestBody String body, HttpServletRequest request ) {
		Instant instant = Instant.ofEpochSecond( Long.valueOf( timestamp ) );

		Assert.isTrue( instant.plus( 5, ChronoUnit.MINUTES ).compareTo( Instant.now() ) >= 0, "Instant: " + instant );

		String digest = digest( String.join( ":", VERSION, timestamp, body ) );

		Assert.isTrue( signature.equals( digest ), String.join( "!=", signature, digest ) );

		request.setAttribute( REQ_BODY, body );
	}

	protected Map<?, ?> map( Map<?, ?> map, String key ) {
		return map( map.get( key ) );
	}

	protected Map<?, ?> map( Object object ) {
		return ( Map<?, ?> ) object;
	}

	protected String string( Map<?, ?> map, String key ) {
		return ( String ) map.get( key );
	}

	protected String message( SlackAttachment attach, String command, String text ) {
		return Slack.message( attach, command, text ).prepare().toString();
	}

	protected String get( String method, String token, String channel, String query ) {
		return call( Request.Get( uri( method ) + String.format( QUERY, token, channel ) + query ) );
	}

	protected String post( String method, String token, Object src ) {
		Request request = Request.Post( uri( method ) ).setHeader( "Authorization", "Bearer " + token );

		return call( request.bodyString( Gson.json( src ), ContentType.APPLICATION_JSON ) );
	}

	protected SlackField field( String title, String value ) {
		return new SlackField().setShorten( true ).setTitle( title ).setValue( value );
	}

	private String digest( String content ) {
		try {
			Mac mac = Mac.getInstance( ALGORITHM );

			mac.init( new SecretKeySpec( secret.getBytes( StandardCharsets.UTF_8 ), ALGORITHM ) );

			return String.join( "=", VERSION, Hex.encodeHexString( mac.doFinal( content.getBytes( StandardCharsets.UTF_8 ) ) ) );

		} catch ( NoSuchAlgorithmException | InvalidKeyException e ) {
			throw new RuntimeException( e );

		}
	}

	private String uri( String method ) {
		return API_URL + method;
	}

	private String call( Request request ) {
		return Utils.getEntityAsString( request );
	}
}