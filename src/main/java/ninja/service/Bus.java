package ninja.service;

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
	public static final String ROUTES_URL = "https://ebus.gov.taipei/EBus/RouteList?ct=tpc";

	private static final String ROUTE_ID_REGEX = "javascript:go\\('(.+?)'\\)", PATH = "Bus/%s/City/Taipei", QUOTE = "\"";

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
		return data().get( route );
	}

	public boolean check( String route ) {
		return data().containsKey( route );
	}

	@Override
	void init( Map<String, String> data ) {
		Jsoup.select( ROUTES_URL, "section.busline li > a", i -> {
			String id = Utils.find( ROUTE_ID_REGEX, Jsoup.href( i ) );

			if ( id != null ) {
				data.put( i.text(), id );
			}
		} );
	}
}