package ninja.controller;

import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.HmacAlgorithms;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.fluent.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import com.google.common.collect.ImmutableMap;
import com.google.common.net.UrlEscapers;

import net.gpedro.integrations.slack.SlackAttachment;
import net.gpedro.integrations.slack.SlackField;
import net.gpedro.integrations.slack.SlackMessage;
import ninja.consts.Zone;
import ninja.util.Check;
import ninja.util.Gson;
import ninja.util.Signature;
import ninja.util.Slack;
import ninja.util.Utils;

public abstract class BaseController {
	protected final Logger log = LoggerFactory.getLogger( this.getClass() );

	protected static final String REQ_BODY = "req_body", CHANNEL_ID = "channel_id", TRIGGER_ID = "trigger_id", RESPONSE_URL = "response_url";

	protected static final String TEXT = "text", LABEL = "label", VALUE = "value", OPTIONS = "options";

	protected static final ZoneId ZONE_ID = ZoneId.of( Zone.TAIPEI );

	private static final String HEADER_TIMESTAMP = "X-Slack-Request-Timestamp", HEADER_SIGNATURE = "X-Slack-Signature";

	private static final String VERSION = "v0", API_URL = "https://slack.com/api/", API_QUERY = "?token=%s&channel=%s";

	private static final String COMMAND_METHOD = "chat.command", COMMAND_QUERY = "&command=/%s&text=%s";

	@Value( "${slack.signing.secret:}" )
	private String secret;

	@Value( "${slack.user.token:}" )
	private String token;

	@ModelAttribute
	public void verify( @RequestHeader( HEADER_TIMESTAMP ) String timestamp, @RequestHeader( HEADER_SIGNATURE ) String signature, @RequestBody String body, HttpServletRequest request ) {
		Instant instant = Instant.ofEpochSecond( Long.valueOf( timestamp ) );

		Check.expr( instant.plus( 5, ChronoUnit.MINUTES ).compareTo( Instant.now() ) >= 0, "Instant: " + instant );

		String digest = digest( String.join( ":", VERSION, timestamp, body.replace( "*", "%2A" ) ) );

		Check.equals( signature, digest, String.join( "!=", signature, digest ) );

		request.setAttribute( REQ_BODY, body );

		preHandle( request );
	}

	protected Map<String, String> option( String label ) {
		return option( label, label );
	}

	protected Map<String, String> option( String label, Object value ) {
		return ImmutableMap.of( LABEL, label, VALUE, value.toString() );
	}

	protected String get( String method, String channel, String query ) {
		return get( method, token, channel, query );
	}

	protected String get( String method, String token, String channel, String query ) {
		return Utils.call( uri( method ) + String.format( API_QUERY, token, channel ) + query );
	}

	protected String post( String method, Object src ) {
		return post( method, token, src );
	}

	protected String post( String method, String token, Object src ) {
		return Utils.call( Request.Post( uri( method ) ).setHeader( "Authorization", "Bearer " + token ), Gson.json( src ) );
	}

	protected String tag( String... tag ) {
		return Utils.spacer( Arrays.stream( tag ).map( i -> StringUtils.wrap( i, "`" ) ).toArray( String[]::new ) );
	}

	protected SlackField field( String title, String value ) {
		return new SlackField().setShorten( true ).setTitle( title ).setValue( value );
	}

	protected void preHandle( HttpServletRequest request ) {
	}

	protected void message( SlackAttachment attach, String command, String text, String url ) {
		message( Slack.message( attach, command, text ), url );
	}

	protected void message( SlackMessage message, String url ) {
		log.info( Utils.call( url, message ) );
	}

	protected void command( String user, String channel, String command, String text ) {
		String query = String.format( COMMAND_QUERY, command, UrlEscapers.urlFragmentEscaper().escape( text ) );

		log.info( get( COMMAND_METHOD, System.getenv( "slack.legacy.token." + user ), channel, query ) );
	}

	protected <T> List<T> list( Stream<T> stream ) {
		return Utils.list( stream );
	}

	private String digest( String content ) {
		return String.join( "=", VERSION, Hex.encodeHexString( Signature.hmac( content, secret, HmacAlgorithms.HMAC_SHA_256 ) ) );
	}

	private String uri( String method ) {
		return API_URL + method;
	}
}