package ninja.service;

import java.util.List;
import java.util.Map;

import ninja.util.Cast;
import ninja.util.Gson;
import ninja.util.Utils;
import org.apache.http.client.fluent.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class Metro extends Data<String> {
	private final Logger log = LoggerFactory.getLogger( this.getClass() );

	public static final String API_URL = "https://web.metro.taipei/apis/metrostationapi/", MENU_LINE = "menuline";

	@Override
	void init( Map<String, String> data ) {
		try {
			init( data, Utils.call( Request.Post( api( MENU_LINE ) ), "{\"LineID\":\"0\",\"Lang\":\"tw\"}" ) );

			if ( data.isEmpty() ) {
				throw new RuntimeException( "無法取得捷運車站資料" );
			}
		} catch ( RuntimeException e ) {
			log.error( "使用預設捷運車站資料", e );

			init( data, magic.util.Utils.getResourceAsString( "/data/mrt_station.json" ) );
		}
	}

	public String api( String path ) {
		return API_URL.concat( path );
	}

	private void init( Map<String, String> data, String json ) {
		List<Map<String, ?>> list = Gson.listOfMaps( json );

		list.stream().flatMap( i -> Cast.list( i, "LineStations" ).stream() ).map( Cast::map ).forEach( i -> {
			data.put( Cast.string( i, "StationName" ), Cast.string( i, "SID" ) );
		} );
	}
}