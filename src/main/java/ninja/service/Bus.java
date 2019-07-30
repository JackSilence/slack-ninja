package ninja.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import ninja.util.Cast;
import ninja.util.Jsoup;
import ninja.util.Utils;

@Service
public class Bus extends PTX {
	private static final String ROUTES_URL = "https://ebus.gov.taipei/EBus/RouteList?ct=tpc", QUOTE = "\"";

	private static final String ROUTE_ID_REGEX = "javascript:go\\('(.+?)'\\)", PATH = "Bus/%s/City/Taipei";

	private static final Map<String, String> ROUTES = new HashMap<>();

	static {
		Jsoup.select( ROUTES_URL, "section.busline li > a", i -> {
			String id = Utils.find( ROUTE_ID_REGEX, Jsoup.href( i ) );

			if ( !id.isEmpty() ) {
				ROUTES.put( i.text(), id );
			}
		} );
	}

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

	public String text( String route, String stop ) {
		return Utils.spacer( route, StringUtils.wrap( stop, QUOTE ) );
	}

	public String unwrap( String stop ) {
		return StringUtils.unwrap( stop, QUOTE );
	}

	public String id( String route ) {
		return ROUTES.get( route );
	}

	public boolean check( String route ) {
		return ROUTES.containsKey( route );
	}
}