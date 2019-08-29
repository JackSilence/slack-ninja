package ninja.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import ninja.util.Cast;
import ninja.util.Gson;
import ninja.util.Utils;

@Service
public class Music extends Data<List<String>> {
	@Value( "${playlist.url:}" )
	private String url;

	@Override
	void init( Map<String, List<String>> data ) {
		String id = StringUtils.substringBefore( StringUtils.substringAfterLast( url, "/" ), "?" );

		Map<?, ?> map = Gson.from( Utils.call( url ), Map.class );

		for ( String i : Arrays.asList( "storePlatformData", "playlist-product", "results", id ) ) {
			map = Cast.map( map, i );
		}

		Map<?, ?> children = Cast.map( map, "children" );

		Cast.list( map, "childrenIds" ).stream().map( i -> Cast.map( children, i.toString() ) ).forEach( i -> {
			String name = Cast.string( i, "name" ), artist = Cast.string( i, "artistName" ), url = Cast.string( i, "url" );

			data.compute( name, ( k, v ) -> v == null ? new ArrayList<>() : v ).add( String.format( "%s - <%s|%s>", artist, url, name ) );
		} );
	}
}