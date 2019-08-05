package ninja.service;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import ninja.util.Jsoup;

@Service
public class Metro extends Data<String> {
	public static final String URL = "https://m.metro.taipei/pda_ticket_price_time.asp";

	@Override
	void init( Map<String, String> data ) {
		Jsoup.select( URL, "select#sstation option", i -> data.put( StringUtils.split( i.text() )[ 1 ], i.val() ) );
	}
}