package ninja.util;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.fluent.Request;

public class Utils {
	public static String call( Request request ) {
		return magic.util.Utils.getEntityAsString( request );
	}

	public static String spacer( String... elements ) {
		return String.join( StringUtils.SPACE, elements );
	}

	public static String find( String regex, String input ) {
		Matcher matcher = Pattern.compile( regex ).matcher( input );

		return matcher.find() ? matcher.group( 1 ) : null;
	}

	public static <T> List<T> list( Stream<T> stream ) {
		return stream.collect( Collectors.toList() );
	}
}