package ninja.service;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import ninja.util.Jsoup;

@Service
public class Metro extends Data<String> {
	public static final String URL = "https://web.metro.taipei/c/TicketALLresult.asp";

	@Override
	void init( Map<String, String> data ) {
		Jsoup.select( URL, "select#bstation option", i -> data.put( StringUtils.split( i.text() )[ 1 ], StringUtils.substringAfterLast( i.val(), "-" ) ) );
	}
}