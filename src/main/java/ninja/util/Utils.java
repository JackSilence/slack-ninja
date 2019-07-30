package ninja.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

public class Utils {
	public static String spacer( String... elements ) {
		return String.join( StringUtils.SPACE, elements );
	}

	public static String find( String regex, String input ) {
		Matcher matcher = Pattern.compile( regex ).matcher( input );

		return matcher.find() ? matcher.group( 1 ) : null;
	}
}