package ninja.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Service;

import ninja.util.Cast;
import ninja.util.Jsoup;

@Service
public class Bus extends PTX {
	private static final String ROUTES_URL = "https://ebus.gov.taipei/EBus/RouteList?ct=tpc";

	private static final String ROUTE_ID_REGEX = "javascript:go\\('(.+?)'\\)", PATH = "Bus/%s/City/Taipei";

	private static final Map<String, String> ROUTES = new HashMap<>();

	@Override
	public List<Map<String, ?>> call( String path, String filter, String... query ) {
		return super.call( String.format( PATH, path ), filter, query );
	}

	public Stream<String> stops( Map<String, ?> map, Function<? super Map<?, ?>, ? extends String> mapper ) {
		return Cast.list( map, "Stops" ).stream().map( Cast::map ).map( mapper );
	}

	public String stop( Map<?, ?> map ) {
		return name( map, "StopName" );
	}

	public String id( String route ) {
		return ROUTES.get( route );
	}

	public boolean check( String route ) {
		return ROUTES.containsKey( route );
	}

	@PostConstruct
	private void init() {
		Jsoup.select( ROUTES_URL, "section.busline li > a", i -> {
			Matcher matcher = Pattern.compile( ROUTE_ID_REGEX ).matcher( i.attr( "href" ) );

			if ( matcher.find() ) {
				ROUTES.put( i.text(), matcher.group( 1 ) );
			}
		} );
	}
}