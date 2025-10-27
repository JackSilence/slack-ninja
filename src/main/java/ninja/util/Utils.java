package ninja.util;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;

import net.gpedro.integrations.slack.SlackMessage;

public class Utils {
	public static String call( Request request ) {
		return magic.util.Utils.getEntityAsString( request.setHeader( "Connection", "close" ), 120000 );
	}

	public static String call( Request request, String body ) {
		return call( request.bodyString( body, ContentType.APPLICATION_JSON ) );
	}

	public static String call( String uri ) {
		return call( Request.Get( uri ) );
	}

	public static String call( String uri, SlackMessage message ) {
		var data = message.prepare();

		data.addProperty( "replace_original", false );

		return call( Request.Post( uri ), data.toString() );
	}

	public static String spacer( String... elements ) {
		return String.join( StringUtils.SPACE, elements );
	}

	public static String find( String regex, String input ) {
		var matcher = Pattern.compile( regex ).matcher( input );

		return matcher.find() ? matcher.group( 1 ) : null;
	}

	public static String join( Stream<String> stream, String delimiter ) {
		return stream.collect( Collectors.joining( delimiter ) );
	}

	public static <T> List<T> list( Stream<T> stream ) {
		return stream.collect( Collectors.toList() );
	}
}