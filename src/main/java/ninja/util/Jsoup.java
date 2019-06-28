package ninja.util;

import java.io.IOException;

import org.jsoup.nodes.Document;

public class Jsoup {
	public static Document get( String url ) {
		try {
			return org.jsoup.Jsoup.connect( url ).get();

		} catch ( IOException e ) {
			throw new RuntimeException( e );

		}
	}
}