package ninja.controller;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Element;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;

import ninja.util.Jsoup;

@RestController
public class MovieController extends DialogController {
	private static final String URL = "http://www.atmovies.com.tw", PATH = "/showtime/a02/";

	private static final Map<String, List<Map<String, String>>> THEATERS = new LinkedHashMap<>();

	@Override
	protected Object[] args() {
		return ArrayUtils.toArray( json( THEATERS.entrySet().stream().map( this::group ) ) );
	}

	@PostMapping( "/theater" )
	public String theater( @RequestParam String text ) {
		return null;
	}

	private Map<String, Object> group( Entry<String, List<Map<String, String>>> entry ) {
		return ImmutableMap.of( LABEL, entry.getKey(), OPTIONS, entry.getValue() );
	}

	@PostConstruct
	private void init() {
		Jsoup.select( URL + PATH, "ul#theaterList > li", i -> {
			if ( i.hasClass( "type0" ) ) {
				THEATERS.put( StringUtils.remove( i.text(), "▼" ), new ArrayList<>() );
			} else {
				Element element = i.selectFirst( "a" );

				THEATERS.get( Iterables.getLast( THEATERS.keySet() ) ).add( option( element.text(), element.attr( "href" ) ) );
			}
		} );
	}
}