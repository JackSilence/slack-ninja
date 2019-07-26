package ninja.service;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import ninja.util.Jsoup;

@Service
public class Metro {
	public static final String URL = "https://m.metro.taipei/pda_ticket_price_time.asp";

	private static final Map<String, String> STATIONS = new HashMap<>();

	public Stream<String> find( String name ) {
		return STATIONS.keySet().stream().filter( i -> i.contains( name ) );
	}

	public String get( String name ) {
		return STATIONS.get( name );
	}

	@PostConstruct
	private void init() {
		Jsoup.select( URL, "select#sstation option", i -> STATIONS.put( StringUtils.split( i.text() )[ 1 ], i.val() ) );
	}
}