package ninja.controller;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Element;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;

import ninja.consts.Dialog;
import ninja.util.Jsoup;

@RestController
public class MovieController extends DialogController {
	private static final String URL = "http://www.atmovies.com.tw", PATH = "/showtime/a02/";

	private static final Map<String, Map<String, String>> THEATERS = new LinkedHashMap<>();

	@Override
	protected Object[] args() {
		return ArrayUtils.toArray( json( THEATERS.entrySet().stream().map( i -> {
			return ImmutableMap.of( LABEL, i.getKey(), OPTIONS, i.getValue().keySet().stream().map( super::option ).collect( Collectors.toList() ) );
		} ) ) );
	}

	@PostMapping( "/theater" )
	public void theater( @RequestParam String command, @RequestParam String text, @RequestParam( TRIGGER_ID ) String id ) {
		List<String> films = new ArrayList<>();

		Jsoup.select( URL + checkNull( THEATERS.values().stream().flatMap( i -> i.entrySet().stream() ).filter( i -> {
			return i.getKey().equals( text );
		} ).map( Entry::getValue ).findFirst().orElse( null ), "查無影院: " + text ), "div#theaterShowtimeBlock > ul#theaterShowtimeTable", i -> {
			films.add( i.selectFirst( "li.filmTitle" ).text() );
		} );

		dialog( id, Dialog.MOVIE, options( films ) );
	}

	@PostConstruct
	private void init() {
		Jsoup.select( URL + PATH, "ul#theaterList > li", i -> {
			if ( i.hasClass( "type0" ) ) {
				THEATERS.put( StringUtils.remove( i.text(), "▼" ), new LinkedHashMap<>() );
			} else {
				Element element = i.selectFirst( "a" );

				THEATERS.get( Iterables.getLast( THEATERS.keySet() ) ).put( element.text(), element.attr( "href" ) );
			}
		} );
	}
}