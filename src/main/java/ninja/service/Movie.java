package ninja.service;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

import com.google.common.collect.Iterables;

import ninja.util.Jsoup;

@Service
public class Movie extends Data<Map<String, String>> {
	public static final String URL = "http://www.atmovies.com.tw";

	private static final String PATH = "/showtime/a02/";

	public Element link( Element element ) {
		return element.selectFirst( "a[href]" );
	}

	@Override
	void init( Map<String, Map<String, String>> data ) {
		Jsoup.select( URL + PATH, "ul#theaterList > li", i -> {
			if ( i.hasClass( "type0" ) ) {
				data.put( StringUtils.remove( i.text(), "▼" ), new LinkedHashMap<>() );
			} else {
				var link = link( i );

				data.get( Iterables.getLast( data.keySet() ) ).put( StringUtils.remove( link.text(), "★ " ), Jsoup.href( link ) );
			}
		} );
	}
}