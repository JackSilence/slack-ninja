package ninja.controller;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

public abstract class BaseController {
	protected final Logger log = LoggerFactory.getLogger( this.getClass() );

	private static final String HEADER_TIMESTAMP = "X-Slack-Request-Timestamp", HEADER_SIGNATURE = "X-Slack-Signature";

	private static final String VERSION = "v0", ALGORITHM = "HmacSHA256";

	@Value( "${slack.signing.secret:}" )
	private String secret;

	@ModelAttribute
	public void verify( @RequestHeader( HEADER_TIMESTAMP ) String timestamp, @RequestHeader( HEADER_SIGNATURE ) String signature, @RequestBody String body ) {
		Instant instant = Instant.ofEpochSecond( Long.valueOf( timestamp ) );

		Assert.isTrue( instant.plus( 5, ChronoUnit.MINUTES ).compareTo( Instant.now() ) >= 0, "Instant: " + instant );

		String digest = digest( String.join( ":", VERSION, timestamp, body ) );

		Assert.isTrue( signature.equals( digest ), String.join( "!=", signature, digest ) );
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
}