package ninja.service;

import java.util.List;
import java.util.Map;

import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.springframework.stereotype.Service;

import ninja.util.Gson;
import ninja.util.Utils;

@Service
public class AQI extends Data<List<String>> {
	private static final String SITE_URL = "https://airtw.moenv.gov.tw/ajax.aspx";

	private static final String SITE_PAYLOAD = "Target=Get%s&%s";

	private static final String COUNTY = "County", NAME = "Name";

	@Override
	void init( Map<String, List<String>> data ) {
		call( request( COUNTY, "AreaID=&SiteID=" ) ).forEach( i -> {
			data.put( i.get( NAME ), Utils.list( call( request( "Site", COUNTY + "=" + i.get( "Value" ) ) ).stream().map( j -> j.get( NAME ) ) ) );
		} );
	}

	private List<Map<String, String>> call( Request request ) {
		return Gson.listOfMaps( Utils.call( request ) );
	}

	private Request request( String target, String query ) {
		return Request.Post( SITE_URL ).bodyString( String.format( SITE_PAYLOAD, target, query ), ContentType.APPLICATION_FORM_URLENCODED );
	}
}
