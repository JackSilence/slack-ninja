package ninja.util;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

public class Metro {
	public static final String URL = "https://m.metro.taipei/pda_ticket_price_time.asp";

	private static final Map<String, String> STATIONS = new HashMap<>();

	static {
		Jsoup.select( URL, "select#sstation option", i -> STATIONS.put( StringUtils.split( i.text() )[ 1 ], i.val() ) );
	}

	public static Stream<String> find( String name ) {
		return STATIONS.keySet().stream().filter( i -> i.contains( name ) );
	}

	public static String get( String name ) {
		return STATIONS.get( name );
	}
}