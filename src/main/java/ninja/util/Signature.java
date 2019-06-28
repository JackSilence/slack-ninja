package ninja.util;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.digest.HmacAlgorithms;

public class Signature {
	public static byte[] hmac( String data, String secret, HmacAlgorithms algorithms ) {
		try {
			String algorithm = algorithms.toString();

			Mac mac = Mac.getInstance( algorithm );

			mac.init( new SecretKeySpec( secret.getBytes( StandardCharsets.UTF_8 ), algorithm ) );

			return mac.doFinal( data.getBytes( StandardCharsets.UTF_8 ) );

		} catch ( NoSuchAlgorithmException | InvalidKeyException e ) {
			throw new RuntimeException( e );

		}
	}
}