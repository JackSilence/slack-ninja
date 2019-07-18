package ninja.util;

import java.util.List;
import java.util.Map;

import org.apache.http.client.fluent.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.net.UrlEscapers;

import magic.util.Utils;

public class AQI {
	private static final Logger log = LoggerFactory.getLogger( Jsoup.class );
	public static final String ID = "AQI", API_URL = "http://opendata.epa.gov.tw/ws/Data/AQI/?$format=json&$filter=";

	public static List<Map<String, ?>> call( String filter ) {
		log.error( API_URL + UrlEscapers.urlFragmentEscaper().escape( filter ) );
		return Gson.list( Utils.getEntityAsString( Request.Get( API_URL + UrlEscapers.urlFragmentEscaper().escape( filter ) ) ) );
	}
}