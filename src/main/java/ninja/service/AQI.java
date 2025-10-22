package ninja.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import ninja.util.Cast;
import ninja.util.Gson;
import ninja.util.Utils;

@Service
public class AQI extends Data<List<String>> {
	private static final String SITE_URL = "https://data.moenv.gov.tw/api/v2/aqx_p_432?format=json&api_key=%s";

	@Value( "${epa.api.key:}" )
	private String key;

	@Override
	void init( Map<String, List<String>> data ) {
		var records = Cast.list( Gson.from( Utils.call( String.format( SITE_URL, key ) ), Map.class ), "records" ).stream().map( Cast::map );

		records.forEach( r -> data.computeIfAbsent( Cast.string( r, "county" ), k -> new ArrayList<>() ).add( Cast.string( r, "sitename" ) ) );
	}
}
