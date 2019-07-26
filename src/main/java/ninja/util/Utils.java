package ninja.util;

import org.apache.commons.lang3.StringUtils;

public class Utils {
	public static String spacer( String... elements ) {
		return String.join( StringUtils.SPACE, elements );
	}
}