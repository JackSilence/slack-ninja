package ninja.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import ninja.util.Gson;
import ninja.util.Utils;

@Service
public class AQI extends Data<List<String>> {
	private static final String SITE_URL = "https://airtw.epa.gov.tw/ajax.aspx?Target=Get%s&%s=";

	private static final String COUNTY = "County", NAME = "Name";

	@Override
	void init( Map<String, List<String>> data ) {
		call( url( COUNTY, "AreaID=&SiteID" ) ).forEach( i -> {
			data.put( i.get( NAME ), Utils.list( call( url( "Site", COUNTY ) + i.get( "Value" ) ).stream().map( j -> j.get( NAME ) ) ) );
		} );
	}

	private List<Map<String, String>> call( String uri ) {
		return Gson.list( Utils.call( uri ) );
	}

	private String url( String target, String field ) {
		return String.format( SITE_URL, target, field );
	}
}