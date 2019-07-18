package ninja.service;

import java.util.List;
import java.util.Map;

import org.apache.http.client.fluent.Request;
import org.springframework.stereotype.Service;

import com.google.common.net.UrlEscapers;

import magic.util.Utils;
import ninja.util.Gson;

@Service
public class AQI {
	private static final String API_URL = "http://opendata.epa.gov.tw/ws/Data/AQI/?$format=json&$filter=";

	public List<Map<String, ?>> call( String filter ) {
		return Gson.list( Utils.getEntityAsString( Request.Get( API_URL + UrlEscapers.urlFragmentEscaper().escape( filter ) ) ) );
	}
}