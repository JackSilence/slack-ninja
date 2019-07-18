package ninja.util;

import java.util.List;
import java.util.Map;

import org.apache.http.client.fluent.Request;

import com.google.common.net.UrlEscapers;

import magic.util.Utils;

public class AQI {
	private static final String API_URL = "http://opendata.epa.gov.tw/ws/Data/AQI/?$format=json&$filter=";

	public static List<Map<String, ?>> call( String filter ) {
		return Gson.list( Utils.getEntityAsString( Request.Get( API_URL + UrlEscapers.urlFragmentEscaper().escape( filter ) ) ) );
	}
}