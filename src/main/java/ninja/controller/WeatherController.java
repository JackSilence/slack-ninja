package ninja.controller;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.fluent.Request;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import magic.util.Utils;
import ninja.util.Gson;

@RestController
public class WeatherController extends BaseController {
	private static final String URL = "https://opendata.cwb.gov.tw/api/v1/rest/datastore/F-D0047-061";

	private static final String QUERY = "?Authorization=%s&locationName=%s";

	@Value( "${cwb.api.key:}" )
	private String key;

	@SuppressWarnings( "unchecked" )
	@PostMapping( "/weather" )
	public String weather( @RequestParam String command, @RequestParam String text ) {
		String district = StringUtils.appendIfMissing( StringUtils.defaultIfEmpty( text, "內湖區" ), "區" );

		try {
			Map<String, ?> result = Gson.from( Utils.getEntityAsString( Request.Get( URL + String.format( QUERY, key, district ) ) ), Map.class );

			Assert.notEmpty( result, "查無此區域: " + text );

			result = ( ( Map<String, List<Map<String, ?>>> ) result.get( "records" ) ).get( "locations" ).get( 0 );

			log.info( result.toString() );

		} catch ( RuntimeException e ) {
			log.error( "", e );

			return e.getMessage();

		}

		return text;

	}
}